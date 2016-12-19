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
import org.wasila.nats.annotation.Subject;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Publisher<T> {

    private final ConnectionFactory cf;
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
        cf = new ConnectionFactory(url);
        handler = new PublisherInvocatorHandler();
    }

    private class PublisherInvocatorHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("Publisher invoke");
            Subject subject = method.getAnnotation(Subject.class);
            if (subject != null) {
                Connection cn = cf.createConnection();
                ObjectMapper objectMapper = new ObjectMapper();
                cn.publish(subject.value(), objectMapper.writeValueAsBytes(args[0]));
                cn.close();
            }
            return null;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}
