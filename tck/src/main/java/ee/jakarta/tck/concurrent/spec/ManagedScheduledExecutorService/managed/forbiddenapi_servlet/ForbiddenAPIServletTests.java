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

package jakarta.enterprise.concurrent.spec.ManagedScheduledExecutorService.managed.forbiddenapi_servlet;

import jakarta.enterprise.concurrent.util.TestClient;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jakarta.enterprise.concurrent.common.ConcurrencyTestUtils;

public class ForbiddenAPIServletTests extends TestClient {

	/**
	 * @class.setup_props: all.props; all properties;
	 */
	@BeforeClass // TODO BeforeClass or BeforeTest
	public void setup() {
		loadServerProperties();
		setURLContext("/forbiddenapi_web/testServlet");
	}

	/*
	 * @testName: testAwaitTermination
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:57.1
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void testAwaitTermination() {
		try {
			ConcurrencyTestUtils.sendClientRequest2Url(HTTP, host, port, getURLContext(),
					ConcurrencyTestUtils.SERVLET_OP_FORBIDDENAPI_TESTAWAITTERMINATION);
		} catch (Exception e) {
			fail(e);
		}
	}

	/*
	 * @testName: testIsShutdown
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:57.2
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void testIsShutdown() {
		try {
			ConcurrencyTestUtils.sendClientRequest2Url(HTTP, host, port, getURLContext(),
					ConcurrencyTestUtils.SERVLET_OP_FORBIDDENAPI_TESTISSHUTDOWN);
		} catch (Exception e) {
			fail(e);
		}
	}

	/*
	 * @testName: testIsTerminated
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:57.3
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void testIsTerminated() {
		try {
			ConcurrencyTestUtils.sendClientRequest2Url(HTTP, host, port, getURLContext(),
					ConcurrencyTestUtils.SERVLET_OP_FORBIDDENAPI_TESTISTERMINATED);
		} catch (Exception e) {
			fail(e);
		}
	}

	/*
	 * @testName: testShutdown
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:57.4
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void testShutdown() {
		try {
			ConcurrencyTestUtils.sendClientRequest2Url(HTTP, host, port, getURLContext(),
					ConcurrencyTestUtils.SERVLET_OP_FORBIDDENAPI_TESTSHUTDOWN);
		} catch (Exception e) {
			fail(e);
		}
	}

	/*
	 * @testName: testShutdownNow
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:57.5
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void testShutdownNow() {
		try {
			ConcurrencyTestUtils.sendClientRequest2Url(HTTP, host, port, getURLContext(),
					ConcurrencyTestUtils.SERVLET_OP_FORBIDDENAPI_TESTSHUTDOWNNOW);
		} catch (Exception e) {
			fail(e);
		}
	}
}
