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

package jakarta.enterprise.concurrent.spec.ManagedThreadFactory.context_servlet;

import javax.naming.InitialContext;

import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.enterprise.concurrent.api.common.RunnableTask;
import jakarta.enterprise.concurrent.tck.framework.TestConstants;
import jakarta.enterprise.concurrent.tck.framework.TestServlet;
import jakarta.enterprise.concurrent.tck.framework.TestUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class ContextServlet extends TestServlet {

	private static final String TEST_JNDI_EVN_ENTRY_VALUE = "hello";

	private static final String TEST_JNDI_EVN_ENTRY_JNDI_NAME = "java:comp/env/ManagedThreadFactory_test_string";

	private static final String TEST_CLASSLOADER_CLASS_NAME = ContextServlet.class.getCanonicalName();

	public void jndiClassloaderPropagationTest(HttpServletRequest req, HttpServletResponse res) throws Exception {
		InitialContext context = new InitialContext();
		ManagedThreadFactory factory = (ManagedThreadFactory) context
				.lookup(TestConstants.DefaultManagedThreadFactory);

		CounterRunnableWithContext task = new CounterRunnableWithContext();
		Thread thread = factory.newThread(task);
		thread.start();
		TestUtil.waitTillThreadFinish(thread);
		TestUtil.assertEquals(1, task.getCount());
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
}
