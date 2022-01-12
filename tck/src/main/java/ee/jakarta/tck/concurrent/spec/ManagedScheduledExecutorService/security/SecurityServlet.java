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

package ee.jakarta.tck.concurrent.spec.ManagedScheduledExecutorService.security;

import static org.testng.Assert.assertEquals;

import java.util.concurrent.ScheduledFuture;

import javax.naming.InitialContext;

import ee.jakarta.tck.concurrent.common.CommonTriggers;
import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import ee.jakarta.tck.concurrent.framework.TestUtil;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("SecurityServlet")
public class SecurityServlet extends TestServlet {

	public void managedScheduledExecutorServiceAPISecurityTest(HttpServletRequest req, HttpServletResponse res) throws Exception {
			req.login("javajoe", "javajoe");
			
			InitialContext context = new InitialContext();
			ManagedScheduledExecutorService executorService = (ManagedScheduledExecutorService) context
					.lookup(TestConstants.DefaultManagedScheduledExecutorService);
			ScheduledFuture future = executorService.schedule(new SecurityTestTask(), new CommonTriggers.OnceTrigger());

			Object result = TestUtil.waitForTaskComplete(future);
			assertEquals(result, TestConstants.SimpleReturnValue);
	}
}
