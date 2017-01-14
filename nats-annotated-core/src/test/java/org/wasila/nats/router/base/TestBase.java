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
package org.wasila.nats.router.base;

import io.nats.client.AsyncSubscription;
import io.nats.client.Connection;
import io.nats.client.ConnectionFactory;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import org.junit.Before;
import org.wasila.nats.router.Router;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestBase {

    protected ConnectionFactory cf;
    protected Connection cn;
    protected AsyncSubscription sub;
    protected Message msg;

    protected MessageHandler currentHandler;

    protected class HandlerResponse {
        protected int count;
        protected Message lastMessage;
        protected Connection lastConnection;
    }

    protected Map<String,HandlerResponse> responses;

    protected void addResponse(String name, Message message, Connection connection) {
        HandlerResponse handlerResponse = responses.computeIfAbsent(name, s -> new HandlerResponse() );
        handlerResponse.count++;
        handlerResponse.lastMessage = message;
        handlerResponse.lastConnection = connection;
    }

    protected HandlerResponse getResponse(String name) {
        return responses.computeIfAbsent(name, s -> new HandlerResponse() );
    }

    @Before
    public void initializeTest() throws IOException, TimeoutException {
        cf = mock(ConnectionFactory.class);
        cn = mock(Connection.class);
        sub = mock(AsyncSubscription.class);
        msg = mock(Message.class);

        responses = new HashMap<>();

        when(cf.createConnection()).thenReturn(cn);
        when(cn.subscribe(any(), any(), any())).thenAnswer(invocation -> {
            currentHandler = invocation.getArgumentAt(2, MessageHandler.class);
            return sub;
        });

        when(sub.isValid()).thenReturn(true);

        when(msg.getData()).thenReturn(("{\"data\":\"\"}").getBytes());
    }

    public static class DataDto {
        public String data;
    }

    public static String sampleDataDtoJson = "{" +
            "\"" +
            "data\" : \"value\"" +
            "}";

    public static class Data2Dto {
        public String data2;
    }

    public static String sampleData2DtoJson = "{" +
            "\"" +
            "data2\" : \"value\"" +
            "}";

    public static class ResponseDto {
        public String responseCode;
        public String response;
    }

    public static String sampleResponseJson = "{" +
            "\"" +
            "response\" : \"value\"" +
            "}";

    protected Router prepareRouter(Class resourceClazz) {
        try {
            Router router = new Router(cf);
            Object resource = resourceClazz.getConstructor(getClass()).newInstance(this);
            router.register(resource);

            assertThat(currentHandler, is(notNullValue()));

            return router;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void validateResponse(String handlerId, int count, Connection cn, Message msg) {
        HandlerResponse response = getResponse(handlerId);
        assertThat(response.count, equalTo(count));
        assertThat(response.lastConnection, equalTo(cn));
        assertThat(response.lastMessage, equalTo(msg));
    }

}
