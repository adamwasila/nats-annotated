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
package org.wasila.nats.publisher;

import io.nats.client.Connection;
import io.nats.client.ConnectionFactory;
import io.nats.client.Message;
import org.junit.Test;
import org.mockito.Matchers;
import org.wasila.nats.annotation.Publish;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.*;

public class PublisherTest {

    public static class TestDto {
        public String testProperty;
    }

    public interface PublisherInterface {
        @Publish(subject = "my-subject")
        void publishMe(TestDto test);
    }

    public interface PublisherInterface2 {
        @Publish(subject = "my-subject")
        TestDto publishMe(TestDto test);
    }

    @Test
    public void createSimplePublisherFireAndForget() throws IOException, TimeoutException {
        ConnectionFactory cf = mock(ConnectionFactory.class);
        Connection cn = mock(Connection.class);

        when(cf.createConnection()).thenReturn(cn);

        PublisherInterface publisher = Publisher.builder().target(PublisherInterface.class, cf);

        publisher.publishMe(new TestDto());

        verify(cn).publish(eq("my-subject"), isNull(String.class), any(byte[].class));
        verify(cn).close();
        verifyNoMoreInteractions(cn);
    }

    @Test
    public void createSimplePublisherReturningValue() throws IOException, TimeoutException {
        ConnectionFactory cf = mock(ConnectionFactory.class);
        Connection cn = mock(Connection.class);
        Message msg = mock(Message.class);

        when(cf.createConnection()).thenReturn(cn);
        when(msg.getData()).thenReturn("{\"testProperty\":\"\"}".getBytes());

        PublisherInterface2 publisher = Publisher.builder().target(PublisherInterface2.class, cf);

        when(cn.request(eq("my-subject"), Matchers.<byte[]>any(), anyInt(), any(TimeUnit.class))).thenReturn(msg);

        TestDto ret = publisher.publishMe(new TestDto());

        verify(cn).request(eq("my-subject"), any(byte[].class), anyInt(), any(TimeUnit.class));
        verify(cn).close();
        verifyNoMoreInteractions(cn);
    }

}
