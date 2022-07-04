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

package ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.URLBuilder;
import ee.jakarta.tck.concurrent.spi.context.IntContextProvider;
import ee.jakarta.tck.concurrent.spi.context.StringContextProvider;
import jakarta.enterprise.concurrent.spi.ThreadContextProvider;

public class ContextPropagationTests extends TestClient {

	public static final String LimitedBeanAppJNDI = "java:app/ContextPropagationTests_web/LimitedBean";

	
	@Deployment(name="ContextPropagationTests", testable=false)
	public static WebArchive createDeployment() {

		JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "ContextPropagationTests_ejb.jar")
				.addPackages(true, getFrameworkPackage(), ContextPropagationTests.class.getPackage())
				.deleteClasses(
						ContextServiceDefinitionServlet.class,
						ClassloaderServlet.class,
						JNDIServlet.class,
						SecurityServlet.class,
						JSPSecurityServlet.class,
						ContextServiceDefinitionFromEJBServlet.class)
				.addAsManifestResource(ContextPropagationTests.class.getPackage(), "ejb-jar.xml", "ejb-jar.xml");
		//TODO document how users can dynamically inject vendor specific deployment descriptors into this archive

		WebArchive war = ShrinkWrap.create(WebArchive.class, "ContextPropagationTests_web.war")
				.addPackages(true, getFrameworkPackage(), getContextPackage(), getContextProvidersPackage())
				.addClasses(
						ContextServiceDefinitionServlet.class,
						ClassloaderServlet.class,
						JNDIServlet.class,
						SecurityServlet.class,
						JSPSecurityServlet.class,
						ContextServiceDefinitionFromEJBServlet.class)
				.addAsServiceProvider(ThreadContextProvider.class.getName(), IntContextProvider.class.getName(), StringContextProvider.class.getName())
				.addAsWebInfResource(ContextPropagationTests.class.getPackage(), "web.xml", "web.xml")
				.addAsWebResource(ContextPropagationTests.class.getPackage(), "jspTests.jsp", "jspTests.jsp")
				.addAsLibrary(jar)
				;
		

		return war;
	}
	
	@ArquillianResource(JNDIServlet.class)
	URL jndiURL;
	
	@ArquillianResource(JSPSecurityServlet.class)
	URL jspURL;
	
	@ArquillianResource(ClassloaderServlet.class)
	URL classloaderURL;
	
	@ArquillianResource(SecurityServlet.class)
	URL securityURL;
	
	@ArquillianResource(ContextServiceDefinitionServlet.class)
	URL contextURL;
	
	@ArquillianResource(ContextServiceDefinitionFromEJBServlet.class)
	URL ejbContextURL;

	// HttpServletRequest.getUserPrincipal behavior is unclear when accessed from another thread or the current user is changed
	@Test(enabled = false)
	public void testSecurityClearedContext() {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(jspURL).withPaths("jspTests.jsp").withTestName(testName);
		runTest(requestURL);
	}

	// HttpServletRequest.getUserPrincipal behavior is unclear when accessed from another thread or the current user is changed
	@Test(enabled = false)
	public void testSecurityUnchangedContext() {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(jspURL).withPaths("jspTests.jsp").withTestName(testName);
		runTest(requestURL);
	}
	
	@Test
	public void testSecurityPropagatedContext() {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(jspURL).withPaths("jspTests.jsp").withTestName(testName);
		runTest(requestURL);
	}

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
	
    /**
     * A ContextServiceDefinition with all attributes configured
     * propagates/clears/ignores context types as configured.
     * ContextA, which is tested here, propagates Application context and IntContext,
     * clears StringContext, and leaves Transaction context unchanged.
     */
	@Test
    public void testContextServiceDefinitionAllAttributes() throws Throwable {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(contextURL).withPaths("ContextServiceDefinitionServlet").withTestName(testName);
		runTest(requestURL);
    }
	
    /**
     * A ContextServiceDefinition defined in an EJB with all attributes configured
     * propagates/clears/ignores context types as configured.
     * ContextA, which is tested here, propagates Application context and IntContext,
     * clears StringContext, and leaves Transaction context unchanged.
     */
	@Test
    public void testContextServiceDefinitionFromEJBAllAttributes() throws Throwable {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL).withPaths("ContextServiceDefinitionFromEJBServlet").withTestName(testName);
		runTest(requestURL);
    }

    /**
     * A ContextServiceDefinition with minimal attributes configured
     * clears transaction context and propagates other types.
     */
	@Test
    public void testContextServiceDefinitionDefaults() throws Throwable {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(contextURL).withPaths("ContextServiceDefinitionServlet").withTestName(testName);
		runTest(requestURL);
    }
	
    /**
     * A ContextServiceDefinition defined in an EJB with minimal attributes configured
     * clears transaction context and propagates other types.
     */
	@Test
    public void testContextServiceDefinitionFromEJBDefaults() throws Throwable {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL).withPaths("ContextServiceDefinitionFromEJBServlet").withTestName(testName);
		runTest(requestURL);
    }

    /**
     * A ContextServiceDefinition can specify a third-party context type to be propagated/cleared/ignored.
     * This test uses 2 ContextServiceDefinitions:
     * ContextA with IntContext propagated and StringContext cleared.
     * ContextB with IntContext unchanged and StringContext propagated (per ALL_REMAINING).
     */
	@Test
    public void testContextServiceDefinitionWithThirdPartyContext() throws Throwable {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(contextURL).withPaths("ContextServiceDefinitionServlet").withTestName(testName);
		runTest(requestURL);
    }

    /**
     * A ContextService contextualizes a Consumer, which can be supplied as a dependent stage action
     * to an unmanaged CompletableFuture. The dependent stage action runs with the thread context of
     * the thread that contextualizes the Consumer, per the configuration of the ContextServiceDefinition.
     */
	@Test
    public void testContextualConsumer() throws Throwable {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(contextURL).withPaths("ContextServiceDefinitionServlet").withTestName(testName);
		runTest(requestURL);
    }

    /**
     * A ContextService contextualizes a Function, which can be supplied as a dependent stage action
     * to an unmanaged CompletableFuture. The dependent stage action runs with the thread context of
     * the thread that contextualizes the Function, per the configuration of the ContextServiceDefinition.
     */
	@Test
    public void testContextualFunction() throws Throwable {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(contextURL).withPaths("ContextServiceDefinitionServlet").withTestName(testName);
		runTest(requestURL);
    }

    /**
     * A ContextService contextualizes a Supplier, which can be supplied as a dependent stage action
     * to an unmanaged CompletableFuture. The dependent stage action runs with the thread context of
     * the thread that contextualizes the Supplier, per the configuration of the ContextServiceDefinition.
     */
	@Test
    public void testContextualSupplier() throws Throwable {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(contextURL).withPaths("ContextServiceDefinitionServlet").withTestName(testName);
		runTest(requestURL);
        requestURL = URLBuilder.get().withBaseURL(ejbContextURL).withPaths("ContextServiceDefinitionFromEJBServlet").withTestName(testName);
        runTest(requestURL);
    }

    /**
     * ContextService can create a contextualized copy of an unmanaged CompletableFuture.
     */
	@Test
    public void testCopyWithContextCapture() throws Throwable {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(contextURL).withPaths("ContextServiceDefinitionServlet").withTestName(testName);
		runTest(requestURL);
    }
}
