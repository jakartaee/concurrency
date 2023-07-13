/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package ee.jakarta.tck.concurrent.api.ManagedScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.common.tasks.CallableTask;
import ee.jakarta.tck.concurrent.common.tasks.CommonTriggers;
import ee.jakarta.tck.concurrent.common.tasks.RunnableTask;
import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common.PACKAGE;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;
import ee.jakarta.tck.concurrent.framework.junit.extensions.Wait;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;

@Web
@Common({ PACKAGE.TASKS })
public class ManagedScheduledExecutorServiceTests {

    // TODO deploy as EJB and JSP artifacts
    @Deployment(name = "ManagedScheduledExecutorServiceTests")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsWebInfResource(ManagedScheduledExecutorServiceTests.class.getPackage(), "web.xml", "web.xml");
    }

    private static final String CALLABLETESTTASK1_RUN_RESULT = "CallableTestTask1";

    private static final String TEST_JNDI_EVN_ENTRY_VALUE = "hello";

    private static final String TEST_JNDI_EVN_ENTRY_JNDI_NAME = "java:comp/env/ManagedScheduledExecutorService_test_string";

    private static final String TEST_CLASSLOADER_CLASS_NAME = ManagedScheduledExecutorServiceTests.class
            .getCanonicalName();

    @Resource(lookup = TestConstants.DefaultManagedScheduledExecutorService)
    public ManagedScheduledExecutorService scheduledExecutor;

    /*
     * @testName: normalScheduleProcess1Test
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:30;CONCURRENCY:SPEC:42;
     * CONCURRENCY:SPEC:42.2;CONCURRENCY:SPEC:43;CONCURRENCY:SPEC:43.1;
     * CONCURRENCY:SPEC:49;CONCURRENCY:SPEC:51; CONCURRENCY:SPEC:54;
     * 
     * @test_Strategy: Creates and executes a task based on a Trigger. The Trigger
     * determines when the task should run and how often.
     */
    @Test
    public void normalScheduleProcess1Test() throws Exception {
        ScheduledFuture<?> result = scheduledExecutor.schedule(
                new RunnableTask(TEST_JNDI_EVN_ENTRY_JNDI_NAME, TEST_JNDI_EVN_ENTRY_VALUE, TEST_CLASSLOADER_CLASS_NAME),
                new CommonTriggers.OnceTrigger());
        Wait.waitForTaskComplete(result);
        assertNull(result.get());
    }

    /*
     * @testName: nullCommandScheduleProcessTest
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:32
     * 
     * @test_Strategy: if command is null.
     */
    @Test
    public void nullCommandScheduleProcessTest() {
        Runnable command = null;

        assertThrows(NullPointerException.class, () -> {
            scheduledExecutor.schedule(command, new CommonTriggers.OnceTrigger());
        });
    }

    /*
     * @testName: normalScheduleProcess2Test
     * 
     * @assertion_ids:
     * CONCURRENCY:JAVADOC:33;CONCURRENCY:SPEC:43;CONCURRENCY:SPEC:43.2;
     * CONCURRENCY:SPEC:54;CONCURRENCY:SPEC:52;
     *
     * 
     * @test_Strategy: Creates and executes a task based on a Trigger. The Trigger
     * determines when the task should run and how often.
     */
    @Test
    public void normalScheduleProcess2Test() throws Exception {
        ScheduledFuture<?> result = scheduledExecutor
                .schedule(
                        (Callable<?>) new CallableTask<String>(TEST_JNDI_EVN_ENTRY_JNDI_NAME, TEST_JNDI_EVN_ENTRY_VALUE,
                                TEST_CLASSLOADER_CLASS_NAME, CALLABLETESTTASK1_RUN_RESULT),
                        new CommonTriggers.OnceTrigger());
        Wait.waitForTaskComplete(result);

        assertEquals(CALLABLETESTTASK1_RUN_RESULT, result.get());

    }

    /*
     * @testName: nullCallableScheduleProcessTest
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:35
     * 
     * @test_Strategy: if callable is null.
     */
    @Test
    public void nullCallableScheduleProcessTest() {
        Callable<?> callable = null;

        assertThrows(NullPointerException.class, () -> {
            scheduledExecutor.schedule(callable, new CommonTriggers.OnceTrigger());
        });
    }

}
