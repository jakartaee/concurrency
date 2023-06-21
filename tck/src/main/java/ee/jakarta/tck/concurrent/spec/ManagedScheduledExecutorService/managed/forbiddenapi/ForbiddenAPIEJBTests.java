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

package ee.jakarta.tck.concurrent.spec.ManagedScheduledExecutorService.managed.forbiddenapi;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.framework.junit.anno.Common;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common.PACKAGE;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;
import jakarta.ejb.EJB;

@Web
@Common({PACKAGE.TASKS, PACKAGE.FIXED_COUNTER})
public class ForbiddenAPIEJBTests {
	
	private static final String APP_NAME = "ManagedScheduledExecutorService.ForbiddenAPITests";
	
	@Deployment(name="ForbiddenAPITests")
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create(JavaArchive.class)
				.addPackages(true, ForbiddenAPIEJBTests.class.getPackage());
	}

	@EJB
	private TestEjbInterface testEjb;

	/*
	 * @testName: testAwaitTermination
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:56;CONCURRENCY:SPEC:57.1
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
	 * @assertion_ids: CONCURRENCY:SPEC:56;CONCURRENCY:SPEC:57.2
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
	 * @assertion_ids: CONCURRENCY:SPEC:56;CONCURRENCY:SPEC:57.3
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
	 * @assertion_ids: CONCURRENCY:SPEC:56;CONCURRENCY:SPEC:57.4
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
	 * @assertion_ids: CONCURRENCY:SPEC:56;CONCURRENCY:SPEC:57.5
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void testShutdownNow() {
		testEjb.testShutdownNow();
	}
}
