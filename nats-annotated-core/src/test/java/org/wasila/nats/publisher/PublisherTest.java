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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.wasila.nats.annotation.Publish;
import org.wasila.nats.annotation.Subject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.*;

public class PublisherTest {

    private static byte[] MESSAGE_BODY_JSON = ("{" +
            "\"testProperty\" : \"propertyValue\"" +
            "}").getBytes();

    public static class TestDto {
        public String testProperty;
    }

    public static class ResponseDto {
        public String testProperty;
    }

    private ConnectionFactory cf;
    private Connection cn;
    private Message msg;

    @Before
    public void prepare() throws IOException, TimeoutException {
        cf = mock(ConnectionFactory.class);
        cn = mock(Connection.class);
        msg = mock(Message.class);

        when(cf.createConnection()).thenReturn(cn);
        when(msg.getData()).thenReturn(MESSAGE_BODY_JSON);
   }

    public interface PublisherInterface {
        @Publish(subject = "my-subject")
        void publishMe(TestDto test);
    }

    @Test
    public void createSimplePublisherFireAndForgetStyle() throws IOException, TimeoutException {

        PublisherInterface publisher = Publisher.builder().target(PublisherInterface.class, cf);

        publisher.publishMe(new TestDto());

        verify(cn).publish(eq("my-subject"), isNull(String.class), any(byte[].class));
        verify(cn).close();
        verifyNoMoreInteractions(cn);
    }

    public interface PublisherInterfaceWithResponse {
        @Publish(subject = "my-subject")
        ResponseDto publishMe(TestDto test);
    }

    @Test
    public void createSimplePublisherReturningValue() throws IOException, TimeoutException {

        PublisherInterfaceWithResponse publisher = Publisher.builder().target(PublisherInterfaceWithResponse.class, cf);

        when(cn.request(eq("my-subject"), Matchers.<byte[]>any(), anyInt(), any(TimeUnit.class))).thenReturn(msg);

        ResponseDto response = publisher.publishMe(new TestDto());

        verify(cn).request(eq("my-subject"), any(byte[].class), anyInt(), any(TimeUnit.class));
        verify(cn).close();
        verifyNoMoreInteractions(cn);

        assertThat(response, is(notNullValue()));
        assertThat(response.testProperty, equalTo("propertyValue"));
    }

    @Subject("base-subject")
    public interface PublisherInterfaceWithComposedSubject {
        @Publish(subject = "my-subject")
        void publishMe(TestDto test);
    }

    @Test
    public void createSimplePublisherWithComposedSubject() throws IOException, TimeoutException {

        PublisherInterfaceWithComposedSubject publisher = Publisher.builder().target(PublisherInterfaceWithComposedSubject.class, cf);

        publisher.publishMe(new TestDto());

        verify(cn).publish(eq("base-subject.my-subject"), isNull(String.class), any(byte[].class));
        verify(cn).close();
        verifyNoMoreInteractions(cn);
    }

}
