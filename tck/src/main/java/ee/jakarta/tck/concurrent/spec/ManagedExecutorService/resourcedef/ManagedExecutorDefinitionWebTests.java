/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package ee.jakarta.tck.concurrent.spec.ManagedExecutorService.resourcedef;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.framework.URLBuilder;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionWebBean;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionInterface;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionServlet;
import ee.jakarta.tck.concurrent.spi.context.IntContextProvider;
import ee.jakarta.tck.concurrent.spi.context.StringContextProvider;
import jakarta.enterprise.concurrent.spi.ThreadContextProvider;

import static ee.jakarta.tck.concurrent.common.TestGroups.JAKARTAEE_WEB;;

@Test(groups = JAKARTAEE_WEB)
public class ManagedExecutorDefinitionWebTests extends TestClient{
	
	@ArquillianResource(ManagedExecutorDefinitionServlet.class)
	URL baseURL;
	
	@ArquillianResource(ManagedExecutorDefinitionOnEJBServlet.class)
	URL ejbContextURL;
	
	@Deployment(name="ManagedExecutorDefinitionTests", testable=false)
	public static WebArchive createDeployment() {
		
		WebArchive war = ShrinkWrap.create(WebArchive.class, "ManagedExecutorDefinitionTests_web.war")
				.addPackages(false,
						ManagedExecutorDefinitionWebTests.class.getPackage(),
						getFrameworkPackage(), 
						getContextPackage(),
						getContextProvidersPackage())
				.addClasses(
						ContextServiceDefinitionInterface.class,
						ContextServiceDefinitionWebBean.class,
						ContextServiceDefinitionServlet.class)
				.addAsServiceProvider(ThreadContextProvider.class.getName(), IntContextProvider.class.getName(), StringContextProvider.class.getName());
		
		return war;
	}
	
	@Override
	protected String getServletPath() {
		return "ManagedExecutorDefinitionServlet";
	}
	
	@Test
    public void testAsyncCompletionStage() {
    	runTest(baseURL);
    }

	@Test
    public void testAsynchronousMethodReturnsCompletableFuture() {
    	runTest(baseURL);
    }

	@Test
    public void testAsynchronousMethodReturnsCompletionStage() {
    	runTest(baseURL);
    }

	@Test
    public void testAsynchronousMethodVoidReturnType() {
    	runTest(baseURL);
    }

    // TCK Accepted Challenge: https://github.com/jakartaee/concurrency/issues/224
	@Test(enabled = false)
    public void testCompletedFuture() {
    	runTest(baseURL);
    }

	@Test
    public void testCopyCompletableFuture() {
    	runTest(baseURL);
    }
	
	@Test
    public void testCopyCompletableFutureEJB() {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL).withPaths("ManagedExecutorDefinitionOnEJBServlet").withTestName(testName);
		runTest(requestURL);
    }

	@Test
    public void testIncompleteFuture() {
    	runTest(baseURL);
    }
	
	@Test
    public void testIncompleteFutureEJB() {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL).withPaths("ManagedExecutorDefinitionOnEJBServlet").withTestName(testName);
		runTest(requestURL);
    }

	@Test
    public void testManagedExecutorDefinitionAllAttributes() {
    	runTest(baseURL);
    }

	@Test
    public void testManagedExecutorDefinitionDefaults() {
    	runTest(baseURL);
    }

}
