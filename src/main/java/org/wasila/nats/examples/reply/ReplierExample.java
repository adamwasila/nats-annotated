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
package org.wasila.nats.examples.reply;

import io.nats.client.Connection;
import io.nats.client.Message;
import org.wasila.nats.annotation.ConnectionContext;
import org.wasila.nats.annotation.MessageContext;
import org.wasila.nats.annotation.Subject;
import org.wasila.nats.examples.pubsub.Quitter;
import org.wasila.nats.examples.reply.dto.Reply;
import org.wasila.nats.examples.reply.dto.Request;
import org.wasila.nats.router.Router;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReplierExample implements Quitter {

    public static class ReplierResource {

        private final Quitter quitter;

        public ReplierResource(Quitter quitter) {
            this.quitter = quitter;
        }

        @Subject("foobar")
        public Reply handleReply(@ConnectionContext Connection connection, @MessageContext Message message, Request request) {
            System.out.println("Received request: " + request);
            quitter.setCanQuit(true);
            return new Reply("hi!");
        }

    }

    AtomicBoolean canQuit = new AtomicBoolean(false);

    public ReplierExample() {
        canQuit.set(false);
    }

    public void executeExample() throws InterruptedException, IOException, TimeoutException {
        Router router = new Router();

        try {
            router.register(new ReplierResource(this));

            while (!canQuit.get()) {
                Thread.sleep(1000);
            }
            System.out.println("Quitting...");
        } finally {
            router.close();
        }
    }

    public void setCanQuit(boolean canQuit) {
        this.canQuit.set(canQuit);
    }

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        new ReplierExample().executeExample();
    }

}
