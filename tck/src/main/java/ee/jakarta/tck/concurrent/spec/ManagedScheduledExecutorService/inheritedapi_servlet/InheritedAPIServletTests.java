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

package jakarta.enterprise.concurrent.spec.ManagedScheduledExecutorService.inheritedapi_servlet;

import jakarta.enterprise.concurrent.util.TestClient;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jakarta.enterprise.concurrent.common.ConcurrencyTestUtils;

public class InheritedAPIServletTests extends TestClient {

	/**
	 * @class.setup_props: all.props; all properties;
	 */
	@BeforeClass // TODO BeforeClass or BeforeTest
	public void setup() {
		loadServerProperties();
		setURLContext("/inheritedapi_web/testServlet");
	}

	/*
	 * @testName: testApiSubmit
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:44.1
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void testApiSubmit() {
		try {
			ConcurrencyTestUtils.sendClientRequest2Url(HTTP, host, port, getURLContext(),
					ConcurrencyTestUtils.SERVLET_OP_INHERITEDAPI_TESTAPISUBMIT);
		} catch (Exception e) {
			fail(e);
		}
	}

	/*
	 * @testName: testApiExecute
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:44.2
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void testApiExecute() {
		try {
			ConcurrencyTestUtils.sendClientRequest2Url(HTTP, host, port, getURLContext(),
					ConcurrencyTestUtils.SERVLET_OP_INHERITEDAPI_TESTAPIEXECUTE);
		} catch (Exception e) {
			fail(e);
		}
	}

	/*
	 * @testName: testApiInvokeAll
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:44.3
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void testApiInvokeAll() {
		try {
			ConcurrencyTestUtils.sendClientRequest2Url(HTTP, host, port, getURLContext(),
					ConcurrencyTestUtils.SERVLET_OP_INHERITEDAPI_TESTAPIINVOKEALL);
		} catch (Exception e) {
			fail(e);
		}
	}

	/*
	 * @testName: testApiInvokeAny
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:44.4
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void testApiInvokeAny() {
		try {
			ConcurrencyTestUtils.sendClientRequest2Url(HTTP, host, port, getURLContext(),
					ConcurrencyTestUtils.SERVLET_OP_INHERITEDAPI_TESTAPIINVOKEANY);
		} catch (Exception e) {
			fail(e);
		}
	}

	/*
	 * @testName: testApiSchedule
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:44.5
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void testApiSchedule() {
		try {
			ConcurrencyTestUtils.sendClientRequest2Url(HTTP, host, port, getURLContext(),
					ConcurrencyTestUtils.SERVLET_OP_INHERITEDAPI_TESTAPISCHEDULE);
		} catch (Exception e) {
			fail(e);
		}
	}

	/*
	 * @testName: testApiScheduleAtFixedRate
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:44.6
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void testApiScheduleAtFixedRate() {
		try {
			ConcurrencyTestUtils.sendClientRequest2Url(HTTP, host, port, getURLContext(),
					ConcurrencyTestUtils.SERVLET_OP_INHERITEDAPI_TESTAPISCHEDULEATFIXEDRATE);
		} catch (Exception e) {
			fail(e);
		}
	}

	/*
	 * @testName: testApiScheduleWithFixedDelay
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:44.7
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void testApiScheduleWithFixedDelay() {
		try {
			ConcurrencyTestUtils.sendClientRequest2Url(HTTP, host, port, getURLContext(),
					ConcurrencyTestUtils.SERVLET_OP_INHERITEDAPI_TESTAPISCHEDULEWITHFIXEDDELAY);
		} catch (Exception e) {
			fail(e);
		}
	}

}
