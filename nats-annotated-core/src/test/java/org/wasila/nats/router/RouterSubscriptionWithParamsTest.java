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
            addResponseParamsOnly("test-subject.{param}", param);
        }
    }

    @Test
    public void routerWithParametrizedSubscription() throws IOException, TimeoutException {
        executeTest("test-subject.paramValue", new TestResource());

        verifyHandlerAndParams("test-subject.{param}", "paramValue");

        verifyThatGivenSubjectWasSubscribed("test-subject.*");
    }

    public class TestTwiceResource {
        @Subscribe
        @Subject("test-subject.{param1}.and.{param2}")
        public void testSubject(@SubjectParam("param1") String param1, @SubjectParam("param2") String param2) {
            addResponseParamsOnly("test-subject.{param1}.and.{param2}", param1, param2);
        }
    }

    @Test
    public void routerWithParametrizedSubscriptionTwice() throws IOException, TimeoutException {
        executeTest("test-subject.paramValue1.and.paramValue2", new TestTwiceResource());

        verifyHandlerAndParams("test-subject.{param1}.and.{param2}", "paramValue1", "paramValue2");

        verifyThatGivenSubjectWasSubscribed("test-subject.*.and.*");
    }

    public class TestTwiceSwitchedParamsResource {
        @Subscribe
        @Subject("test-subject.{param1}.and.{param2}")
        public void testSubjectSwitchedParams(@SubjectParam("param2") String param2, @SubjectParam("param1") String param1) {
            addResponse("test-subject.{param1}.and.{param2}", null, null, param1, param2);
        }
    }

    @Test
    public void routerWithParametrizedSubscriptionTwiceSwitchedParams() throws IOException, TimeoutException {
        executeTest("test-subject.paramValue1.and.paramValue2", new TestTwiceSwitchedParamsResource());

        verifyHandlerAndParams("test-subject.{param1}.and.{param2}", "paramValue1", "paramValue2");

        verifyThatGivenSubjectWasSubscribed("test-subject.*.and.*");
    }

    public class TestResourceWithOnlyOneParamUsed {
        @Subscribe
        @Subject("test-subject.{param1}.and.{param2}")
        public void testSubjectSwitchedOnlyFirstParam(@SubjectParam("param1") String param1) {
            addResponseParamsOnly("testSubjectSwitchedOnlyFirstParam", param1);
        }
    }

    @Test
    public void routerWithParametrizedSubscriptionNotAllArgsUsed() throws IOException, TimeoutException {
        executeTest("test-subject.paramValue1.and.paramValue2", new TestResourceWithOnlyOneParamUsed());

        verifyHandlerAndParams("testSubjectSwitchedOnlyFirstParam", "paramValue1");

        verifyThatGivenSubjectWasSubscribed("test-subject.*.and.*");
    }

    @Subject("main-subject.{param0}.other-subject")
    public class TestResourceWithParametrizedComposedSubject {
        @Subscribe
        @Subject("test-subject.{param1}")
        public void testSubject(@SubjectParam("param0") String param0, @SubjectParam("param1") String param1) {
            addResponseParamsOnly("testSubject", param0, param1);
        }

    }

    @Test
    public void routerWithComposedParametrizedSubject() throws IOException, TimeoutException {
        executeTest("main-subject.param0-value.other-subject.test-subject.param1-value", new TestResourceWithParametrizedComposedSubject());

        verifyHandlerAndParams("testSubject", "param0-value", "param1-value");

        verifyThatGivenSubjectWasSubscribed("main-subject.*.other-subject.test-subject.*");
    }

    protected void executeTest(String messageSubject, Object resourceInstance) throws IOException, TimeoutException {
        when(msg.getSubject()).thenReturn(messageSubject);

        Router router = new Router(cf);
        router.register(resourceInstance);

        currentHandler.onMessage(msg);

        router.close();
    }

    protected void verifyHandlerAndParams(String handlerMessage, Object... params) {
        validateResponse2(handlerMessage, 1, params);
    }

    protected void verifyThatGivenSubjectWasSubscribed(String subject) {
        verify(cn).subscribe(eq(subject), isNull(String.class), isA(MessageHandler.class));
    }

}
