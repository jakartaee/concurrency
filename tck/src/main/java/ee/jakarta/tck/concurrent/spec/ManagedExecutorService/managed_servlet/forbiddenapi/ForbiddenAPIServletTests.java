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

package jakarta.enterprise.concurrent.spec.ManagedExecutorService.managed_servlet.forbiddenapi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jakarta.enterprise.concurrent.util.TestClient;
import jakarta.enterprise.concurrent.util.TestUtil;

public class ForbiddenAPIServletTests extends TestClient {

	/**
	 * @class.setup_props: all.props; all properties;
	 */
	@BeforeClass // TODO BeforeClass or BeforeTest
	public void setup() {
		loadServerProperties();
		setURLContext("/forbiddenapiTest_web");
	}

	/*
	 * @testName: testAwaitTermination
	 * 
	 * @assertion_ids:
	 * CONCURRENCY:SPEC:23;CONCURRENCY:SPEC:24;CONCURRENCY:SPEC:24.1;
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void testAwaitTermination() {
		String res = request(Constants.OP_AWAITTERMINATION);
		checkResponse(res);
	}

	/*
	 * @testName: testIsShutdown
	 * 
	 * @assertion_ids:
	 * CONCURRENCY:SPEC:23;CONCURRENCY:SPEC:24;CONCURRENCY:SPEC:24.2;
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void testIsShutdown() {
		String res = request(Constants.OP_ISSHUTDOWN);
		checkResponse(res);
	}

	/*
	 * @testName: testIsTerminated
	 * 
	 * @assertion_ids:
	 * CONCURRENCY:SPEC:23;CONCURRENCY:SPEC:24;CONCURRENCY:SPEC:24.3;
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void testIsTerminated() {
		String res = request(Constants.OP_ISTERMINATED);
		checkResponse(res);
	}

	/*
	 * @testName: testShutdown
	 * 
	 * @assertion_ids:
	 * CONCURRENCY:SPEC:23;CONCURRENCY:SPEC:24;CONCURRENCY:SPEC:24.4;
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void testShutdown() {
		String res = request(Constants.OP_SHUTDOWN);
		checkResponse(res);
	}

	/*
	 * @testName: testShutdownNow
	 * 
	 * @assertion_ids:
	 * CONCURRENCY:SPEC:23;CONCURRENCY:SPEC:24;CONCURRENCY:SPEC:24.5;
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void testShutdownNow() {
		String res = request(Constants.OP_SHUTDOWNNOW);
		checkResponse(res);
	}

	private void checkResponse(String responseStr) {
		assertEquals(testName + " failed to get successful response.", Constants.SUCCESSMESSAGE, // expected
				responseStr); // actual
	}

	private String request(String operation) {
		String result = "";
		Properties prop = new Properties();
		URL url;
		try {
			url = new URL(HTTP, host, port, getURLContext() + Constants.SERVLET_TEST_URL);
			prop.put(Constants.OP_NAME, operation);
			URLConnection urlConn = TestUtil.sendPostData(prop, url);
			result = TestUtil.getResponse(urlConn);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

}
