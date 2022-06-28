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

package ee.jakarta.tck.concurrent.spec.ManagedExecutorService.security;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import ee.jakarta.tck.concurrent.framework.TestClient;

public class SecurityTests extends TestClient {
	
	public static final String SecurityEJBJNDI = "java:global/security/security_ejb/SecurityTestEjb";
	
	@ArquillianResource
	URL baseURL;
	
	@Deployment(name="SecurityTests", testable=false)
	public static EnterpriseArchive createDeployment() {
		WebArchive war = ShrinkWrap.create(WebArchive.class, "security_web.war")
				.addPackages(true, getFrameworkPackage(), getCommonPackage(), SecurityTests.class.getPackage())
				.deleteClasses(SecurityTestLocal.class, SecurityTestEjb.class); //SecurityTestEjb and SecurityTestRemote are in the jar;
		
		JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "security_ejb.jar")
				.addClasses(SecurityTestLocal.class, SecurityTestEjb.class);
		
		EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "security.ear")
				.addAsModules(war, jar);
				//TODO document how users can dynamically inject vendor specific deployment descriptors into this archive
		
		return ear;
	}
	
	@Override
	protected String getServletPath() {
		return "SecurityServlet";
	}

	/*
	 * @testName: managedExecutorServiceAPISecurityTest
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:4.3; CONCURRENCY:SPEC:50;
	 * CONCURRENCY:SPEC:85; CONCURRENCY:SPEC:96.6; CONCURRENCY:SPEC:106;
	 * CONCURRENCY:SPEC:22;
	 * 
	 * @test_Strategy: login in a servlet with username "javajoe(in role manager)",
	 * then submit a task by ManagedExecutorService in which call a ejb that
	 * requires role manager.
         *
         * Accepted TCK challenge: https://github.com/jakartaee/concurrency/issues/227
         * fix: https://github.com/jakartaee/concurrency/pull/218  
         * Can be reenabled in next release of Concurrency
	 */
	@Test(enabled = false)
	public void managedExecutorServiceAPISecurityTest() {
		runTest(baseURL);
	}

}
