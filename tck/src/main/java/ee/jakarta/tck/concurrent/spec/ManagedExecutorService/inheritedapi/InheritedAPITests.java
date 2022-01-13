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

package ee.jakarta.tck.concurrent.spec.ManagedExecutorService.inheritedapi;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import ee.jakarta.tck.concurrent.framework.TestClient;

public class InheritedAPITests extends TestClient {
	
	@ArquillianResource
	URL baseURL;
	
	@Deployment(name="InheritedAPITests", testable=false)
	public static WebArchive createDeployment() {
		return ShrinkWrap.create(WebArchive.class)
				.addPackages(true, getFrameworkPackage(), InheritedAPITests.class.getPackage());
	}
	
	@Override
	protected String getServletPath() {
		return "CommonServlet";
	}

	/*
	 * @testName: testBasicManagedExecutorService
	 * 
	 * @assertion_ids:
	 * CONCURRENCY:SPEC:10.2;CONCURRENCY:SPEC:13;CONCURRENCY:SPEC:13.1;CONCURRENCY
	 * :SPEC:13.2;
	 * CONCURRENCY:SPEC:14;CONCURRENCY:SPEC:14.1;CONCURRENCY:SPEC:14.2;CONCURRENCY
	 * :SPEC:14.3;
	 * CONCURRENCY:SPEC:14.4;CONCURRENCY:SPEC:6.1;CONCURRENCY:SPEC:6.2;CONCURRENCY
	 * :SPEC:8;
	 * CONCURRENCY:SPEC:8.1;CONCURRENCY:SPEC:9;CONCURRENCY:SPEC:10;CONCURRENCY:
	 * SPEC:10.2; CONCURRENCY:SPEC:12;CONCURRENCY:SPEC:19;CONCURRENCY:SPEC:27;
	 * 
	 * @test_Strategy: test basic function for ManagedExecutorService include
	 * execute, submit, invokeAny, invokeAll, atMostOnce
	 */
	
	@Test
	public void testExecute() {
		runTest(baseURL);
	}

	@Test
	public void testSubmit() {
		runTest(baseURL);
	}

	@Test
	public void testInvokeAny() {
		runTest(baseURL);
	}

	@Test
	public void testInvokeAll() {
		runTest(baseURL);
	}

	@Test
	public void testAtMostOnce() {
		runTest(baseURL);
	}
}
