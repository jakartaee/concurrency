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

package jakarta.enterprise.concurrent.spec.ContextService.contextPropagate;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import jakarta.enterprise.concurrent.tck.framework.TestClient;
import jakarta.enterprise.concurrent.tck.framework.TestConstants;
import jakarta.enterprise.concurrent.tck.framework.URLBuilder;

public class ContextPropagationTests extends TestClient {
	
	public static final String LimitedBeanAppJNDI = "java:app/ContextPropagate_ejb/LimitedBean";
	
	@Deployment(name="ContextService.contextPropagate", testable=false)
	public static EnterpriseArchive createDeployment() {
		
		WebArchive war = ShrinkWrap.create(WebArchive.class, "ContextPropagate.war")
				.addPackages(true, getFrameworkPackage(), ContextPropagationTests.class.getPackage())
				.deleteClass(ContextPropagateBean.class)
				.addAsWebInfResource(ContextPropagationTests.class.getPackage(), "web.xml", "web.xml");
		
		JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "ContextPropagate_ejb.jar")
				.addPackages(true, getFrameworkPackage(), ContextPropagationTests.class.getPackage())
				.deleteClasses(ClassloaderServlet.class, JNDIServlet.class, SecurityServlet.class)
				.addAsManifestResource(ContextPropagationTests.class.getPackage(), "ejb-jar.xml", "ejb-jar.xml")
				.addAsManifestResource(ContextPropagationTests.class.getPackage(), "sun-ejb-jar.xml", "sun-ejb-jar.xml");
		
		EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "ContextPropagate.ear").addAsModules(war, jar);
		
		return ear;
	}
	
	@ArquillianResource(JNDIServlet.class)
	URL jndiURL;
	
	@ArquillianResource(ClassloaderServlet.class)
	URL classloaderURL;
	
	@ArquillianResource(SecurityServlet.class)
	URL securityURL;

	/*
	 * @testName: testJNDIContextAndCreateProxyInServlet
	 *
	 * @assertion_ids:
	 * CONCURRENCY:SPEC:85;CONCURRENCY:SPEC:76;CONCURRENCY:SPEC:76.1;
	 * CONCURRENCY:SPEC:76.2;CONCURRENCY:SPEC:76.3;CONCURRENCY:SPEC:77;
	 * CONCURRENCY:SPEC:84;CONCURRENCY:SPEC:2;CONCURRENCY:SPEC:4.1;
	 *
	 * @test_Strategy: create proxy in servlet and pass it into ejb container, then
	 * verify JNDI Context.
	 *
	 */
	@Test
	public void testJNDIContextAndCreateProxyInServlet() {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(jndiURL).withPaths("JNDIServlet").withTestName(testName);
		String resp = runTestWithResponse(requestURL, null);
		this.assertStringInResponse(testName + "failed to get correct result.", "JNDIContextWeb", resp);
	}

	/*
	 * @testName: testJNDIContextAndCreateProxyInEJB
	 *
	 * @assertion_ids:
	 * CONCURRENCY:SPEC:85;CONCURRENCY:SPEC:76;CONCURRENCY:SPEC:76.1;
	 * CONCURRENCY:SPEC:76.2;CONCURRENCY:SPEC:76.3;CONCURRENCY:SPEC:77;
	 * CONCURRENCY:SPEC:84;CONCURRENCY:SPEC:3;CONCURRENCY:SPEC:3.1;
	 * CONCURRENCY:SPEC:3.2;CONCURRENCY:SPEC:3.3;CONCURRENCY:SPEC:3.4;
	 * CONCURRENCY:SPEC:4;
	 *
	 * @test_Strategy: create proxy in servlet and pass it into ejb container, then
	 * verify JNDI Context.
	 *
	 */
	@Test
	public void testJNDIContextAndCreateProxyInEJB() {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(jndiURL).withPaths("JNDIServlet").withTestName(testName);
		String resp = runTestWithResponse(requestURL, null);
		this.assertStringInResponse(testName + "failed to get correct result.", "JNDIContextEJB", resp);
	}

	/*
	 * @testName: testClassloaderAndCreateProxyInServlet
	 *
	 * @assertion_ids:
	 * CONCURRENCY:SPEC:85;CONCURRENCY:SPEC:76;CONCURRENCY:SPEC:76.1;
	 * CONCURRENCY:SPEC:76.2;CONCURRENCY:SPEC:76.3;CONCURRENCY:SPEC:77;
	 * CONCURRENCY:SPEC:84;CONCURRENCY:SPEC:4.2;CONCURRENCY:SPEC:4.4;
	 *
	 * @test_Strategy: create proxy in servlet and pass it into ejb container, then
	 * verify classloader.
	 *
	 */
	@Test
	public void testClassloaderAndCreateProxyInServlet() {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(securityURL).withPaths("ClassloaderServlet").withTestName(testName);
		String resp = runTestWithResponse(requestURL, null);
		this.assertStringInResponse(testName + "failed to get correct result.", TestConstants.ComplexReturnValue, resp);
	}

	/*
	 * @testName: testSecurityAndCreateProxyInServlet
	 *
	 * @assertion_ids:
	 * CONCURRENCY:SPEC:85;CONCURRENCY:SPEC:76;CONCURRENCY:SPEC:76.1;
	 * CONCURRENCY:SPEC:76.2;CONCURRENCY:SPEC:76.3;CONCURRENCY:SPEC:77;
	 * CONCURRENCY:SPEC:84;CONCURRENCY:SPEC:4.3;CONCURRENCY:SPEC:4.4;
	 * CONCURRENCY:SPEC:4.4;
	 *
	 * @test_Strategy: create proxy in servlet and pass it into ejb container, then
	 * verify permission.
	 *
	 */
	@Test
	public void testSecurityAndCreateProxyInServlet() {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(classloaderURL).withPaths("SecurityServlet").withTestName(testName);
		String resp = runTestWithResponse(requestURL, null);
		this.assertStringInResponse(testName + "failed to get correct result.", TestConstants.ComplexReturnValue, resp);
	}
}
