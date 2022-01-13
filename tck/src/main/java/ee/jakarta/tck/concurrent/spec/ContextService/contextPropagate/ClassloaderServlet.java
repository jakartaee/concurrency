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

package ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate;

import java.io.Serializable;

import ee.jakarta.tck.concurrent.framework.TestServlet;
import ee.jakarta.tck.concurrent.framework.TestUtil;
import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("ClassloaderServlet")
public class ClassloaderServlet extends TestServlet {

	@EJB
	private ContextPropagateInterface intf;	

	public void testClassloaderAndCreateProxyInServlet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		String result = intf.executeWorker((TestWorkInterface) TestUtil.getContextService().createContextualProxy(
				new TestClassloaderRunnableWork(), Runnable.class, TestWorkInterface.class, Serializable.class));
		resp.getWriter().println(result);
	}
}
