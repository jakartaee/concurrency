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

package ee.jakarta.tck.concurrent.api.ManageableThread;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.common.fixed.counter.CounterRunnableTask;
import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common.PACKAGE;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManageableThread;
import jakarta.enterprise.concurrent.ManagedThreadFactory;

@Web
@Common( { PACKAGE.FIXED_COUNTER } )
public class ManageableThreadTests {
	
	//TODO deploy as EJB and JSP artifacts
	@Deployment(name="ManageableThreadTests")
	public static WebArchive createDeployment() {
		return ShrinkWrap.create(WebArchive.class)
				.addPackages(true,  ManageableThreadTests.class.getPackage());
	}
	
    @Resource(lookup = TestConstants.DefaultManagedThreadFactory)
    public ManagedThreadFactory threadFactory;

	/*
	 * @testName: isShutdown
	 * 
	 * @assertion_ids: CONCURRENCY:JAVADOC:20;CONCURRENCY:SPEC:99.1;
	 * 
	 * @test_Strategy: Lookup default ManagedThreadFactory object and create new
	 * thread. Check return value of method isShutdown of new thread.
	 */
	@Test
	public void isShutdown() {
	    ManageableThread m = (ManageableThread) threadFactory.newThread(new CounterRunnableTask());
	    assertFalse(m.isShutdown());
	}
}
