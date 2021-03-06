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
import org.wasila.nats.annotation.QueueGroup;
import org.wasila.nats.annotation.Subject;
import org.wasila.nats.annotation.Subscribe;
import org.wasila.nats.router.base.TestBase;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.*;

public class RouterSubscriptionTest extends TestBase {

    private static final String RESPONSE_HANDLER_ID = "TestResource::helloWorld";

    public class TestResource {
        @Subscribe
        @Subject("test-subject")
        public void helloWorld() {
            addResponse(RESPONSE_HANDLER_ID, null, null);
        }
    }

    @Test
    public void createRouterWithSimplestSubscription() throws IOException, TimeoutException {
        Router router = new Router(cn);
        router.register(new TestResource());
        router.register(TestResource.class);
        router.close();

        verify(cn, times(2)).subscribe(eq("test-subject"), isNull(String.class), isA(MessageHandler.class));
    }

    public class TestResourceSameSubject {
        @Subscribe
        @Subject("test-subject")
        public void helloWorld() {
            addResponse(RESPONSE_HANDLER_ID, null, null);
        }

        @Subscribe
        @Subject("test-subject")
        public void helloWorld2() {
            addResponse(RESPONSE_HANDLER_ID, null, null);
        }
    }

    @Test
    public void createRouterWithMultipleSubscription() throws IOException, TimeoutException {
        Router router = new Router(cn);
        router.register(new TestResourceSameSubject());
        router.register(TestResourceSameSubject.class);
        router.close();

        verify(cn, times(4)).subscribe(eq("test-subject"), isNull(String.class), isA(MessageHandler.class));
    }

    @Subject("test-base-subject")
    public class TestResourceNoSubject {
        @Subscribe
        public void helloWorld() {
            addResponse(RESPONSE_HANDLER_ID, null, null);
        }
    }

    @Test
    public void createRouterWithSubscriptionWithNoSubject() throws IOException, TimeoutException {
        Router router = new Router(cn);
        router.register(new TestResourceNoSubject());
        router.register(TestResourceNoSubject.class);
        router.close();

        verify(cn, times(2)).subscribe(eq("test-base-subject"), isNull(String.class), isA(MessageHandler.class));
    }

    @Subject("base-subject")
    public class TestCompositeSubResource {
        @Subscribe
        @Subject("test-subject")
        public void helloWorld() {
            addResponse(RESPONSE_HANDLER_ID, null, null);
        }
    }

    @Test
    public void createRouterWithCompositeSubscription() throws IOException, TimeoutException {
        Router router = new Router(cn);
        router.register(new TestCompositeSubResource());
        router.register(TestCompositeSubResource.class);
        router.close();

        verify(cn, times(2)).subscribe(eq("base-subject.test-subject"), isNull(String.class), isA(MessageHandler.class));
    }

    public class TestQueueGroupResource {
        @Subscribe
        @Subject("test-subject")
        @QueueGroup("group1")
        public void helloWorld() {
            addResponse(RESPONSE_HANDLER_ID, null, null);
        }
    }

    @Test
    public void createRouterWithQueueGroupSubscription() throws IOException, TimeoutException {
        Router router = new Router(cn);
        router.register(new TestQueueGroupResource());
        router.register(TestQueueGroupResource.class);
        router.close();

        verify(cn, times(2)).subscribe(eq("test-subject"), eq("group1"), isA(MessageHandler.class));
    }

    public class TestEmptyResource {
        public void helloWorld() {

        }
    }


    @Test(expected=NoSubscriptionException.class)
    public void registerResourceWithNoResourceMethods() throws IOException, TimeoutException {
        Router router = new Router(cn);
        try {
            router.register(new TestEmptyResource());
        } finally {
            router.close();
        }
    }

    @Test(expected=NoSubscriptionException.class)
    public void registerClassResourceWithNoResourceMethods() throws IOException, TimeoutException {
        Router router = new Router(cn);
        try {
            router.register(TestEmptyResource.class);
        } finally {
            router.close();
        }
    }

}
