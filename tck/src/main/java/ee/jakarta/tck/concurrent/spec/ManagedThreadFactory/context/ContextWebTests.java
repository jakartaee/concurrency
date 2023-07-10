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

package ee.jakarta.tck.concurrent.spec.ManagedThreadFactory.context;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common.PACKAGE;
import ee.jakarta.tck.concurrent.framework.junit.anno.TestName;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;

@Web @RunAsClient //Requires client testing due to login request 
@Common({PACKAGE.TASKS})
public class ContextWebTests extends TestClient {
	
	@ArquillianResource
	URL baseURL;
	
	@Deployment(name="ContextTests")
	public static WebArchive createDeployment() {
		WebArchive war = ShrinkWrap.create(WebArchive.class)
				.addPackages(true, ContextWebTests.class.getPackage())
				.addAsWebInfResource(ContextWebTests.class.getPackage(), "web.xml", "web.xml");
		
		return war;
	}
	
    @TestName
    String testname;
	
	@Override
	protected String getServletPath() {
		return "SecurityServlet";
	}
	
	/*
	 * @testName: jndiClassloaderPropagationTest
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:96.7; CONCURRENCY:SPEC:100;
	 * CONCURRENCY:SPEC:106;
	 * 
	 * @test_Strategy:
	 */
	@Test
	public void jndiClassloaderPropagationTest() {
		runTest(baseURL, testname);
	}
	
	@Test
	public void jndiClassloaderPropagationWithSecurityTest() {
		runTest(baseURL, testname);
	}

}
