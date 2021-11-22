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

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import jakarta.enterprise.concurrent.tck.framework.TestClient;

public class InheritedAPIServletTests extends TestClient {
	
	@ArquillianResource
	URL baseURL;
	
	@Deployment(name="ManagedScheduledExecutorService.inheritedapi_servlet", testable=false)
	public static WebArchive createDeployment() {
		return ShrinkWrap.create(WebArchive.class)
				.addPackages(true, getFrameworkPackage(), getCommonPackage(), getCommonCounterPackage(), InheritedAPIServletTests.class.getPackage());
	}
	
	@Override
	protected String getServletPath() {
		return "InheritedAPIServlet";
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
		runTest(baseURL);
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
		runTest(baseURL);
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
		runTest(baseURL);
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
		runTest(baseURL);
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
		runTest(baseURL);
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
		runTest(baseURL);
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
		runTest(baseURL);
	}

}
