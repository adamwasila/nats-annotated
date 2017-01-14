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

import io.nats.client.MessageHandler;
import org.junit.Test;
import org.wasila.nats.annotation.Subscribe;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.*;

public class RouterUnsubscribeTest extends RouterBaseTest {

    private static final String RESPONSE_HANDLER_ID = "TestResource::helloWorld";

    public class TestResource {
        @Subscribe(subject = "test-subject")
        public void helloWorld() {
            addResponse(RESPONSE_HANDLER_ID, null, null);
        }
    }

    @Test
    public void testUnsubscribeWithOneResourceMethod() throws IOException, TimeoutException, NoSubscriptionException {
        Router router = new Router(cf);
        router.register(new TestResource());

        router.close();

        verify(cn).subscribe(eq("test-subject"), isNull(String.class), isA(MessageHandler.class));
        verify(cn).close();
        verifyNoMoreInteractions(cn);

        verify(sub).isValid();
        verify(sub).unsubscribe();
        verifyNoMoreInteractions(sub);

        validateResponse(RESPONSE_HANDLER_ID, 0, null, null);
    }

    @Test
    public void testUnsubscribeThenSubscribeAgain() throws IOException, TimeoutException, NoSubscriptionException {
        Router router = new Router(cf);
        router.register(new TestResource());
        router.close();
        router.register(new TestResource());
        router.close();

        verify(cn, times(2)).subscribe(eq("test-subject"), isNull(String.class), isA(MessageHandler.class));
        verify(cn, times(2)).close();
        verifyNoMoreInteractions(cn);

        verify(sub, times(2)).isValid();
        verify(sub, times(2)).unsubscribe();
        verifyNoMoreInteractions(sub);

        validateResponse(RESPONSE_HANDLER_ID, 0, null, null);
    }


}