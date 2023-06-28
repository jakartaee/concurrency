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

package ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Base64;
import java.util.Properties;

import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.TestLogger;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import ee.jakarta.tck.concurrent.framework.TestUtil;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("/ProxyCreatorServlet")
public class ProxyCreatorServlet extends TestServlet {
	
	private static final TestLogger log = TestLogger.get(ProxyCreatorServlet.class);
	
    @Resource(lookup = TestConstants.DefaultContextService)
    public ContextService context;

	public void testJNDIContextInServlet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		Object proxy = true;
		String result = null;
		String proxyURLString = req.getParameter("proxyURL");
		
		log.info("Proxy URL from parameter", proxyURLString);

		URL url = new URL(proxyURLString);
		
		proxy = context.createContextualProxy(new TestJNDIRunnableWork(),
				Runnable.class, TestWorkInterface.class, Serializable.class);

		Properties p = new Properties();
		p.setProperty("proxy", proxyToString(proxy));
		
		result = TestUtil.getResponse(TestUtil.sendPostData(url, p));
		resp.getWriter().println(result);
	}
	
	public void testClassloaderInServlet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		Object proxy = true;
		String result = null;
		String proxyURLString = req.getParameter("proxyURL");
		
		log.info("Proxy URL from parameter", proxyURLString);

		URL url = new URL(proxyURLString);

		proxy = context.createContextualProxy(new TestClassloaderRunnableWork(),
				Runnable.class, TestWorkInterface.class, Serializable.class);

		Properties p = new Properties();
		p.setProperty("proxy", proxyToString(proxy));
		
		result = TestUtil.getResponse(TestUtil.sendPostData(url, p));
		resp.getWriter().println(result);
	}

	private String proxyToString(Object proxy) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bout);
		out.writeObject(proxy);
		out.close();
		return Base64.getEncoder().encodeToString(bout.toByteArray());
	}
}
