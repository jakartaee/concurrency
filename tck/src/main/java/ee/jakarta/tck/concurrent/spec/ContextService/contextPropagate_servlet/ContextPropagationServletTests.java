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

package jakarta.enterprise.concurrent.spec.ContextService.contextPropagate_servlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jakarta.enterprise.concurrent.util.TestClient;
import jakarta.enterprise.concurrent.util.TestUtil;

public class ContextPropagationServletTests extends TestClient {

	/*
	 * @class.setup_props: webServerHost; webServerPort; ts_home; all.props; all
	 * properties;
	 *
	 */
	@BeforeClass // TODO BeforeClass or BeforeTest
	public void setup() {
		loadServerProperties();
		setURLContext("/ContextPropagate_servlet1_web");
	}

	/*
	 * @testName: testJNDIContextInServlet
	 *
	 * @assertion_ids:
	 * CONCURRENCY:SPEC:85;CONCURRENCY:SPEC:76;CONCURRENCY:SPEC:76.1;
	 * CONCURRENCY:SPEC:76.2;CONCURRENCY:SPEC:76.3;CONCURRENCY:SPEC:77;
	 * CONCURRENCY:SPEC:78;CONCURRENCY:SPEC:82;CONCURRENCY:SPEC:84;
	 *
	 * @test_Strategy: create proxy in servlet and pass it to other servlet in other
	 * web module, then verify JNDI Context.
	 *
	 */
	@Test
	public void testJNDIContextInServlet() {
		URL url;
		String resp = null;
		try {
			url = new URL(
					"http://" + host + ":" + port + getURLContext() + "/ProxyCreatorServlet?action=createJNDIWork");
			resp = TestUtil.getResponse(url.openConnection());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("++ get response: " + resp);
		assertNotNull("Response should not be null", resp);
		assertEquals(testName + " failed to get correct result.", "JNDIContextWeb", // expected
				resp.trim()); // actual
	}

	/*
	 * @testName: testClassloaderInServlet
	 *
	 * @assertion_ids:
	 * CONCURRENCY:SPEC:85;CONCURRENCY:SPEC:76;CONCURRENCY:SPEC:76.1;
	 * CONCURRENCY:SPEC:76.2;CONCURRENCY:SPEC:76.3;CONCURRENCY:SPEC:77;
	 * CONCURRENCY:SPEC:78;CONCURRENCY:SPEC:82;CONCURRENCY:SPEC:84;
	 *
	 * @test_Strategy: create proxy in servlet and pass it into other servlet in
	 * other web module, then verify classloader.
	 *
	 */
	@Test
	public void testClassloaderInServlet() {
		URL url;
		String resp = null;
		try {
			url = new URL("http://" + host + ":" + port + getURLContext()
					+ "/ProxyCreatorServlet?action=createClassloaderWork");
			resp = TestUtil.getResponse(url.openConnection());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("++ get response: " + resp);
		assertNotNull("Response should not be null", resp);
		assertEquals(testName + " failed to get correct result.", "success", // expected
				resp.trim()); // actual
	}
}
