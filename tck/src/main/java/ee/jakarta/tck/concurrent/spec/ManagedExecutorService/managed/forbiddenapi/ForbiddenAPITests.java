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

package jakarta.enterprise.concurrent.spec.ManagedExecutorService.managed.forbiddenapi;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.annotations.Test;

import jakarta.ejb.EJB;
import jakarta.enterprise.concurrent.tck.framework.TestClient;

public class ForbiddenAPITests extends TestClient {
	
	@Deployment(name="ManagedExecutorService.managed.forbiddenapi")
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create(JavaArchive.class)
				.addPackages(true, getFrameworkPackage(), ForbiddenAPITests.class.getPackage())
				.addAsManifestResource(ForbiddenAPITests.class.getPackage(), "sun-ejb-jar.xml", "sun-ejb-jar.xml");
	}

	@EJB
	private TestEjbRemote testEjb;

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
		testEjb.testAwaitTermination();
	}

	/*
	 * @testName: testIsShutdown
	 * 
	 * @assertion_ids:
	 * 
	 * CONCURRENCY:SPEC:23;CONCURRENCY:SPEC:24;CONCURRENCY:SPEC:24.2;
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void testIsShutdown() {
		testEjb.testIsShutdown();
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
		testEjb.testIsTerminated();
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
		testEjb.testShutdown();
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
		testEjb.testShutdownNow();
	}
}
