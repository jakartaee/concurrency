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

package ee.jakarta.tck.concurrent.spec.ManagedThreadFactory.context;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.naming.InitialContext;

import ee.jakarta.tck.concurrent.common.tasks.RunnableTask;
import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import ee.jakarta.tck.concurrent.framework.TestUtil;
import jakarta.ejb.EJB;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("/SecurityServlet")
public class SecurityServlet extends TestServlet {
	
	@EJB
	private SecurityTestInterface str;

	private static final String TEST_JNDI_EVN_ENTRY_VALUE = "hello";

	private static final String TEST_JNDI_EVN_ENTRY_JNDI_NAME = "java:comp/env/ManagedThreadFactory_test_string";

	private static final String TEST_CLASSLOADER_CLASS_NAME = SecurityServlet.class.getCanonicalName();

	public void jndiClassloaderPropagationTest(HttpServletRequest req, HttpServletResponse res) throws Exception {

			ManagedThreadFactory factory = InitialContext.doLookup(TestConstants.DefaultManagedThreadFactory);

			CounterRunnableWithContext task = new CounterRunnableWithContext();
			Thread thread = factory.newThread(task);
			thread.start();
			TestUtil.waitTillThreadFinish(thread);
			assertEquals(task.getCount(), 1);
	}
	
	public void jndiClassloaderPropagationWithSecurityTest(HttpServletRequest req, HttpServletResponse res) throws Exception {

		req.login("javajoe", "javajoe");
		
		ManagedThreadFactory factory = InitialContext.doLookup(TestConstants.DefaultManagedThreadFactory);

		CounterRunnableWithSecurityCheck task = new CounterRunnableWithSecurityCheck(str);
		Thread thread = factory.newThread(task);
		thread.start();
		TestUtil.waitTillThreadFinish(thread);
		assertEquals(task.getCount(), 1);
}

	public static class CounterRunnableWithContext extends RunnableTask {
		private volatile int count = 0;

		public int getCount() {
			return count;
		}

		public CounterRunnableWithContext() {
			super(TEST_JNDI_EVN_ENTRY_JNDI_NAME, TEST_JNDI_EVN_ENTRY_VALUE, TEST_CLASSLOADER_CLASS_NAME);
		}

		public void run() {
			super.run();
			count++;
		}
	}

	public static class CounterRunnableWithSecurityCheck implements Runnable {
		private volatile int count = 0;
		
		private SecurityTestInterface str;
		
		CounterRunnableWithSecurityCheck(SecurityTestInterface str) {
			this.str = str;
		}

		public int getCount() {
			return count;
		}

		public void run() {
			try {
				assertEquals(str.managerMethod1(), TestConstants.SimpleReturnValue);
			} catch (Exception e) {
				return;
			}
			count++;
		}
	}
}
