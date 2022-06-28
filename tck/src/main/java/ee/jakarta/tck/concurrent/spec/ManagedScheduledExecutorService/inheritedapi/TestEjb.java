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

package ee.jakarta.tck.concurrent.spec.ManagedScheduledExecutorService.inheritedapi;

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
import ee.jakarta.tck.concurrent.common.counter.Counter;
import ee.jakarta.tck.concurrent.common.counter.CounterRunnableTask;
import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.TestUtil;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

@Stateless
public class TestEjb implements TestEjbLocal {

	@EJB
	private Counter counter;

	public void testApiSubmit() {
		try {
			Future result = TestUtil.getManagedScheduledExecutorService().submit(new CommonTasks.SimpleCallable());
			TestUtil.waitTillFutureIsDone(result);
			assertEquals(result.get(), CommonTasks.SIMPLE_RETURN_STRING);

			result = TestUtil.getManagedScheduledExecutorService().submit(new CommonTasks.SimpleRunnable());
			TestUtil.waitTillFutureIsDone(result);
			result.get();

			result = TestUtil.getManagedScheduledExecutorService().submit(new CommonTasks.SimpleRunnable(), CommonTasks.SIMPLE_RETURN_STRING);
			TestUtil.waitTillFutureIsDone(result);
			assertEquals(result.get(), CommonTasks.SIMPLE_RETURN_STRING);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void testApiExecute() {
		try {
			TestUtil.getManagedScheduledExecutorService().execute(new CounterRunnableTask(InheritedAPITests.CounterSingletonJNDI));
			waitForCounter(1);
		} catch (Exception e) {
			fail(e.getMessage());
		}finally {
			counter.reset();
		}
	}

	public void testApiInvokeAll() {
		try {
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

			resultList = TestUtil.getManagedScheduledExecutorService().invokeAll(taskList, TestConstants.WaitTimeout.getSeconds(), TimeUnit.SECONDS);
			for (Future each : resultList) {
				TestUtil.waitTillFutureIsDone(each);
			}
			assertEquals(resultList.get(0).get(), 1);
			assertEquals(resultList.get(1).get(), 2);
			assertEquals(resultList.get(2).get(), 3);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		try {
			List taskList = new ArrayList();
			taskList.add(new CommonTasks.SimpleCallable(TestConstants.WaitTimeout.toMillis()));
			taskList.add(new CommonTasks.SimpleCallable(TestConstants.WaitTimeout.toMillis()));
			List<Future> resultList = TestUtil.getManagedScheduledExecutorService().invokeAll(taskList, TestConstants.PollInterval.getSeconds(),
					TimeUnit.SECONDS);
			for (Future each : resultList) {
				TestUtil.waitTillFutureThrowsException(each, CancellationException.class);
			}
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
	}

	public void testApiInvokeAny() {
		try {
			List taskList = new ArrayList();
			taskList.add(new CommonTasks.SimpleArgCallable(1));
			taskList.add(new CommonTasks.SimpleArgCallable(2));
			taskList.add(new CommonTasks.SimpleArgCallable(3));
			Object result = TestUtil.getManagedScheduledExecutorService().invokeAny(taskList);
			TestUtil.assertInRange(new Integer[] { 1, 2, 3 }, result);

			result = TestUtil.getManagedScheduledExecutorService().invokeAny(taskList, TestConstants.WaitTimeout.getSeconds(), TimeUnit.SECONDS);
			TestUtil.assertInRange(new Integer[] { 1, 2, 3 }, result);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		try {
			List taskList = new ArrayList();
			taskList.add(new CommonTasks.SimpleCallable(TestConstants.WaitTimeout.toMillis()));
			taskList.add(new CommonTasks.SimpleCallable(TestConstants.WaitTimeout.toMillis()));
			Object result = TestUtil.getManagedScheduledExecutorService().invokeAny(taskList, TestConstants.PollInterval.getSeconds(), TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			return; //expected
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
		fail("Task should be cancelled because of timeout");
	}

	public void testApiSchedule() {
		try {
			Future result = TestUtil.getManagedScheduledExecutorService().schedule(new CommonTasks.SimpleCallable(),
					TestConstants.PollInterval.getSeconds(), TimeUnit.SECONDS);
			TestUtil.waitTillFutureIsDone(result);
			assertEquals(CommonTasks.SIMPLE_RETURN_STRING, result.get());

			result = TestUtil.getManagedScheduledExecutorService().schedule(new CommonTasks.SimpleRunnable(), TestConstants.PollInterval.getSeconds(),
					TimeUnit.SECONDS);
			TestUtil.waitTillFutureIsDone(result);
			assertEquals(result.get(), null);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void testApiScheduleAtFixedRate() {
		ScheduledFuture result = null;
		try {
			result = TestUtil.getManagedScheduledExecutorService().scheduleAtFixedRate(new CounterRunnableTask(InheritedAPITests.CounterSingletonJNDI),
					TestConstants.PollInterval.getSeconds(), TestConstants.PollInterval.getSeconds(), TimeUnit.SECONDS);
			TestUtil.sleep(TestConstants.WaitTimeout);
			TestUtil.assertIntInRange(TestConstants.PollsPerTimeout - 2, TestConstants.PollsPerTimeout + 2, counter.getCount());
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
			counter.reset();
		}
	}

	public void testApiScheduleWithFixedDelay() {
		ScheduledFuture result = null;
		try {
			result = TestUtil.getManagedScheduledExecutorService().scheduleWithFixedDelay(
					new CounterRunnableTask(InheritedAPITests.CounterSingletonJNDI, TestConstants.PollInterval.toMillis()), //task
					TestConstants.PollInterval.getSeconds(), //initial delay
					TestConstants.PollInterval.getSeconds(), //delay
					TimeUnit.SECONDS); //Time units
			TestUtil.sleep(TestConstants.WaitTimeout);
			TestUtil.assertIntInRange((TestConstants.PollsPerTimeout / 2) - 2, (TestConstants.PollsPerTimeout / 2) + 2, counter.getCount());
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
			counter.reset();
		}
	}
	
	private void waitForCounter(int expected) {
		long start = System.currentTimeMillis();

		while (expected != counter.getCount()) {
			try {
				TestUtil.sleep(TestConstants.PollInterval);
			} catch (InterruptedException ignore) {
			}

			if ((System.currentTimeMillis() - start) > TestConstants.WaitTimeout.toMillis()) {
				throw new RuntimeException("Static counter did not produce expected counter before timeout. Expected: " + expected + " Actual: " + counter.getCount());
			}
		}
	}
}
