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

package jakarta.enterprise.concurrent.spec.ContextService.contextPropagate;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.testng.annotations.Test;

import jakarta.enterprise.concurrent.tck.framework.TestClient;
import jakarta.enterprise.concurrent.tck.framework.TestUtil;
import jakarta.enterprise.concurrent.tck.framework.URLBuilder;

public class ContextPropagationTests extends TestClient {
	
	@ArquillianResource
	URL baseURL;

	/*
	 * @testName: testJNDIContextAndCreateProxyInServlet
	 *
	 * @assertion_ids:
	 * CONCURRENCY:SPEC:85;CONCURRENCY:SPEC:76;CONCURRENCY:SPEC:76.1;
	 * CONCURRENCY:SPEC:76.2;CONCURRENCY:SPEC:76.3;CONCURRENCY:SPEC:77;
	 * CONCURRENCY:SPEC:84;CONCURRENCY:SPEC:2;CONCURRENCY:SPEC:4.1;
	 *
	 * @test_Strategy: create proxy in servlet and pass it into ejb container, then
	 * verify JNDI Context.
	 *
	 */
	@Test
	public void testJNDIContextAndCreateProxyInServlet() {
		URL url;
		String resp = null;
		try {
			url = URLBuilder.get().withBaseURL(baseURL).withPaths("JNDIServlet").withQueries("action=createProxyInServlet").withTestName("service").build();
			resp = TestUtil.getResponse(url.openConnection());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("++ get response: " + resp);
		assertEquals(testName + "failed to get correct result.", "JNDIContextWeb", // expected
				resp.trim()); // actual
	}

	/*
	 * @testName: testJNDIContextAndCreateProxyInEJB
	 *
	 * @assertion_ids:
	 * CONCURRENCY:SPEC:85;CONCURRENCY:SPEC:76;CONCURRENCY:SPEC:76.1;
	 * CONCURRENCY:SPEC:76.2;CONCURRENCY:SPEC:76.3;CONCURRENCY:SPEC:77;
	 * CONCURRENCY:SPEC:84;CONCURRENCY:SPEC:3;CONCURRENCY:SPEC:3.1;
	 * CONCURRENCY:SPEC:3.2;CONCURRENCY:SPEC:3.3;CONCURRENCY:SPEC:3.4;
	 * CONCURRENCY:SPEC:4;
	 *
	 * @test_Strategy: create proxy in servlet and pass it into ejb container, then
	 * verify JNDI Context.
	 *
	 */
	@Test
	public void testJNDIContextAndCreateProxyInEJB() {
		URL url;
		String resp = null;
		try {
			url = URLBuilder.get().withBaseURL(baseURL).withPaths("JNDIServlet").withQueries("action=createProxyInEJB").withTestName("service").build();
			resp = TestUtil.getResponse(url.openConnection());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("++ get response: " + resp);
		assertEquals(testName + "failed to get correct result.", "JNDIContextWeb", // expected
				resp.trim()); // actual
	}

	/*
	 * @testName: testClassloaderAndCreateProxyInServlet
	 *
	 * @assertion_ids:
	 * CONCURRENCY:SPEC:85;CONCURRENCY:SPEC:76;CONCURRENCY:SPEC:76.1;
	 * CONCURRENCY:SPEC:76.2;CONCURRENCY:SPEC:76.3;CONCURRENCY:SPEC:77;
	 * CONCURRENCY:SPEC:84;CONCURRENCY:SPEC:4.2;CONCURRENCY:SPEC:4.4;
	 *
	 * @test_Strategy: create proxy in servlet and pass it into ejb container, then
	 * verify classloader.
	 *
	 */
	@Test
	public void testClassloaderAndCreateProxyInServlet() {
		URL url;
		String resp = null;
		try {
			url = URLBuilder.get().withBaseURL(baseURL).withPaths("ClassloaderServlet").withTestName("service").build();
			resp = TestUtil.getResponse(url.openConnection());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("++ get response: " + resp);
		assertEquals(testName + "failed to get correct result.", "success", // expected
				resp.trim()); // actual
	}

	/*
	 * @testName: testSecurityAndCreateProxyInServlet
	 *
	 * @assertion_ids:
	 * CONCURRENCY:SPEC:85;CONCURRENCY:SPEC:76;CONCURRENCY:SPEC:76.1;
	 * CONCURRENCY:SPEC:76.2;CONCURRENCY:SPEC:76.3;CONCURRENCY:SPEC:77;
	 * CONCURRENCY:SPEC:84;CONCURRENCY:SPEC:4.3;CONCURRENCY:SPEC:4.4;
	 * CONCURRENCY:SPEC:4.4;
	 *
	 * @test_Strategy: create proxy in servlet and pass it into ejb container, then
	 * verify permission.
	 *
	 */
	@Test
	public void testSecurityAndCreateProxyInServlet() {
		URL url;
		String resp = null;
		try {
			url = URLBuilder.get().withBaseURL(baseURL).withPaths("SecurityServlet").withTestName("service").build();
			resp = TestUtil.getResponse(url.openConnection());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("++ get response: " + resp);
		assertEquals(testName + "failed to get correct result.", "success", // expected
				resp.trim()); // actual
	}
}
