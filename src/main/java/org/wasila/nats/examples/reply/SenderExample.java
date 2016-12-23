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
package org.wasila.nats.examples.reply;

import org.wasila.nats.annotation.Publish;
import org.wasila.nats.annotation.Subscribe;
import org.wasila.nats.examples.reply.dto.Reply;
import org.wasila.nats.examples.reply.dto.Request;
import org.wasila.nats.publisher.Publisher;
import org.wasila.nats.router.Router;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class SenderExample {

    public interface ExamplePublisher {

        @Publish(subject="foobar", replyTo="foobarreply")
        void sendRequest(Request request);

    }

    public static class SenderResource {
        @Subscribe(subject="foobarreply")
        public void handleReply(Reply reply) {
            System.out.println("Received reply: " + reply);
        }

    }

    AtomicBoolean canQuit = new AtomicBoolean(false);

    private void executePublish() throws IOException, TimeoutException, InterruptedException {
        Router router = new Router();

        try {
            router.register(new SenderResource());

            ExamplePublisher publisher = Publisher.builder()
                    .target(ExamplePublisher.class, "nats://localhost:4222");

            publisher.sendRequest(new Request("hello there!"));

        } finally {
            router.close();
        }
    }

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        new SenderExample().executePublish();
    }

}
