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

package ee.jakarta.tck.concurrent.spec.ManagedThreadFactory.apitests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.common.fixed.counter.CounterRunnableTask;
import ee.jakarta.tck.concurrent.common.fixed.counter.StaticCounter;
import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.framework.TestUtil;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common.PACKAGE;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManageableThread;
import jakarta.enterprise.concurrent.ManagedThreadFactory;

@Web
@Common({PACKAGE.TASKS, PACKAGE.FIXED_COUNTER})
public class APITests extends TestClient {
	
	@Deployment(name="APITests")
	public static WebArchive createDeployment() {
		return ShrinkWrap.create(WebArchive.class)
				.addPackages(true, APITests.class.getPackage());
	}
	

    @Resource
    public ManagedThreadFactory factory;
	

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
	    CounterRunnableTask task = new CounterRunnableTask();
        Thread thread = factory.newThread(task);
        thread.start();
        thread.interrupt();
        TestUtil.waitTillThreadFinish(thread);
        assertEquals(StaticCounter.getCount(), 0);
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
	    CounterRunnableTask task = new CounterRunnableTask();
        Thread thread = factory.newThread(task);
        assertTrue(thread instanceof ManageableThread, "The thread returned by ManagedThreadFactory should be instance of ManageableThread.");
	}

}
