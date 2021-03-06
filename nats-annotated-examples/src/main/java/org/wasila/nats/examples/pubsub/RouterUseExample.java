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
package org.wasila.nats.examples.pubsub;

import org.wasila.nats.examples.util.KeyReader;
import org.wasila.nats.router.Router;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class RouterUseExample {

    AtomicBoolean canQuit = new AtomicBoolean(false);

    public RouterUseExample() {
        canQuit.set(false);
    }

    public void executeExample() throws InterruptedException, IOException, TimeoutException {
        Router router = new Router();

        try {
            router.register(new ExampleResource());

            KeyReader.waitForEnter();

        } finally {
            router.close();
        }
    }

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        new RouterUseExample().executeExample();
    }

}
