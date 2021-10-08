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

package jakarta.enterprise.concurrent.api.ManagedScheduledExecutorService;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;

import javax.naming.InitialContext;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jakarta.enterprise.concurrent.util.TestClient;
import jakarta.enterprise.concurrent.api.common.CallableTask;
import jakarta.enterprise.concurrent.api.common.CommonTriggers;
import jakarta.enterprise.concurrent.api.common.RunnableTask;
import jakarta.enterprise.concurrent.api.common.Util;

import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;

public class ManagedScheduledExecutorServiceTests extends TestClient {

	InitialContext context;

	ManagedScheduledExecutorService executorService;

	public static final String CALLABLETESTTASK1_RUN_RESULT = "CallableTestTask1";

	private static final String TEST_JNDI_EVN_ENTRY_VALUE = "hello";

	private static final String TEST_JNDI_EVN_ENTRY_JNDI_NAME = "java:comp/env/ManagedScheduledExecutorService_test_string";

	private static final String TEST_CLASSLOADER_CLASS_NAME = "com.sun.ts.tests.concurrency.api.ManagedScheduledExecutorService.Client";

	@BeforeClass // TODO BeforeClass or BeforeTest
	public void setup() {
		try {
			context = new InitialContext();
			executorService = (ManagedScheduledExecutorService) context
					.lookup(Util.SCHEDULED_MANAGED_EXECUTOR_SVC_JNDI_NAME);

		} catch (Exception e) {
			setupFailure(e);
		}
	}

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
	public void normalScheduleProcess1Test() {
		ScheduledFuture result = executorService.schedule(
				new RunnableTask(TEST_JNDI_EVN_ENTRY_JNDI_NAME, TEST_JNDI_EVN_ENTRY_VALUE, TEST_CLASSLOADER_CLASS_NAME),
				new CommonTriggers.OnceTrigger());
		Util.waitForTaskComplete(result, Util.COMMON_TASK_TIMEOUT_IN_SECOND);

		try {
			Object obj = result.get();
			assertNull(obj);
		} catch (Exception e) {
			fail(e);
		}
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

		try {
			executorService.schedule(command, new CommonTriggers.OnceTrigger());
		} catch (NullPointerException e) {
			return; // expected
		}

		fail("NullPointerException should be thrown when arg command is null");
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
	public void normalScheduleProcess2Test() {
		ScheduledFuture result = executorService
				.schedule(
						(Callable) new CallableTask(TEST_JNDI_EVN_ENTRY_JNDI_NAME, TEST_JNDI_EVN_ENTRY_VALUE,
								TEST_CLASSLOADER_CLASS_NAME, CALLABLETESTTASK1_RUN_RESULT),
						new CommonTriggers.OnceTrigger());
		Util.waitForTaskComplete(result, Util.COMMON_TASK_TIMEOUT_IN_SECOND);

		try {
			Object obj = result.get();

			if (CALLABLETESTTASK1_RUN_RESULT.equals(obj)) {
				return;
			} else {
				throw new RuntimeException("get wrong result:" + obj);
			}

		} catch (Exception e) {
			fail(e);
		}
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
		Callable callable = null;

		try {
			executorService.schedule(callable, new CommonTriggers.OnceTrigger());
		} catch (NullPointerException e) {
			return; // expected
		}

		fail("NullPointerException should be thrown when arg command is null");
	}

}
