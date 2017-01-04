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

import io.nats.client.AsyncSubscription;
import io.nats.client.Connection;
import io.nats.client.ConnectionFactory;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import org.junit.Before;
import org.junit.Test;
import org.wasila.nats.annotation.ConnectionContext;
import org.wasila.nats.annotation.MessageContext;
import org.wasila.nats.annotation.Subscribe;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.*;

public class RouterInjectedParamsTest {

    private boolean resourceMethodCalled;

    private ConnectionFactory cf;
    private Connection cn;
    private AsyncSubscription sub;
    private Message msg;

    private MessageHandler currentHandler;

    @Before
    public void initializeTest() throws IOException, TimeoutException {
        cf = mock(ConnectionFactory.class);
        cn = mock(Connection.class);
        sub = mock(AsyncSubscription.class);
        msg = mock(Message.class);
        resourceMethodCalled = false;

        when(cf.createConnection()).thenReturn(cn);
        when(cn.subscribe(any(), any(), any())).thenAnswer(invocation -> {
            currentHandler = invocation.getArgumentAt(2, MessageHandler.class);
            return sub;
        });

        when(msg.getData()).thenReturn(("{\"data\":\"\"}").getBytes());
    }

    public static class DataDto {
        public String data;
    }

    public class TestResource {
        @Subscribe(subject = "test-subject")
        public DataDto helloWorld(@MessageContext Message message, @ConnectionContext Connection connection, DataDto dataDto) {
            resourceMethodCalled = true;
            assertThat(message, is(notNullValue()));
//            assertThat(connection, is(notNullValue()));
            return dataDto;
        }
    }

    @Test
    public void createRouterWithSimplestSubscription() throws IOException, TimeoutException, NoSuchMethodException {
        Router router = new Router(cf);

        TestResource testResource = new TestResource();

        router.register(testResource);

        assertThat(currentHandler, is(notNullValue()));

        currentHandler.onMessage(msg);

        router.close();

        verify(cn).subscribe(eq("test-subject"), isNull(String.class), isA(MessageHandler.class));
        verify(cn).close();
//        verifyNoMoreInteractions(cn);

        assertThat(resourceMethodCalled, is(true));
    }

}
