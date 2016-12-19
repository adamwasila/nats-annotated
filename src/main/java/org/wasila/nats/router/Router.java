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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.ConnectionFactory;
import io.nats.client.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wasila.nats.annotation.QueueGroup;
import org.wasila.nats.annotation.Subject;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class Router {

    private final Logger log = LoggerFactory.getLogger(Router.class);

    private final ConnectionFactory connectionFactory;
    private final Connection connection;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    private final List<Subscription> subscriptions;

    public Router() throws IOException, TimeoutException {
        connectionFactory = new ConnectionFactory(ConnectionFactory.DEFAULT_URL);
        connection = connectionFactory.createConnection();
        subscriptions = new ArrayList<>();
        registerCleanupTask();
    }

    private void addSubscription(String subject, String queueGroup, final Method method, final Object target) {
        Subscription subscription = connection.subscribe(subject, queueGroup, msg -> {
            try {
                method.invoke(target, jsonMapper.readValue(msg.getData(), method.getParameterTypes()[0]));
            } catch (ReflectiveOperationException e) {
                log.error("Exception while invoking subscription handler", e);
            } catch (JsonProcessingException e) {
                log.error("Exception while invoking subscription handler", e);
            } catch (IOException e) {
                log.error("Exception while invoking subscription handler", e);
            }
        });
        subscriptions.add(subscription);
    }

    public void register(Object object) {
        List<Method> subjectMethods = getMethodsAnnotatedWith(object.getClass(), Subject.class);
        Subject classSubject = object.getClass().getAnnotation(Subject.class);
        String subjectPrefix = classSubject != null ? classSubject.value() : "";
        log.info("Found " + subjectMethods.size() + " methods");
        for (Method method : subjectMethods) {
            String subject = subjectPrefix + method.getAnnotation(Subject.class).value();
            QueueGroup queueGroup = method.getAnnotation(QueueGroup.class);
            if (queueGroup != null) {
                addSubscription(subject, queueGroup.value(), method, object);
            } else {
                addSubscription(subject, null, method, object);
            }
        }
    }

    public void close() {
        unregisterAllAndClose();
    }

    private void registerCleanupTask() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                log.debug("Starting cleanup task");
                unregisterAllAndClose();
            }
        }));
    }

    private void unregisterAllAndClose() {
        try {
            for (Subscription sub : subscriptions) {
                sub.unsubscribe();
            }
        } catch (IOException e) {
            log.error("Unsubscribe failed", e);
        }

    }

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

    public static void main(String[] args) {

    }

}
