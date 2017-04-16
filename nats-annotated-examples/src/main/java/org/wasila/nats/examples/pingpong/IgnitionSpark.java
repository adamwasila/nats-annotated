/**
 * (C) Copyright 2016 Adam Wasila.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wasila.nats.examples.pingpong;

import org.wasila.nats.annotation.Publish;
import org.wasila.nats.examples.pingpong.dto.Ping;
import org.wasila.nats.publisher.Publisher;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class IgnitionSpark {

    private final PingPublisher pongPublisher;

    public interface PingPublisher {
        @Publish(subject = "ping")
        void sendRequest(Ping ping);
    }

    public void initiate() {
        pongPublisher.sendRequest(new Ping(new Random().nextInt(1000), LocalDateTime.now().toString()));
    }

    public IgnitionSpark() throws IOException, TimeoutException {
        pongPublisher = Publisher.builder().target(PingPublisher.class, "nats://127.0.0.1:4222");
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        new IgnitionSpark().initiate();
    }

}
