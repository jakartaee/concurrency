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

package ee.jakarta.tck.concurrent.spec.ManagedThreadFactory.apitests;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import ee.jakarta.tck.concurrent.framework.TestClient;

public class APITests extends TestClient {
	
	@ArquillianResource
	URL baseURL;
	
	@Deployment(name="APITests", testable=false)
	public static WebArchive createDeployment() {
		return ShrinkWrap.create(WebArchive.class)
				.addPackages(true, getFrameworkPackage(), getCommonPackage(), APITests.class.getPackage());
	}
	
	@Override
	protected String getServletPath() {
		return "APIServlet";
	}

	/*
	 * @testName: interruptThreadApiTest
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:83; CONCURRENCY:SPEC:83.1;
	 * CONCURRENCY:SPEC:83.2; CONCURRENCY:SPEC:83.3; CONCURRENCY:SPEC:103;
	 * CONCURRENCY:SPEC:96.5; CONCURRENCY:SPEC:96.6; CONCURRENCY:SPEC:105;
	 * CONCURRENCY:SPEC:96; CONCURRENCY:SPEC:93; CONCURRENCY:SPEC:96.3;
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void interruptThreadApiTest() {
		runTest(baseURL);
	}

	/*
	 * @testName: implementsManageableThreadInterfaceTest
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:97;
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void implementsManageableThreadInterfaceTest() {
		runTest(baseURL);
	}

}
