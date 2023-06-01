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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.framework.TestLogger;
import ee.jakarta.tck.concurrent.framework.TestUtil;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;
import jakarta.enterprise.concurrent.ManageableThread;

@Web
public class ManageableThreadTests {

	private static final TestLogger log = TestLogger.get(ManageableThreadTests.class);
	
	//TODO deploy as EJB and JSP artifacts
	@Deployment(name="ManageableThreadTests")
	public static WebArchive createDeployment() {
		return ShrinkWrap.create(WebArchive.class)
				.addPackages(true,  ManageableThreadTests.class.getPackage());
	}

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
		boolean pass = false;
		try {
			ManageableThread m = (ManageableThread) TestUtil.getManagedThreadFactory().newThread(new TestRunnableWork());
			pass = !m.isShutdown();
		} catch (Exception e) {
			log.warning("Unexpected Exception Caught", e);
		}
		assertTrue(pass);
	}
}
