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
import io.nats.client.MessageHandler;
import org.junit.Test;
import org.wasila.nats.annotation.ConnectionContext;
import org.wasila.nats.annotation.MessageContext;
import org.wasila.nats.annotation.Subscribe;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class RouterInjectedParamsAndResponseTest extends RouterBaseTest {

    private static final String SUBJECT = "test-subject";

    private static final String RESPONSE_HANDLER_ID = "TestResource::helloWorld";

    public class TestWithMessageAndConnectionResource {
        @Subscribe(subject = SUBJECT)
        public void notImportantName(@MessageContext Message message, @ConnectionContext Connection connection, DataDto dataDto) {
            addResponse(RESPONSE_HANDLER_ID, message, connection);
        }
    }

    @Test
    public void createRouterForOneSubscriptionWithInjectsAndResponse() throws IOException, TimeoutException, NoSuchMethodException {
        Router router = prepareRouter(TestWithMessageAndConnectionResource.class);

        currentHandler.onMessage(msg);

        router.close();

        verify(cn).subscribe(eq(SUBJECT), isNull(String.class), isA(MessageHandler.class));
        verify(cn).close();
        verifyNoMoreInteractions(cn);

        validateResponse(RESPONSE_HANDLER_ID, 1, cn, msg);
    }

    public class TestWithMessageResource {
        @Subscribe(subject = SUBJECT)
        public void itIsReallyIrrelevantName(@MessageContext Message message, DataDto dataDto) {
            addResponse(RESPONSE_HANDLER_ID, message, null);
        }
    }

    @Test
    public void createRouterForOneSubscriptionWithMessageAndResponse() throws IOException, TimeoutException, NoSuchMethodException {
        Router router = prepareRouter(TestWithMessageResource.class);

        currentHandler.onMessage(msg);

        router.close();

        verify(cn).subscribe(eq(SUBJECT), isNull(String.class), isA(MessageHandler.class));
        verify(cn).close();
        verifyNoMoreInteractions(cn);

        validateResponse(RESPONSE_HANDLER_ID, 1, null, msg);
    }

    public class TestWithConnectionResource {
        @Subscribe(subject = SUBJECT)
        public void youShouldUseSomethingDescriptiveHere(@ConnectionContext Connection connection, DataDto dataDto) {
            addResponse(RESPONSE_HANDLER_ID, null, connection);
        }
    }

    @Test
    public void createRouterForOneSubscriptionWithConnectionAndResponse() throws IOException, TimeoutException, NoSuchMethodException {
        Router router = prepareRouter(TestWithConnectionResource.class);

        currentHandler.onMessage(msg);

        router.close();

        verify(cn).subscribe(eq(SUBJECT), isNull(String.class), isA(MessageHandler.class));
        verify(cn).close();
        verifyNoMoreInteractions(cn);

        validateResponse(RESPONSE_HANDLER_ID, 1, cn, null);

    }

}
