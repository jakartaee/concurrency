/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.concurrent.api.ManagedScheduledExecutorService;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.framework.TestClient;

public class ManagedScheduledExecutorServiceTests extends TestClient {
	
	@ArquillianResource
	URL baseURL;
	
	//TODO deploy as EJB and JSP artifacts
	@Deployment(name="ManagedScheduledExecutorServiceTests", testable=false)
	public static WebArchive createDeployment() {
		return ShrinkWrap.create(WebArchive.class)
				.addPackages(true, getFrameworkPackage(), getCommonPackage(), ManagedScheduledExecutorServiceTests.class.getPackage())
				.addAsWebInfResource(ManagedScheduledExecutorServiceTests.class.getPackage(), "web.xml", "web.xml");
	}
	
	@Override
	protected String getServletPath() {
		return "ManagedScheduledExecutorServiceServlet";
	}


	/*
	 * @testName: normalScheduleProcess1Test
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:30;CONCURRENCY:SPEC:42;
	 * CONCURRENCY:SPEC:42.2;CONCURRENCY:SPEC:43;CONCURRENCY:SPEC:43.1;
	 * CONCURRENCY:SPEC:49;CONCURRENCY:SPEC:51; CONCURRENCY:SPEC:54;
	 * 
	 * @test_Strategy: Creates and executes a task based on a Trigger. The Trigger
	 * determines when the task should run and how often.
	 */
	@Test
	public void normalScheduleProcess1Test() {
		runTest(baseURL);
	}

	/*
	 * @testName: nullCommandScheduleProcessTest
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:32
	 * 
	 * @test_Strategy: if command is null.
	 */
	@Test
	public void nullCommandScheduleProcessTest() {
		runTest(baseURL);
	}

	/*
	 * @testName: normalScheduleProcess2Test
	 * 
	 * @assertion_ids:
	 * CONCURRENCY:JAVADOC:33;CONCURRENCY:SPEC:43;CONCURRENCY:SPEC:43.2;
	 * CONCURRENCY:SPEC:54;CONCURRENCY:SPEC:52;
	 *
	 * 
	 * @test_Strategy: Creates and executes a task based on a Trigger. The Trigger
	 * determines when the task should run and how often.
	 */
	@Test
	public void normalScheduleProcess2Test() {
		runTest(baseURL);
	}

	/*
	 * @testName: nullCallableScheduleProcessTest
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:35
	 * 
	 * @test_Strategy: if callable is null.
	 */
	@Test
	public void nullCallableScheduleProcessTest() {
		runTest(baseURL);
	}

}
