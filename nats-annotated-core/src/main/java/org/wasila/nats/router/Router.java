/**
 * (C) Copyright 2016 Adam Wasila.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wasila.nats.router;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.ConnectionFactory;
import io.nats.client.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wasila.nats.annotation.ConnectionContext;
import org.wasila.nats.annotation.MessageContext;
import org.wasila.nats.annotation.QueueGroup;
import org.wasila.nats.annotation.Subject;
import org.wasila.nats.annotation.SubjectParam;
import org.wasila.nats.annotation.Subscribe;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Router implements AutoCloseable {

    private final Logger log = LoggerFactory.getLogger(Router.class);

    private final Connection connection;
    private final ObjectMapper jsonMapper;

    private final List<Subscription> subscriptions;
    private Thread shutdownHook;

    public Router() throws IOException, TimeoutException {
        this(ConnectionFactory.DEFAULT_URL);
    }

    public Router(String url) throws IOException, TimeoutException {
        this(new ConnectionFactory(url));
    }

    public Router(ConnectionFactory connectionFactory) throws IOException, TimeoutException {
        this.jsonMapper = new ObjectMapper();
        this.connection = connectionFactory.createConnection();
        this.subscriptions = new ArrayList<>();
        registerCleanupTask();
    }

    private interface TargetFactory {
        Object get() throws InstantiationException, IllegalAccessException;
    }

    private void addSubscription(String baseSubject, String queueGroup, final Method method, final TargetFactory targetFactory) {
        Map<Integer, String> subjectParams = new HashMap<>();

        final String[] subjectBaseSegments = baseSubject.split("\\.");

        String subject = IntStream.range(0, subjectBaseSegments.length)
                .mapToObj(index -> {
                    String s = subjectBaseSegments[index];
                    if (s.startsWith("{") && s.endsWith("}")) {
                        subjectParams.put(index, s.substring(1, s.length()-1));
                        return "*";
                    } else {
                        return s;
                    }
                })
                .collect(Collectors.joining("."));

        Subscription subscription = connection.subscribe(subject, queueGroup, msg -> {
            try {
                String[] subjectSegments = msg.getSubject().split("\\.");

                Map<String, String> paramsMapping = new HashMap<>();

                for (Map.Entry<Integer, String> subjectParam : subjectParams.entrySet()) {
                    String paramName = subjectParam.getValue();
                    String paramValue = subjectSegments[subjectParam.getKey()];
                    paramsMapping.put(paramName, paramValue);
                }

                Object[] params = Arrays.stream(method.getParameters()).map(param -> {
                            if (param.getAnnotations().length == 0) {
                                try {
                                    return jsonMapper.readValue(msg.getData(), param.getType());
                                } catch (IOException e) {
                                    throw new WrappingException(e);
                                }
                            } else if (param.getAnnotation(ConnectionContext.class) != null) {
                                return connection;
                            } else if (param.getAnnotation(MessageContext.class) != null) {
                                return msg;
                            } else {
                                SubjectParam subjectParam = param.getAnnotation(SubjectParam.class);
                                if (subjectParam != null) {
                                    return paramsMapping.get(subjectParam.value());
                                }
                            }
                            return null;
                        }
                ).toArray();

                Object reply = method.invoke(targetFactory.get(), params);

                if (reply != null) {
                    connection.publish(msg.getReplyTo(), jsonMapper.writeValueAsBytes(reply));
                }

            } catch (ReflectiveOperationException | IOException e) {
                log.error("Exception while invoking subscription handler", e);
            } catch (WrappingException e) {
                log.error("Exception while invoking subscription handler", e.getCause());
            }
        });
        subscriptions.add(subscription);
    }

    private void doRegister(Class<?> clazz, TargetFactory targetFactory) {
        List<Method> subscribeMethod = getMethodsAnnotatedWith(clazz, Subscribe.class);
        Subject classSubject = clazz.getAnnotation(Subject.class);
        String subjectPrefix = classSubject != null ? classSubject.value() : null;
        log.info("Registering " + subscribeMethod.size() + " methods");

        if (subscribeMethod.size() == 0) {
            throw new NoSubscriptionException("No registrable methods in resource");
        }

        for (Method method : subscribeMethod) {
            StringJoiner subjectJoiner = new StringJoiner(".");

            if (subjectPrefix != null) {
                subjectJoiner.add(subjectPrefix);
            }

            Subject methodSubject = method.getAnnotation(Subject.class);

            if (methodSubject != null) {
                subjectJoiner.add(methodSubject.value());
            }
            QueueGroup queueGroup = method.getAnnotation(QueueGroup.class);
            String queueGroupValue = queueGroup != null ? queueGroup.value() : null;

            addSubscription(subjectJoiner.toString(), queueGroupValue, method, targetFactory::get);
            log.info(" Method: " + method.getName() + ", Subject: " + subjectJoiner.toString());
        }
    }

    public void register(final Object object) {
        doRegister(object.getClass(), () -> object);
    }

    public void register(final Class<?> clazz) {
        doRegister(clazz, () -> clazz.newInstance());
    }

    @Override
    public void close() {
        unregisterAllAndClose();
    }

    private void registerCleanupTask() {
        if (this.shutdownHook == null) {
            this.shutdownHook = new Thread(() -> {
                log.debug("Starting cleanup task");
                unregisterAllAndClose();
            });
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }
    }

    private void unregisterAllAndClose() {
        try {
            if (shutdownHook != null) {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
                shutdownHook = null;
            }
            for (Subscription sub : subscriptions) {
                if (sub.isValid()) {
                    sub.unsubscribe();
                }
            }
            subscriptions.clear();
            connection.close();
        } catch (IOException e) {
            log.error("Unsubscribe failed", e);
        }

    }

    // code taken from: http://stackoverflow.com/questions/6593597/java-seek-a-method-with-specific-annotation-and-its-annotation-element
    private static List<Method> getMethodsAnnotatedWith(final Class<?> type, final Class<? extends Annotation> annotation) {
        final List<Method> methods = new ArrayList<Method>();
        Class<?> klass = type;
        while (klass != Object.class) {
            final List<Method> allMethods = new ArrayList<Method>(Arrays.asList(klass.getDeclaredMethods()));
            for (final Method method : allMethods) {
                if (method.isAnnotationPresent(annotation)) {
                    // Annotation annotInstance = method.getAnnotation(annotation);
                    // TODO process annotInstance
                    methods.add(method);
                }
            }
            // move to the upper class in the hierarchy in search for more methods
            klass = klass.getSuperclass();
        }
        return methods;
    }

}
