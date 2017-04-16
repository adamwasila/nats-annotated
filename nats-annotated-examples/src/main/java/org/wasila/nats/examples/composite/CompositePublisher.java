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
package org.wasila.nats.examples.composite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wasila.nats.annotation.Publish;
import org.wasila.nats.examples.composite.dto.Hello;
import org.wasila.nats.publisher.Publisher;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class CompositePublisher {

    public interface ExamplePublisher {

        @Publish(subject = "test.me")
        void sendTest(Hello hello);

        @Publish(subject = "test.abc")
        void sendTest2(Hello hello);

    }

    private static Logger log = LoggerFactory.getLogger(CompositePublisher.class);

    public static void main(String[] args) throws InterruptedException, IOException, TimeoutException {

        ExamplePublisher publisher = Publisher.builder()
                .target(ExamplePublisher.class, "nats://localhost:4222");

        Hello hello = new Hello("hello", false);

        publisher.sendTest(hello);
        publisher.sendTest2(hello);

    }

}
