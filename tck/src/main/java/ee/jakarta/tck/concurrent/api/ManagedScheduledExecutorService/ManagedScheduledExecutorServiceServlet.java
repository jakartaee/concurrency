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
package ee.jakarta.tck.concurrent.api.ManagedScheduledExecutorService;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;

import ee.jakarta.tck.concurrent.common.CallableTask;
import ee.jakarta.tck.concurrent.common.CommonTriggers;
import ee.jakarta.tck.concurrent.common.RunnableTask;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import ee.jakarta.tck.concurrent.framework.TestUtil;
import jakarta.servlet.annotation.WebServlet;

@SuppressWarnings("serial")
@WebServlet("ManagedScheduledExecutorServiceServlet")
public class ManagedScheduledExecutorServiceServlet extends TestServlet{
	
	public static final String CALLABLETESTTASK1_RUN_RESULT = "CallableTestTask1";

	private static final String TEST_JNDI_EVN_ENTRY_VALUE = "hello";

	private static final String TEST_JNDI_EVN_ENTRY_JNDI_NAME = "java:comp/env/ManagedScheduledExecutorService_test_string";

	private static final String TEST_CLASSLOADER_CLASS_NAME = ManagedScheduledExecutorServiceServlet.class.getCanonicalName();


	public void normalScheduleProcess1Test() throws Exception {
		ScheduledFuture result = TestUtil.getManagedScheduledExecutorService().schedule(
				new RunnableTask(TEST_JNDI_EVN_ENTRY_JNDI_NAME, TEST_JNDI_EVN_ENTRY_VALUE, TEST_CLASSLOADER_CLASS_NAME),
				new CommonTriggers.OnceTrigger());
		TestUtil.waitForTaskComplete(result);
		
		Object obj = result.get();
		if(obj != null) {
			throw new RuntimeException ("expected null, instead got result: " + obj.toString());
		}
	}

	public void nullCommandScheduleProcessTest() {
		Runnable command = null;

		try {
			TestUtil.getManagedScheduledExecutorService().schedule(command, new CommonTriggers.OnceTrigger());
		} catch (NullPointerException e) {
			return; // expected
		}

		throw new RuntimeException("NullPointerException should be thrown when arg command is null");
	}

	public void normalScheduleProcess2Test() throws Exception {
		ScheduledFuture result = TestUtil.getManagedScheduledExecutorService()
				.schedule(
						(Callable) new CallableTask(TEST_JNDI_EVN_ENTRY_JNDI_NAME, TEST_JNDI_EVN_ENTRY_VALUE,
								TEST_CLASSLOADER_CLASS_NAME, CALLABLETESTTASK1_RUN_RESULT),
						new CommonTriggers.OnceTrigger());
		TestUtil.waitForTaskComplete(result);

		Object obj = result.get();

		if (CALLABLETESTTASK1_RUN_RESULT.equals(obj)) {
			return;
		} else {
			throw new RuntimeException("get wrong result:" + obj);
		}

	}

	public void nullCallableScheduleProcessTest() {
		Callable callable = null;

		try {
			TestUtil.getManagedScheduledExecutorService().schedule(callable, new CommonTriggers.OnceTrigger());
		} catch (NullPointerException e) {
			return; // expected
		}

		throw new RuntimeException("NullPointerException should be thrown when arg command is null");
	}

}
