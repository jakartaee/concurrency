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

package jakarta.enterprise.concurrent.spec.ManagedExecutorService.inheritedapi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.tck.framework.TestServlet;
import jakarta.enterprise.concurrent.tck.framework.TestUtil;
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
			e.printStackTrace();
		}
	}

	public void testSubmit() {
		Task<?> commonTask = new Task.CommonTask(0);
		Future<?> noRes = mes.submit((Runnable) commonTask);
		try {
			TestUtil.waitForTaskComplete(noRes);
		} catch (Exception e) {
			e.printStackTrace();
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
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		if (!tasks.get(res).isRan()) {
			throw new RuntimeException("failed to run any tasks");
		}
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
			e.printStackTrace();
		}
		if (!commonTask0.isRan() || !commonTask1.isRan()) {
			throw new RuntimeException("failed to run all tasks");
		}
	}

	public void testAtMostOnce() {
		Task.CommonTask commonTask = new Task.CommonTask(0);
		Future<?> future = mes.submit((Runnable) commonTask);
		try {
			TestUtil.waitForTaskComplete(future);
			// check number.
			if (commonTask.runCount() == 1) {
				return; //expected
			} else {
				throw new RuntimeException("failed to run task exactly once");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
