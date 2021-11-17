/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.enterprise.concurrent.api.ManagedExecutors;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jakarta.enterprise.concurrent.tck.framework.TestClient;
import jakarta.enterprise.concurrent.tck.framework.TestUtil;
import jakarta.enterprise.concurrent.api.common.CallableTask;
import jakarta.enterprise.concurrent.api.common.RunnableTask;
import jakarta.enterprise.concurrent.api.common.Util;
import jakarta.enterprise.concurrent.api.common.managedTaskListener.ListenerEvent;
import jakarta.enterprise.concurrent.api.common.managedTaskListener.ManagedTaskListenerImpl;
import jakarta.enterprise.concurrent.tck.framework.TestLogger;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedExecutors;
import jakarta.enterprise.concurrent.ManagedTask;
import jakarta.enterprise.concurrent.ManagedThreadFactory;

public class ManagedExecutorsTests extends TestClient {

	private static final TestLogger log = TestLogger.get(ManagedExecutorsTests.class);

	private static final String MANAGED_EXECUTOR_SVC_JNDI_NAME = "java:comp/DefaultManagedExecutorService";

	private static final String MANAGED_THREAD_FACTORY_JNDI_NAME = "java:comp/DefaultManagedThreadFactory";

	private static final int TASK_MAX_WAIT_SECONDS = 10;// (s)

	private static final long LISTENER_MAX_WAIT_MILLIS = 3 * 1000;// (ms)

	private static final int LISTENER_POOL_INTERVAL_MILLIS = 100;// (ms)

	private static final String ENV_ENTRY_JNDI_NAME = "java:comp/env/StringValue";

	private static final String ENV_ENTRY_VALUE = "something";

	private ManagedExecutorService managedExecutorSvc;

	private ManagedThreadFactory managedThreadFactory;

	private ManagedTaskListenerImpl managedTaskListener;

	private boolean shutdown = true;

	@BeforeClass // TODO BeforeClass or BeforeTest
	public void setup(String[] args, Properties p) {
		Context ctx = null;
		try {
			ctx = new InitialContext();
			managedExecutorSvc = (ManagedExecutorService) ctx.lookup(MANAGED_EXECUTOR_SVC_JNDI_NAME);
			managedThreadFactory = (ManagedThreadFactory) ctx.lookup(MANAGED_THREAD_FACTORY_JNDI_NAME);
		} catch (Exception e) {
			setupFailure(e);
		} finally {
			try {
				ctx.close();
			} catch (NamingException e) {
				setupFailure(e);
			}
		}
		managedTaskListener = new ManagedTaskListenerImpl();
	}

	@AfterClass // TODO AfterClass or AfterTest
	public void cleanup() {
		managedTaskListener.clearEvents();
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
		Thread createdThread = managedThreadFactory.newThread(new Runnable() {
			@Override
			public void run() {
				shutdown = ManagedExecutors.isCurrentThreadShutdown();
			}
		});
		// Executors.newSingleThreadExecutor() uses Executors.defaultThreadFactory()
		// to create new thread. So the thread used in this test is a non Manageable
		// Thread.
		Future<?> future = Executors.newSingleThreadExecutor().submit(createdThread);
		waitForTaskComplete(future);
		assertFalse(testName + " failed because shutdown is set to be true when running job", shutdown);
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
		Thread createdThread = managedThreadFactory.newThread(new Runnable() {
			@Override
			public void run() {
				shutdown = ManagedExecutors.isCurrentThreadShutdown();
			}
		});
		// Executors.newSingleThreadExecutor(managedThreadFactory) uses
		// ManagedThreadFactory
		// to create new (Manageable) thread.
		Future<?> future = Executors.newSingleThreadExecutor(managedThreadFactory).submit(createdThread);
		waitForTaskComplete(future);
		assertFalse(testName + " failed because shutdown is set to be true when running job", shutdown);
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
		Future<?> futureResult = managedExecutorSvc.submit(taskWithListener);
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
		boolean pass = false;
		Runnable nullTask = null;
		try {
			ManagedExecutors.managedTask(nullTask, managedTaskListener);
		} catch (IllegalArgumentException e) {
			// this is what expected
			pass = true;
		} catch (Exception e) {
			log.warning("Unexpected Exception Caught", e);
		}

		assertTrue(testName + " failed to get expected exception", pass);
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
			if (managedTask.getExecutionProperties().get("key") == "value")
				pass = true;
		}

