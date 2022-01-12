/*
 * Copyright (c) 2013, 2022 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.concurrent.spec.ManagedScheduledExecutorService.inheritedapi_servlet;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ee.jakarta.tck.concurrent.common.CommonTasks;
import ee.jakarta.tck.concurrent.common.fixed.counter.CounterRunnableTask;
import ee.jakarta.tck.concurrent.common.fixed.counter.StaticCounter;
import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import ee.jakarta.tck.concurrent.framework.TestUtil;
import jakarta.servlet.annotation.WebServlet;

@SuppressWarnings("serial")
@WebServlet("InheritedAPIServlet")
public class InheritedAPIServlet extends TestServlet {
	
	@Override
	protected void before() {
		StaticCounter.reset();
	}

	public void testApiSubmit() throws Exception {
		Future result = TestUtil.getManagedScheduledExecutorService().submit(new CommonTasks.SimpleCallable());
		TestUtil.waitTillFutureIsDone(result);
		assertEquals(result.get(), CommonTasks.SIMPLE_RETURN_STRING);

		result = TestUtil.getManagedScheduledExecutorService().submit(new CommonTasks.SimpleRunnable());
		TestUtil.waitTillFutureIsDone(result);
		result.get();

		result = TestUtil.getManagedScheduledExecutorService().submit(new CommonTasks.SimpleRunnable(), CommonTasks.SIMPLE_RETURN_STRING);
		TestUtil.waitTillFutureIsDone(result);
		assertEquals(result.get(), CommonTasks.SIMPLE_RETURN_STRING);
	}

	public void testApiExecute() throws Exception{
		TestUtil.getManagedScheduledExecutorService().execute(new CounterRunnableTask());
		waitForCounter(1);
	}

	public void testApiInvokeAll() throws Exception{
		List taskList = new ArrayList();
		taskList.add(new CommonTasks.SimpleArgCallable(1));
		taskList.add(new CommonTasks.SimpleArgCallable(2));
		taskList.add(new CommonTasks.SimpleArgCallable(3));
		List<Future> resultList = TestUtil.getManagedScheduledExecutorService().invokeAll(taskList);
		for (Future each : resultList) {
			TestUtil.waitTillFutureIsDone(each);
		}
		assertEquals(resultList.get(0).get(), 1);
		assertEquals(resultList.get(1).get(), 2);
		assertEquals(resultList.get(2).get(), 3);
		resultList = TestUtil.getManagedScheduledExecutorService().invokeAll(taskList, TestConstants.WaitTimeout.getSeconds(),
				TimeUnit.SECONDS);
		for (Future each : resultList) {
			TestUtil.waitTillFutureIsDone(each);
		}
		assertEquals(resultList.get(0).get(), 1);
		assertEquals(resultList.get(1).get(), 2);
		assertEquals(resultList.get(2).get(), 3);

		try {
			taskList = new ArrayList();
			taskList.add(new CommonTasks.SimpleCallable(TestConstants.WaitTimeout.toMillis()));
			taskList.add(new CommonTasks.SimpleCallable(TestConstants.WaitTimeout.toMillis()));
			resultList = TestUtil.getManagedScheduledExecutorService().invokeAll(taskList, TestConstants.PollInterval.getSeconds(),
					TimeUnit.SECONDS);
			for (Future each : resultList) {
				TestUtil.waitTillFutureThrowsException(each, CancellationException.class);
			}
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
	}

	public void testApiInvokeAny() throws Exception {
		List taskList = new ArrayList();
		taskList.add(new CommonTasks.SimpleArgCallable(1));
		taskList.add(new CommonTasks.SimpleArgCallable(2));
		taskList.add(new CommonTasks.SimpleArgCallable(3));
		Object result = TestUtil.getManagedScheduledExecutorService().invokeAny(taskList);
		TestUtil.assertInRange(new Integer[] { 1, 2, 3 }, result);
		result = TestUtil.getManagedScheduledExecutorService().invokeAny(taskList, TestConstants.WaitTimeout.getSeconds(), TimeUnit.SECONDS);
		TestUtil.assertInRange(new Integer[] { 1, 2, 3 }, result);

		try {
			taskList = new ArrayList();
			taskList.add(new CommonTasks.SimpleCallable(TestConstants.WaitTimeout.toMillis()));
			taskList.add(new CommonTasks.SimpleCallable(TestConstants.WaitTimeout.toMillis()));
			result = TestUtil.getManagedScheduledExecutorService().invokeAny(taskList, TestConstants.PollInterval.getSeconds(),
					TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			return; //expected
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
		fail("Task should be cancelled because of timeout");
	}

	public void testApiSchedule() throws Exception {
		Future result = TestUtil.getManagedScheduledExecutorService().schedule(new CommonTasks.SimpleCallable(),
				TestConstants.PollInterval.getSeconds(), TimeUnit.SECONDS);
		TestUtil.waitTillFutureIsDone(result);
		assertEquals(result.get(), CommonTasks.SIMPLE_RETURN_STRING);

		result = TestUtil.getManagedScheduledExecutorService().schedule(new CommonTasks.SimpleRunnable(),
				TestConstants.PollInterval.getSeconds(), TimeUnit.SECONDS);
		TestUtil.waitTillFutureIsDone(result);
		assertEquals(result.get(), null);
	}

	public void testApiScheduleAtFixedRate() throws Exception {
		ScheduledFuture result = null;
		
		try {
			result = TestUtil.getManagedScheduledExecutorService().scheduleAtFixedRate(new CounterRunnableTask(),
					TestConstants.PollInterval.getSeconds(),
					TestConstants.PollInterval.getSeconds(), TimeUnit.SECONDS);
			TestUtil.sleep(TestConstants.WaitTimeout);
			TestUtil.assertIntInRange(TestConstants.PollsPerTimeout - 2, TestConstants.PollsPerTimeout + 2, StaticCounter.getCount());
		} catch (Exception e) {
			fail(e.getMessage());
		} finally {
			if (result != null) {
				result.cancel(true);
				// Sleep to ensure cancel take effect.
				try {
					TestUtil.sleep(TestConstants.PollInterval);
				} catch (Exception e) {
				}
			}
		}
	}

	public void testApiScheduleWithFixedDelay() throws Exception {
		ScheduledFuture result = null;
		try {
			result = TestUtil.getManagedScheduledExecutorService().scheduleWithFixedDelay(
					new CounterRunnableTask(TestConstants.PollInterval.toMillis()),
					TestConstants.PollInterval.getSeconds(),
					TestConstants.PollInterval.getSeconds(), TimeUnit.SECONDS);
			TestUtil.sleep(TestConstants.WaitTimeout);
			TestUtil.assertIntInRange((TestConstants.PollsPerTimeout / 2) - 2, (TestConstants.PollsPerTimeout / 2) + 2, StaticCounter.getCount());
		} catch (Exception e) {
			fail(e.getMessage());
		} finally {
			if (result != null) {
				result.cancel(true);
				// Sleep to ensure cancel take effect.
				try {
					TestUtil.sleep(TestConstants.PollInterval);
				} catch (Exception e) {
				}
			}
		}
	}
	
	private void waitForCounter(int expected) {
		long start = System.currentTimeMillis();

		while (expected != StaticCounter.getCount()) {
			try {
				TestUtil.sleep(TestConstants.PollInterval);
			} catch (InterruptedException ignore) {
			}

			if ((System.currentTimeMillis() - start) > TestConstants.WaitTimeout.toMillis()) {
				throw new RuntimeException("Static counter did not produce expected counter before timeout. Expected: " + expected + " Actual: " + StaticCounter.getCount());
			}
		}
	}
}
