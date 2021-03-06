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
package org.wasila.nats.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.ConnectionFactory;
import io.nats.client.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wasila.nats.annotation.Publish;
import org.wasila.nats.annotation.Subject;
import org.wasila.nats.internal.ConnectionCache;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Publisher<T> {

    private final Logger log = LoggerFactory.getLogger(Publisher.class);

    private final Connection connection;

    private final Class clazz;

    public static class Builder {

        public <T> T target(Class<T> clazz, Connection connection) throws IOException, TimeoutException {
            Publisher<T> publisher = new Publisher<>(connection, clazz);

            boolean hasTopSubject = clazz.getAnnotation(Subject.class) != null;

            for (Method method : clazz.getMethods()) {
                boolean hasPublish = method.getAnnotation(Publish.class) != null;
                if (!hasTopSubject && !hasPublish) {
                    throw new PublisherConfigurationException(String.format("Method %s::%s is not annotated properely to publish to nats",
                            clazz.getName(), method.getName()));
                }
            }

            return publisher.createProxyImplementation();
        }

        public <T> T target(Class<T> clazz, String url) throws IOException, TimeoutException {
            return this.target(clazz, ConnectionCache.getConnectionForUrl(url));
        }

        public <T> T target(Class<T> clazz) throws IOException, TimeoutException {
            return this.target(clazz, ConnectionFactory.DEFAULT_URL);
        }

    }

    @SuppressWarnings("unchecked")
    private T createProxyImplementation() {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),
                new Class[] {clazz}, new PublisherInvocatorHandler());
    }

    private Publisher(Connection connection, Class clazz) throws IOException, TimeoutException {
        this.clazz = clazz;
        this.connection = connection;
    }

    private class PublisherInvocatorHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Subject baseSubject = method.getDeclaringClass().getAnnotation(Subject.class);
            Publish subject = method.getAnnotation(Publish.class);

            Object value = null;

            String reply = (subject == null || subject.replyTo() == null || subject.replyTo().isEmpty()) ? null : subject.replyTo();
            int timeout = subject != null ? subject.timeout() : -1;
            TimeUnit timeoutUnit = subject != null ? subject.timeoutUnit() : TimeUnit.MILLISECONDS;

            if (baseSubject != null || subject != null) {
                Class<?> retType = method.getReturnType();
                String subjectValue = "";

                if (baseSubject != null) {
                    subjectValue = baseSubject.value();
                }

                if (subject != null) {
                    if (subjectValue != null && !subjectValue.isEmpty()) {
                        subjectValue += ".";
                    }
                    subjectValue += subject.subject();
                }

                ObjectMapper objectMapper = new ObjectMapper();
                if (retType != void.class) {
                    Message msg = connection.request(subjectValue, objectMapper.writeValueAsBytes(args[0]),
                            timeout, timeoutUnit);
                    value = objectMapper.readValue(msg.getData(), retType);
                } else {
                    connection.publish(subjectValue, reply, objectMapper.writeValueAsBytes(args[0]));
                }
            } else {
                log.warn("Could not invoke publish action: subject is null");
            }
            return value;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}
