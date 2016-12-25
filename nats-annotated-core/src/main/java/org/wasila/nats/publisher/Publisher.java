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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Publisher<T> {

    private final Logger log = LoggerFactory.getLogger(Publisher.class);

    private final static int REPLY_TIMEOUT = 5000;

    private final ConnectionFactory connectionFactory;
    private final PublisherInvocatorHandler handler;
    private final Class clazz;

    public static class Builder {
        public <T> T target(Class<T> clazz, String url) {
            Publisher<T> publisher = new Publisher<T>(clazz, url);
            return publisher.createProxyImplementation();
        }
    }

    private T createProxyImplementation() {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),
                new Class[] {clazz}, new PublisherInvocatorHandler());
    }

    public Publisher(Class clazz, String url) {
        this.clazz = clazz;
        connectionFactory = new ConnectionFactory(url);
        handler = new PublisherInvocatorHandler();
    }

    private class PublisherInvocatorHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Publish subject = method.getAnnotation(Publish.class);

            String reply = (subject.replyTo() == null || subject.replyTo().isEmpty()) ? null : subject.replyTo();

            Class<?> retType = method.getReturnType();

            Object value = null;

            if (subject != null) {
                String subjectValue = subject.subject();
                Connection cn = connectionFactory.createConnection();
                ObjectMapper objectMapper = new ObjectMapper();
                if (retType != void.class) {
                    Message msg = cn.request(subjectValue, objectMapper.writeValueAsBytes(args[0]), REPLY_TIMEOUT);
                    value = objectMapper.readValue(msg.getData(), retType);
                } else {
                    cn.publish(subjectValue, reply, objectMapper.writeValueAsBytes(args[0]));
                }
                cn.close();
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