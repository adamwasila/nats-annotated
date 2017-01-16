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
import org.wasila.nats.annotation.Subject;
import org.wasila.nats.annotation.SubjectParam;
import org.wasila.nats.annotation.Subscribe;
import org.wasila.nats.router.base.TestBase;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.*;

public class RouterSubscriptionWithParamsTest extends TestBase {

    public class TestResource {
        @Subscribe
        @Subject("test-subject.{param}")
        public void testSubject(@SubjectParam("param") String param) {
            addResponse("test-subject.{param}", null, null, param);
        }
    }

    @Test
    public void createRouterWithParametrizedSubscription() throws IOException, TimeoutException {
        Router router = new Router(cf);
        router.register(new TestResource());
        router.close();

        verify(cn).subscribe(eq("test-subject.*"), isNull(String.class), isA(MessageHandler.class));
    }

    @Test
    public void callRouterWithParametrizedSubscription() throws IOException, TimeoutException {
        when(msg.getSubject()).thenReturn("test-subject.paramValue");

        Router router = new Router(cf);
        router.register(new TestResource());
        currentHandler.onMessage(msg);
        router.close();

        validateResponse("test-subject.{param}", 1, null, null, new Object[] {"paramValue"});
    }

}
