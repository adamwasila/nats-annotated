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
package org.wasila.nats.router;

import io.nats.client.Connection;
import io.nats.client.Message;
import org.junit.Test;
import org.wasila.nats.annotation.ConnectionContext;
import org.wasila.nats.annotation.MessageContext;
import org.wasila.nats.annotation.Subject;
import org.wasila.nats.annotation.Subscribe;
import org.wasila.nats.router.base.TestBase;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RouterInjectedParamsTest extends TestBase {

    private static final String SUBJECT = "test-subject";

    private static final String RESPONSE_HANDLER_ID = "TestInjectsResource::helloWorld";
    private static final String RESPONSE_HANDLER_REVERSED_ID = "TestInjectsReversedResource::helloWorld";

    public class TestInjectsResource {

        @Subscribe
        @Subject(SUBJECT)
        public DataDto nameIsIrrelevant(@MessageContext Message message, @ConnectionContext Connection connection, DataDto dataDto) {
            addResponse(RESPONSE_HANDLER_ID, message, connection);
            return dataDto;
        }

    }

    @Test
    public void handleResourceWithInjectedContext() throws IOException, TimeoutException, NoSuchMethodException {
        Router router = prepareRouter(TestInjectsResource.class);
        currentHandler.onMessage(msg);
        router.close();

        validateResponse(RESPONSE_HANDLER_ID, 1, cn, msg);
    }

    public class TestInjectsReversedResource {

        @Subscribe
        @Subject(SUBJECT)
        public DataDto nameIsIrrelevant(@ConnectionContext Connection connection, @MessageContext Message message, DataDto dataDto) {
            addResponse(RESPONSE_HANDLER_REVERSED_ID, message, connection);
            return dataDto;
        }

    }

    @Test
    public void handleResourceWithInjectedContextReversed() throws IOException, TimeoutException, NoSuchMethodException {
        Router router = prepareRouter(TestInjectsReversedResource.class);
        currentHandler.onMessage(msg);
        router.close();

        validateResponse(RESPONSE_HANDLER_REVERSED_ID, 1, cn, msg);
    }

}
