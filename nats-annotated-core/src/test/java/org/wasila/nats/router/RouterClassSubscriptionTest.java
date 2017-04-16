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
import org.junit.Before;
import org.junit.Test;
import org.wasila.nats.annotation.Subject;
import org.wasila.nats.annotation.Subscribe;
import org.wasila.nats.router.base.TestBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.mockito.Mockito.*;

public class RouterClassSubscriptionTest extends TestBase {

    private static List<Object> resourceInstances;

    public static class TestResource {
        @Subscribe
        @Subject("test-subject")
        public void helloWorld() {
            resourceInstances.add(this);
        }
    }

    @Before
    public void init() {
        resourceInstances = new ArrayList<>();
    }

    @Test
    public void registerOnInstanceResource() throws IOException, TimeoutException {
        Router router = new Router(cn);
        router.register(new TestResource());
        currentHandler.onMessage(msg);
        currentHandler.onMessage(msg);
        router.close();
        verify(cn).subscribe(eq("test-subject"), isNull(String.class), isA(MessageHandler.class));

        assertThat(resourceInstances.size(), equalTo(2));
        assertThat(resourceInstances.get(0), sameInstance(resourceInstances.get(1)));
    }

    @Test
    public void registerOnClasResource() throws IOException, TimeoutException {
        Router router = new Router(cn);
        router.register(TestResource.class);
        currentHandler.onMessage(msg);
        currentHandler.onMessage(msg);
        router.close();
        verify(cn).subscribe(eq("test-subject"), isNull(String.class), isA(MessageHandler.class));

        assertThat(resourceInstances.size(), equalTo(2));
        assertThat(resourceInstances.get(0), not(sameInstance(resourceInstances.get(1))));
    }

}
