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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;

import ee.jakarta.tck.concurrent.common.managed.task.listener.ListenerEvent;
import ee.jakarta.tck.concurrent.common.managed.task.listener.ManagedTaskListenerImpl;
import ee.jakarta.tck.concurrent.common.tasks.CallableTask;
import ee.jakarta.tck.concurrent.common.tasks.RunnableTask;
import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.junit.anno.Assertion;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common.PACKAGE;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;
import ee.jakarta.tck.concurrent.framework.junit.extensions.Wait;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedExecutors;
import jakarta.enterprise.concurrent.ManagedTask;
import jakarta.enterprise.concurrent.ManagedThreadFactory;

@Web
@Common({ PACKAGE.MANAGED_TASK_LISTENER, PACKAGE.TASKS })
public class ManagedExecutorsTests {

    @Deployment(name = "ManagedExecutorsTests")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addAsWebInfResource(ManagedExecutorsTests.class.getPackage(),
                "web.xml", "web.xml");
    }

    private static final String ENV_ENTRY_JNDI_NAME = "java:comp/env/StringValue";

    private static final String ENV_ENTRY_VALUE = "something";

    private ManagedTaskListenerImpl managedTaskListener = new ManagedTaskListenerImpl();

    private boolean shutdown = true;

    @Resource(lookup = TestConstants.defaultManagedThreadFactory)
    private ManagedThreadFactory threadFactory;

    @Resource(lookup = TestConstants.defaultManagedExecutorService)
    private ManagedExecutorService executor;

    @AfterEach
    public void after() {
        managedTaskListener.clearEvents();
    }

    private RunnableTask createRunnableTask() {
        return new RunnableTask(ENV_ENTRY_JNDI_NAME, ENV_ENTRY_VALUE, this.getClass().getName());
    }

    private CallableTask<String> createCallableTask(final String expectedReturnValue) {
        return new CallableTask<String>(ENV_ENTRY_JNDI_NAME, ENV_ENTRY_VALUE, this.getClass().getName(),
                expectedReturnValue);
    }

    private void assertTaskAndListenerComplete(final Future<?> future, final RunnableTask runnableTask) {
        Wait.waitForTaskComplete(future);
        assertListenerComplete(runnableTask);
    }

    private void assertTaskAndListenerComplete(final String expectedResult, final Future<String> future,
            final CallableTask<?> callableTask) {
        String result = Wait.waitForTaskComplete(future);
        assertTrue(expectedResult.endsWith(result));
        assertListenerComplete(callableTask);
    }

    private void assertListenerComplete(final RunnableTask task) {
        // wait for the listener run done.
        Wait.waitForListenerComplete(managedTaskListener);

        // check listener status.
        assertTrue(managedTaskListener.eventCalled(ListenerEvent.SUBMITTED));
        assertTrue(managedTaskListener.eventCalled(ListenerEvent.STARTING));
        assertTrue(managedTaskListener.eventCalled(ListenerEvent.DONE));

    }

    @Assertion(id = "JAVADOC:21", strategy = "Use a regular thread(non-Manageable thread) and verify isCurrentThreadShutdown() returns false")
    public void isCurrentThreadShutdown() {
        Thread createdThread = threadFactory.newThread(new Runnable() {
            @Override
            public void run() {
                shutdown = ManagedExecutors.isCurrentThreadShutdown();
            }
        });
        // Executors.newSingleThreadExecutor() uses Executors.defaultThreadFactory()
        // to create new thread. So the thread used in this test is a non Manageable
        // Thread.
        Future<?> future = Executors.newSingleThreadExecutor().submit(createdThread);
        Wait.waitForTaskComplete(future);
        assertFalse(shutdown, "Failed because shutdown is set to be true when running job");
    }

    @Assertion(id = "JAVADOC:21", strategy = "Create a ManageableThread from ManagedThreadFactory and check the shutdown status.")
    public void isCurrentThreadShutdownManageableThread() {
        Thread createdThread = threadFactory.newThread(new Runnable() {
            @Override
            public void run() {
                shutdown = ManagedExecutors.isCurrentThreadShutdown();
            }
        });
        // Executors.newSingleThreadExecutor(managedThreadFactory) uses
        // ManagedThreadFactory
        // to create new (Manageable) thread.
        Future<?> future = Executors.newSingleThreadExecutor(threadFactory).submit(createdThread);
        Wait.waitForTaskComplete(future);
        assertFalse(shutdown, "Failed because shutdown is set to be true when running job");
    }

    @Assertion(id = "JAVADOC:22 SPEC:7 SPEC:7.1 SPEC:7.2 SPEC:4 SPEC:4.2 SPEC:18",
            strategy = "Returns a Runnable object that also implements ManagedTask interface"
                    + " so it can receive notification of life cycle events with the provided ManagedTaskListener"
                    + " when the task is submitted to a ManagedExecutorService or a ManagedScheduledExecutorService.")
    public void manageRunnableTaskWithTaskListener() {
        RunnableTask runnableTask = createRunnableTask();
        Runnable taskWithListener = ManagedExecutors.managedTask(runnableTask, managedTaskListener);
        Future<?> futureResult = executor.submit(taskWithListener);
        assertTaskAndListenerComplete(futureResult, runnableTask);
    }

    @Assertion(id = "JAVADOC:23", strategy = "Catch IllegalArgumentException when get the manage task with null runnable task.")
    public void manageRunnableTaskWithNullArg() {
        Runnable nullTask = null;
        assertThrows(IllegalArgumentException.class, () -> {
            ManagedExecutors.managedTask(nullTask, managedTaskListener);
        });
    }

    @Assertion(id = "JAVADOC:24 SPEC:13",
            strategy = "Returns a Runnable object that also implements ManagedTask interface"
                    + " so it can receive notification of life cycle events with the provided ManagedTaskListener"
                    + " and to provide additional execution properties when the task is submitted to a ManagedExecutorService")
    public void manageRunnableTaskWithTaskListenerAndMap() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("key", "value");
        RunnableTask runnableTask = createRunnableTask();
        Runnable task = ManagedExecutors.managedTask(runnableTask, properties, managedTaskListener);

        assertTrue(task instanceof ManagedTask);
        ManagedTask managedTask = (ManagedTask) task;

        assertEquals("value", managedTask.getExecutionProperties().get("key"));

        assertTaskAndListenerComplete(executor.submit(task), runnableTask);
    }

    @Assertion(id = "JAVADOC:25", strategy = "Catch IllegalArgumentException when get the manage task with null runnable task and additional execution properties.")
    public void manageRunnableTaskWithMapAndNullArg() {
        Runnable nullTask = null;
        Map<String, String> properties = new HashMap<String, String>();

        assertThrows(IllegalArgumentException.class, () -> {
            ManagedExecutors.managedTask(nullTask, properties, managedTaskListener);
        });
    }

    @Assertion(id = "JAVADOC:26",
            strategy = "Returns a Callable object that also implements ManagedTask interface"
                    + " so it can receive notification of life cycle events with the provided ManagedTaskListener"
                    + " when the task is submitted to a ManagedExecutorService")
    public void manageCallableTaskWithTaskListener() {
        String expectedResultStr = "expected something";
        CallableTask<String> callableTask = createCallableTask(expectedResultStr);
        Callable<String> taskWithListener = ManagedExecutors.managedTask((Callable<String>) callableTask,
                managedTaskListener);
        Future<String> futureResult = executor.submit(taskWithListener);
        assertTaskAndListenerComplete(expectedResultStr, futureResult, callableTask);
    }

    @Assertion(id = "JAVADOC:27", strategy = "Catch IllegalArgumentException when get the manage task with null Callable task.")
    public void manageCallableTaskWithNullArg() {
        Callable<?> nullTask = null;
        assertThrows(IllegalArgumentException.class, () -> {
            ManagedExecutors.managedTask(nullTask, managedTaskListener);
        });
    }

    @Assertion(id = "JAVADOC:28 SPEC:13.1 SPEC:45 SPEC:45.1",
            strategy = "Returns a Callable object that also implements ManagedTask interface"
                    + " so it can receive notification of life cycle events with the provided ManagedTaskListener"
                    + " and to provide additional execution properties when the task is submitted to a ManagedExecutorService")
    public void manageCallableTaskWithTaskListenerAndMap() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("key", "value");
        properties.put(ManagedTask.IDENTITY_NAME, "id");
        String expectedResultStr = "expected something";

        CallableTask<String> callableTask = createCallableTask(expectedResultStr);
        Callable<String> task = ManagedExecutors.managedTask((Callable<String>) callableTask, properties,
                managedTaskListener);

        assertTrue(task instanceof ManagedTask);

        ManagedTask managedTask = (ManagedTask) task;

        assertEquals("value", managedTask.getExecutionProperties().get("key"));

        assertTaskAndListenerComplete(expectedResultStr, executor.submit(task), callableTask);
    }

    @Assertion(id = "JAVADOC:29", strategy = "Catch IllegalArgumentException when get the manage task with null Callable task and additional execution properties.")
    public void manageCallableTaskWithMapAndNullArg() {
        Callable<?> nullTask = null;
        Map<String, String> properties = new HashMap<String, String>();

        assertThrows(IllegalArgumentException.class, () -> {
            ManagedExecutors.managedTask(nullTask, properties, managedTaskListener);
        });
    }
}
