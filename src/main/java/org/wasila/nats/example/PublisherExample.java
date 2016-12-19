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
package org.wasila.nats.example;

import org.wasila.nats.annotation.Subject;
import org.wasila.nats.example.dto.Hello;
import org.wasila.nats.publisher.Publisher;

public class PublisherExample {

    public interface ExamplePublisher {

        @Subject("somesubject")
        void sendTest(Hello hello);

    }

    public static void main(String[] args) throws InterruptedException {

        ExamplePublisher publisher = Publisher.builder()
                .target(ExamplePublisher.class, "nats://localhost:4222");

        publisher.sendTest(new Hello("hello", false));

        Thread.sleep(1000);

        publisher.sendTest(new Hello("world", false));

        Thread.sleep(1000);

        publisher.sendTest(new Hello(".", true));

    }

}