		assertTrue(testName + " failed to get expected property", pass);
		assertTaskAndListenerComplete(managedExecutorSvc.submit(task), runnableTask);
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
		boolean pass = false;
		Runnable nullTask = null;
		Map<String, String> properties = new HashMap<String, String>();
		try {
			ManagedExecutors.managedTask(nullTask, properties, managedTaskListener);
		} catch (IllegalArgumentException e) {
			// this is what expected
			pass = true;
		} catch (Exception e) {
			log.warning("Unexpected Exception Caught", e);
		}
		assertTrue(testName + " failed to get expected exception", pass);
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
		Future<String> futureResult = managedExecutorSvc.submit(taskWithListener);
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
		boolean pass = false;
		Callable<?> nullTask = null;
		try {
			ManagedExecutors.managedTask(nullTask, managedTaskListener);
		} catch (IllegalArgumentException e) {
			// this is what expected
			pass = true;
		} catch (Exception e) {
			log.warning("Unexpected Exception Caught", e);
		}
		assertTrue(testName + " failed to get expected exception", pass);
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
			if (managedTask.getExecutionProperties().get("key") == "value")
				pass = true;
		}
		assertTrue(testName + " failed to get expected property", pass);
		assertTaskAndListenerComplete(expectedResultStr, managedExecutorSvc.submit(task), callableTask);
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
		boolean pass = false;
		Callable<?> nullTask = null;
		Map<String, String> properties = new HashMap<String, String>();
		try {
			ManagedExecutors.managedTask(nullTask, properties, managedTaskListener);
		} catch (IllegalArgumentException e) {
			// this is what expected
			pass = true;
		} catch (Exception e) {
			log.warning("Unexpected Exception Caught", e);
		}
		assertTrue(testName + " failed", pass);
	}

	private void assertTaskAndListenerComplete(Future<?> future, RunnableTask runnableTask) {
		waitForTaskComplete(future);
		assertListenerComplete(runnableTask);
	}

	private void assertTaskAndListenerComplete(String expectedResult, Future<String> future,
			CallableTask<?> callableTask) {
		String result = waitForTaskComplete(future);
		assertTrue("Task return different value with expected one.", expectedResult.endsWith(result));
		assertListenerComplete(callableTask);
	}

	private <T> T waitForTaskComplete(Future<T> future) {
		return Util.waitForTaskComplete(future, TASK_MAX_WAIT_SECONDS);
	}

	private void assertListenerComplete(RunnableTask task) {
		// wait for the listener run done.
		Util.waitForListenerComplete(managedTaskListener, LISTENER_MAX_WAIT_MILLIS, LISTENER_POOL_INTERVAL_MILLIS);
		// check listener status.
		if (!(managedTaskListener.eventCalled(ListenerEvent.SUBMITTED)
				&& managedTaskListener.eventCalled(ListenerEvent.STARTING)
				&& managedTaskListener.eventCalled(ListenerEvent.DONE))) {
			fail("TaskListener is not completely executed.");
		}
	}

	private RunnableTask createRunnableTask() {
		return new RunnableTask(ENV_ENTRY_JNDI_NAME, ENV_ENTRY_VALUE, this.getClass().getName());
	}

	private CallableTask<String> createCallableTask(String expectedReturnValue) {
		return new CallableTask<String>(ENV_ENTRY_JNDI_NAME, ENV_ENTRY_VALUE, this.getClass().getName(),
				expectedReturnValue);
	}
}
