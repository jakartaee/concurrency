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

package ee.jakarta.tck.concurrent.api.ManagedExecutors;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.common.managed.task.listener.ListenerEvent;
import ee.jakarta.tck.concurrent.common.managed.task.listener.ManagedTaskListenerImpl;
import ee.jakarta.tck.concurrent.common.tasks.CallableTask;
import ee.jakarta.tck.concurrent.common.tasks.RunnableTask;
import ee.jakarta.tck.concurrent.framework.TestLogger;
import ee.jakarta.tck.concurrent.framework.TestUtil;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common.PACKAGE;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;
import jakarta.enterprise.concurrent.ManagedExecutors;
import jakarta.enterprise.concurrent.ManagedTask;

@Web
@Common({ PACKAGE.MANAGED_TASK_LISTENER, PACKAGE.TASKS })
public class ManagedExecutorsTests {

    private static final TestLogger log = TestLogger.get(ManagedExecutorsTests.class);

    // TODO deploy as EJB and JSP artifacts
    @Deployment(name = "ManagedExecutorsTests")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsWebInfResource(ManagedExecutorsTests.class.getPackage(), "web.xml", "web.xml");
    }

    private static final String ENV_ENTRY_JNDI_NAME = "java:comp/env/StringValue";

    private static final String ENV_ENTRY_VALUE = "something";

    private ManagedTaskListenerImpl managedTaskListener = new ManagedTaskListenerImpl();

    private boolean shutdown = true;

    @AfterEach
    public void after() {
        managedTaskListener.clearEvents();
    }

    private RunnableTask createRunnableTask() {
        return new RunnableTask(ENV_ENTRY_JNDI_NAME, ENV_ENTRY_VALUE, this.getClass().getName());
    }

    private CallableTask<String> createCallableTask(String expectedReturnValue) {
        return new CallableTask<String>(ENV_ENTRY_JNDI_NAME, ENV_ENTRY_VALUE, this.getClass().getName(),
                expectedReturnValue);
    }

    private void assertTaskAndListenerComplete(Future<?> future, RunnableTask runnableTask) {
        TestUtil.waitForTaskComplete(future);
        assertListenerComplete(runnableTask);
    }

    private void assertTaskAndListenerComplete(String expectedResult, Future<String> future,
            CallableTask<?> callableTask) {
        String result = TestUtil.waitForTaskComplete(future);
        if (!expectedResult.endsWith(result))
            throw new RuntimeException("Task return different value with expected one.");
        assertListenerComplete(callableTask);
    }

    private void assertListenerComplete(RunnableTask task) {
        // wait for the listener run done.
        TestUtil.waitForListenerComplete(managedTaskListener);
        // check listener status.
        if (!(managedTaskListener.eventCalled(ListenerEvent.SUBMITTED)
                && managedTaskListener.eventCalled(ListenerEvent.STARTING)
                && managedTaskListener.eventCalled(ListenerEvent.DONE))) {
            throw new RuntimeException("TaskListener is not completely executed.");
        }
    }

    /*
     * @testName: IsCurrentThreadShutdown
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:21
     * 
     * @test_Strategy: Use a regular thread(non-Manageable thread) and verify
     * isCurrentThreadShutdown() returns false
     * 
     */
    @Test
    public void IsCurrentThreadShutdown() {
        Thread createdThread = TestUtil.getManagedThreadFactory().newThread(new Runnable() {
            @Override
            public void run() {
                shutdown = ManagedExecutors.isCurrentThreadShutdown();
            }
        });
        // Executors.newSingleThreadExecutor() uses Executors.defaultThreadFactory()
        // to create new thread. So the thread used in this test is a non Manageable
        // Thread.
        Future<?> future = Executors.newSingleThreadExecutor().submit(createdThread);
        TestUtil.waitForTaskComplete(future);
        if (shutdown) {
            throw new RuntimeException("Failed because shutdown is set to be true when running job");
        }
    }

    /*
     * @testName: IsCurrentThreadShutdown_ManageableThread
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:21
     * 
     * @test_Strategy: Create a ManageableThread from ManagedThreadFactory and check
     * the shutdown status.
     */
    @Test
    public void IsCurrentThreadShutdown_ManageableThread() {
        Thread createdThread = TestUtil.getManagedThreadFactory().newThread(new Runnable() {
            @Override
            public void run() {
                shutdown = ManagedExecutors.isCurrentThreadShutdown();
            }
        });
        // Executors.newSingleThreadExecutor(managedThreadFactory) uses
        // ManagedThreadFactory
        // to create new (Manageable) thread.
        Future<?> future = Executors.newSingleThreadExecutor(TestUtil.getManagedThreadFactory()).submit(createdThread);
        TestUtil.waitForTaskComplete(future);
        if (shutdown) {
            throw new RuntimeException("Failed because shutdown is set to be true when running job");
        }
    }

    /*
     * @testName: ManageRunnableTaskWithTaskListener
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:22;CONCURRENCY:SPEC:7;
     * CONCURRENCY:SPEC:7.1;CONCURRENCY:SPEC:7.2;
     * CONCURRENCY:SPEC:4;CONCURRENCY:SPEC:4.2; CONCURRENCY:SPEC:18;
     *
     * @test_Strategy: Returns a Runnable object that also implements ManagedTask
     * interface so it can receive notification of life cycle events with the
     * provided ManagedTaskListener when the task is submitted to a
     * ManagedExecutorService or a ManagedScheduledExecutorService.
     */
    @Test
    public void ManageRunnableTaskWithTaskListener() {
        RunnableTask runnableTask = createRunnableTask();
        Runnable taskWithListener = ManagedExecutors.managedTask(runnableTask, managedTaskListener);
        Future<?> futureResult = TestUtil.getManagedExecutorService().submit(taskWithListener);
        assertTaskAndListenerComplete(futureResult, runnableTask);
    }

    /*
     * @testName: ManageRunnableTaskWithNullArg
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:23
     * 
     * @test_Strategy: Catch IllegalArgumentException when get the manage task with
     * null runnable task.
     */
    @Test
    public void ManageRunnableTaskWithNullArg() {
        Runnable nullTask = null;
        try {
            ManagedExecutors.managedTask(nullTask, managedTaskListener);
        } catch (IllegalArgumentException e) {
            return; // expected
        } catch (Exception e) {
            log.warning("Unexpected Exception Caught", e);
        }

        throw new RuntimeException("Failed to get expected exception");
    }

    /*
     * @testName: ManageRunnableTaskWithTaskListenerAndMap
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:24;CONCURRENCY:SPEC:13;
     * 
     * @test_Strategy: Returns a Runnable object that also implements ManagedTask
     * interface so it can receive notification of life cycle events with the
     * provided ManagedTaskListener and to provide additional execution properties
     * when the task is submitted to a ManagedExecutorService
     */
    @Test
    public void ManageRunnableTaskWithTaskListenerAndMap() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("key", "value");
        RunnableTask runnableTask = createRunnableTask();
        Runnable task = ManagedExecutors.managedTask(runnableTask, properties, managedTaskListener);
        boolean pass = false;
        if (task instanceof ManagedTask) {
            ManagedTask managedTask = (ManagedTask) task;
            if (managedTask.getExecutionProperties().get("key") != "value")
                throw new RuntimeException("Failed to get expected property");
        }

        assertTaskAndListenerComplete(TestUtil.getManagedExecutorService().submit(task), runnableTask);
    }

    /*
     * @testName: ManageRunnableTaskWithMapAndNullArg
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:25
     * 
     * @test_Strategy: Catch IllegalArgumentException when get the manage task with
     * null runnable task and additional execution properties.
     */
    @Test
    public void ManageRunnableTaskWithMapAndNullArg() {
        Runnable nullTask = null;
        Map<String, String> properties = new HashMap<String, String>();
        try {
            ManagedExecutors.managedTask(nullTask, properties, managedTaskListener);
        } catch (IllegalArgumentException e) {
            return; // expected
        } catch (Exception e) {
            log.warning("Unexpected Exception Caught", e);
        }
        throw new RuntimeException("Failed to get expected exception");
    }

    /*
     * @testName: ManageCallableTaskWithTaskListener
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:26
     * 
     * @test_Strategy: Returns a Callable object that also implements ManagedTask
     * interface so it can receive notification of life cycle events with the
     * provided ManagedTaskListener when the task is submitted to a
     * ManagedExecutorService
     */
    @Test
    public void ManageCallableTaskWithTaskListener() {
        String expectedResultStr = "expected something";
        CallableTask<String> callableTask = createCallableTask(expectedResultStr);
        Callable<String> taskWithListener = ManagedExecutors.managedTask((Callable<String>) callableTask,
                managedTaskListener);
        Future<String> futureResult = TestUtil.getManagedExecutorService().submit(taskWithListener);
        assertTaskAndListenerComplete(expectedResultStr, futureResult, callableTask);
    }

    /*
     * @testName: ManageCallableTaskWithNullArg
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:27
     * 
     * @test_Strategy: Catch IllegalArgumentException when get the manage task with
     * null Callable task.
     */
    @Test
    public void ManageCallableTaskWithNullArg() {
        Callable<?> nullTask = null;
        try {
            ManagedExecutors.managedTask(nullTask, managedTaskListener);
        } catch (IllegalArgumentException e) {
            return; // expected
        } catch (Exception e) {
            log.warning("Unexpected Exception Caught", e);
        }
        throw new RuntimeException("Failed to get expected exception");
    }

    /*
     * @testName: ManageCallableTaskWithTaskListenerAndMap
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:28;CONCURRENCY:SPEC:13.1;
     * CONCURRENCY:SPEC:45;CONCURRENCY:SPEC:45.1;
     * 
     * @test_Strategy: Returns a Callable object that also implements ManagedTask
     * interface so it can receive notification of life cycle events with the
     * provided ManagedTaskListener and to provide additional execution properties
     * when the task is submitted to a ManagedExecutorService
     */
    @Test
    public void ManageCallableTaskWithTaskListenerAndMap() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("key", "value");
        properties.put(ManagedTask.IDENTITY_NAME, "id");
        String expectedResultStr = "expected something";

        CallableTask<String> callableTask = createCallableTask(expectedResultStr);
        Callable<String> task = ManagedExecutors.managedTask((Callable<String>) callableTask, properties,
                managedTaskListener);

        boolean pass = false;
        if (task instanceof ManagedTask) {
            ManagedTask managedTask = (ManagedTask) task;
            if (managedTask.getExecutionProperties().get("key") != "value")
                throw new RuntimeException("Failed to get expected property");
        }
        assertTaskAndListenerComplete(expectedResultStr, TestUtil.getManagedExecutorService().submit(task),
                callableTask);
    }

    /*
     * @testName: ManageCallableTaskWithMapAndNullArg
     * 
     * @assertion_ids: CONCURRENCY:JAVADOC:29
     * 
     * @test_Strategy: Catch IllegalArgumentException when get the manage task with
     * null Callable task and additional execution properties.
     */
    @Test
    public void ManageCallableTaskWithMapAndNullArg() {
        Callable<?> nullTask = null;
        Map<String, String> properties = new HashMap<String, String>();
        try {
            ManagedExecutors.managedTask(nullTask, properties, managedTaskListener);
        } catch (IllegalArgumentException e) {
            return; // expected
        } catch (Exception e) {
            log.warning("Unexpected Exception Caught", e);
        }
        throw new RuntimeException("Failed to get expected exception");
    }
}
