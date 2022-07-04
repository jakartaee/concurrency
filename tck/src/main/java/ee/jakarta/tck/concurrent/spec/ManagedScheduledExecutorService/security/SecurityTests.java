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

package ee.jakarta.tck.concurrent.spec.ManagedScheduledExecutorService.security;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import ee.jakarta.tck.concurrent.framework.TestClient;

public class SecurityTests extends TestClient {
	public static final String SecurityEJBJNDI = "java:global/security/security_ejb/SecurityTestEjb";
	
	@ArquillianResource(SecurityServlet.class)
	URL baseURL;
	
	@Deployment(name="SecurityTests", testable=false)
	public static WebArchive createDeployment() {

		JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "security_ejb.jar")
				.addClasses(SecurityTestLocal.class, SecurityTestEjb.class);

		WebArchive war = ShrinkWrap.create(WebArchive.class, "security_web.war")
				.addPackages(true, getFrameworkPackage(), getCommonPackage(), SecurityTests.class.getPackage())
				//SecurityTestEjb and SecurityTestRemote are in the jar
				.deleteClasses(SecurityTestLocal.class, SecurityTestEjb.class)
				.addAsLibrary(jar)
				;
				//TODO document how users can dynamically inject vendor specific deployment descriptors into this archive
		
		return war;
	}
	
	@Override
	protected String getServletPath() {
		return "SecurityServlet";
	}

	/*
	 * @testName: managedScheduledExecutorServiceAPISecurityTest
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:4.3; CONCURRENCY:SPEC:50;
	 * 
	 * @test_Strategy: login in a servlet with username "javajoe(in role manager)",
	 * then submit a task by ManagedScheduledExecutorService in which call a ejb
	 * that requires role manager.
         *
         * Accepted TCK challenge: https://github.com/jakartaee/concurrency/issues/227
         * fix: https://github.com/jakartaee/concurrency/pull/221
         * Can be reenabled in the next release of Jakarta Concurrency
	 */
	@Test(enabled = false)
	public void managedScheduledExecutorServiceAPISecurityTest() {
		runTest(baseURL);
	}

}
