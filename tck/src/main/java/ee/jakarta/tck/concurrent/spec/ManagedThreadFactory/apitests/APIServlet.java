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

package ee.jakarta.tck.concurrent.spec.ManagedThreadFactory.apitests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import ee.jakarta.tck.concurrent.framework.TestUtil;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManageableThread;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("APIServlet")
public class APIServlet extends TestServlet {

	@Resource(lookup = TestConstants.DefaultManagedThreadFactory)
	private ManagedThreadFactory factory;

	public void interruptThreadApiTest(HttpServletRequest req, HttpServletResponse res) throws Exception {
		CounterRunnable task = new CounterRunnable();
		Thread thread = factory.newThread(task);
		thread.start();
		thread.interrupt();
		TestUtil.waitTillThreadFinish(thread);
		assertEquals(task.getCount(), 0);
	}
	
	public void implementsManageableThreadInterfaceTest(HttpServletRequest req, HttpServletResponse res) throws Exception {
		CounterRunnable task = new CounterRunnable();
		Thread thread = factory.newThread(task);
		assertTrue(thread instanceof ManageableThread, "The thread returned by ManagedThreadFactory should be instance of ManageableThread.");
	}

	public static class CounterRunnable implements Runnable {
		private volatile int count = 0;

		public int getCount() {
			return count;
		}

		public void run() {
			try {
				TestUtil.sleep(TestConstants.PollInterval);
				count++;
			} catch (InterruptedException ignore) {
				return;
			}
		}
	}
}
