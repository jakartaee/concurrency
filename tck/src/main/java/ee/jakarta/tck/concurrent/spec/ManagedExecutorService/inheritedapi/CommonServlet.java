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

package ee.jakarta.tck.concurrent.spec.ManagedExecutorService.inheritedapi;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import ee.jakarta.tck.concurrent.framework.TestServlet;
import ee.jakarta.tck.concurrent.framework.TestUtil;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.servlet.annotation.WebServlet;

@SuppressWarnings("serial")
@WebServlet("CommonServlet")
public class CommonServlet extends TestServlet {

	@Resource
	ManagedExecutorService mes;

	public void testExecute() {
		Task<?> commonTask = new Task.CommonTask(0);
		mes.execute(commonTask);
		// wait for a while.
		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			fail(e.toString());
		}
	}

	public void testSubmit() {
		Task<?> commonTask = new Task.CommonTask(0);
		Future<?> noRes = mes.submit((Runnable) commonTask);
		try {
			TestUtil.waitForTaskComplete(noRes);
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testInvokeAny() {
		Task.CommonTask commonTask0 = new Task.CommonTask(0);
		Task.CommonTask commonTask1 = new Task.CommonTask(1);
		List<Task.CommonTask> tasks = new ArrayList<Task.CommonTask>();
		tasks.add(commonTask0);
		tasks.add(commonTask1);
		int res = -1;
		try {
			res = mes.invokeAny(tasks);
		} catch (InterruptedException e) {
			fail(e.toString());
		} catch (ExecutionException e) {
			fail(e.toString());
		}
		assertTrue(tasks.get(res).isRan());
	}

	public void testInvokeAll() {
		Task.CommonTask commonTask0 = new Task.CommonTask(0);
		Task.CommonTask commonTask1 = new Task.CommonTask(1);
		List<Task.CommonTask> tasks = new ArrayList<Task.CommonTask>();
		tasks.add(commonTask0);
		tasks.add(commonTask1);
		List<Future<Integer>> res = null;
		try {
			res = mes.invokeAll(tasks);
			TestUtil.waitForTaskComplete(res.get(0));
			TestUtil.waitForTaskComplete(res.get(1));
		} catch (Exception e) {
			fail(e.toString());
		}
		assertTrue(commonTask0.isRan());
		assertTrue(commonTask1.isRan());
	}

	public void testAtMostOnce() {
		Task.CommonTask commonTask = new Task.CommonTask(0);
		Future<?> future = mes.submit((Runnable) commonTask);
		try {
			TestUtil.waitForTaskComplete(future);
		} catch (Exception e) {
			fail(e.toString());
		}
		// check number.
		assertEquals(commonTask.runCount(), 1);
	}
}
