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

package jakarta.enterprise.concurrent.api.LastExecution;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import javax.naming.InitialContext;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jakarta.enterprise.concurrent.util.TestClient;
import jakarta.enterprise.concurrent.api.common.Util;
import jakarta.enterprise.concurrent.common.counter.CounterCallableTask;
import jakarta.enterprise.concurrent.common.counter.CounterRunnableTask;
import jakarta.enterprise.concurrent.common.counter.StaticCounter;

import jakarta.enterprise.concurrent.ManagedExecutors;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.ManagedTask;

public class LastExecutionTests extends TestClient {

	InitialContext context;

	ManagedScheduledExecutorService executorService;

	public static final String IDENTITY_NAME_TEST_ID = "lastExecutionGetIdentityNameTest";

	@BeforeClass // TODO BeforeClass or BeforeTest
	public void setup() {
		try {
			context = new InitialContext();
			executorService = (ManagedScheduledExecutorService) context
					.lookup(Util.SCHEDULED_MANAGED_EXECUTOR_SVC_JNDI_NAME);
			StaticCounter.reset();
		} catch (Exception e) {
			setupFailure(e);
		}
	}

	/*
	 * @testName: lastExecutionGetIdentityNameTest
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:15
	 * 
	 * @test_Strategy: The name or ID of the identifiable object, as specified in
	 * the ManagedTask#IDENTITY_NAME execution property of the task if it also
	 * implements the ManagedTask interface.
	 */
	@Test
	public void lastExecutionGetIdentityNameTest() {

		Map<String, String> executionProperties = new HashMap<String, String>();
		executionProperties.put(ManagedTask.IDENTITY_NAME, IDENTITY_NAME_TEST_ID);

		ScheduledFuture sf = executorService.schedule(
				ManagedExecutors.managedTask(new CounterRunnableTask(), executionProperties, null),
				new LogicDrivenTrigger(Util.COMMON_CHECK_INTERVAL,
						LogicDrivenTrigger.TEST_NAME_LASTEXECUTIONGETIDENTITYNAMETEST));
		Util.waitTillFutureIsDone(sf);

		assertEquals("Got wrong identity name. See server log for more details.", LogicDrivenTrigger.RIGHT_COUNT, // expected
				StaticCounter.getCount()); // actual
	}

	/*
	 * @testName: lastExecutionGetResultTest
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:16
	 * 
	 * @test_Strategy: Result of the last execution.
	 */
	@Test
	public void lastExecutionGetResultTest() {
		// test with runnable, LastExecution should return null
		ScheduledFuture sf = executorService
				.schedule(ManagedExecutors.managedTask(new CounterRunnableTask(), null, null), new LogicDrivenTrigger(
						Util.COMMON_CHECK_INTERVAL, LogicDrivenTrigger.TEST_NAME_LASTEXECUTIONGETRESULTTEST_RUNNABLE));
		Util.waitTillFutureIsDone(sf);

		assertEquals("Got wrong last execution result. See server log for more details.",
				LogicDrivenTrigger.RIGHT_COUNT, // expected
				StaticCounter.getCount()); // actual

		StaticCounter.reset();
		// test with callable, LastExecution should return 1
		sf = executorService.schedule(ManagedExecutors.managedTask(new CounterCallableTask(), null, null),
				new LogicDrivenTrigger(Util.COMMON_CHECK_INTERVAL,
						LogicDrivenTrigger.TEST_NAME_LASTEXECUTIONGETRESULTTEST_CALLABLE));
		Util.waitTillFutureIsDone(sf);

		assertEquals("Got wrong last execution result. See server log for more details.",
				LogicDrivenTrigger.RIGHT_COUNT, // expected
				StaticCounter.getCount()); // actual
	}

	/*
	 * @testName: lastExecutionGetRunningTimeTest
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:17; CONCURRENCY:JAVADOC:18;
	 * CONCURRENCY:JAVADOC:19
	 * 
	 * @test_Strategy: The last time in which the task was completed.
	 */
	@Test
	public void lastExecutionGetRunningTimeTest() {
		ScheduledFuture sf = executorService.schedule(ManagedExecutors.managedTask(
				new CounterRunnableTask(LogicDrivenTrigger.LASTEXECUTIONGETRUNNINGTIMETEST_SLEEP_TIME), null, null),
				new LogicDrivenTrigger(Util.COMMON_CHECK_INTERVAL,
						LogicDrivenTrigger.TEST_NAME_LASTEXECUTIONGETRUNNINGTIMETEST));
		Util.waitTillFutureIsDone(sf);
		assertEquals("Got wrong last execution result.", LogicDrivenTrigger.RIGHT_COUNT, // expected
				StaticCounter.getCount()); // actual
	}

}
