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
import org.wasila.nats.annotation.QueueGroup;
import org.wasila.nats.annotation.Subject;
import org.wasila.nats.annotation.Subscribe;
import org.wasila.nats.examples.pingpong.dto.Ping;
import org.wasila.nats.examples.pingpong.dto.Pong;
import org.wasila.nats.examples.util.KeyReader;
import org.wasila.nats.publisher.Publisher;
import org.wasila.nats.router.Router;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;

public class Pinger {

    public interface PingPublisher {
        @Publish(subject = "ping")
        void sendRequest(Ping ping);
    }

    public static class PongResource {
        private PingPublisher pongPublisher;

        public PongResource() throws IOException, TimeoutException {
            pongPublisher = Publisher.builder().target(PingPublisher.class, "nats://127.0.0.1:4222");
        }

        @Subscribe
        @Subject("pong")
        @QueueGroup("silna-grupa-pod-wezwaniem")
        public void getPong(Pong pong) {
            System.out.println("Pong received: " + pong);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            pongPublisher.sendRequest(new Ping(pong.getId(), LocalDateTime.now().toString()));
        }

    }

    private void executePublish() throws IOException, TimeoutException, InterruptedException {
        Router router = new Router();

        try {
            PongResource pongResource = new PongResource();
            router.register(pongResource);
            KeyReader.waitForEnter();
        } finally {
            router.close();
        }
    }

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        new Pinger().executePublish();
    }

}
