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
package org.wasila.nats.example.dto;

public class Hello {

    public String value;

    public boolean lastMessage;

    public Hello() {
    }

    public Hello(String value, boolean lastMessage) {
        this.value = value;
        this.lastMessage = lastMessage;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(boolean lastMessage) {
        this.lastMessage = lastMessage;
    }

    @Override
    public String toString() {
        return "Hello{" +
                "value='" + value + '\'' +
                ", lastMessage=" + lastMessage +
                '}';
    }
}
