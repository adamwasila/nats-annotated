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

import io.nats.client.Message;
import org.wasila.nats.annotation.MessageContext;
import org.wasila.nats.annotation.Subject;
import org.wasila.nats.annotation.Subscribe;
import org.wasila.nats.examples.composite.dto.Hello;

@Subject("test")
public class CompositeResource {

    public CompositeResource() {
    }

    @Subscribe(subject="me")
    public void testTestMe(@MessageContext Message message, Hello hello) {
        System.out.println("[me] Received message: " + hello + " from " + message.getSubject());
    }

    @Subscribe(subject="abc")
    public void testTestAbc(@MessageContext Message message, Hello hello) {
        System.out.println("[abc] Received message: " + hello + " from " + message.getSubject());
    }

    @Subscribe(subject="me.me")
    public void testTestMeMe(@MessageContext Message message, Hello hello) {
        System.out.println("[abc] Received message: " + hello + " from " + message.getSubject());
    }

    @Subscribe
    public void testTest(@MessageContext Message message, Hello hello) {
        System.out.println("[abc] Received message: " + hello + " from " + message.getSubject());
    }

    @Subscribe(subject="*")
    public void testAll(@MessageContext Message message, Hello hello) {
        System.out.println("[*] Received message: " + hello + " from " + message.getSubject());
    }


}
