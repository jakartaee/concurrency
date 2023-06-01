/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
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
package ee.jakarta.tck.concurrent.spec.ManagedScheduledExecutorService.resourcedef;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.framework.URLBuilder;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionWebBean;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionInterface;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionServlet;
import ee.jakarta.tck.concurrent.spi.context.IntContextProvider;
import ee.jakarta.tck.concurrent.spi.context.StringContextProvider;
import jakarta.enterprise.concurrent.spi.ThreadContextProvider;

import static ee.jakarta.tck.concurrent.common.TestGroups.JAKARTAEE_WEB;;

@Tag(JAKARTAEE_WEB)
public class ManagedScheduledExecutorDefinitionWebTests extends TestClient {
	
	@ArquillianResource(ManagedScheduledExecutorDefinitionServlet.class)
	URL baseURL;
	
	@ArquillianResource(ManagedScheduledExecutorDefinitionOnEJBServlet.class)
	URL ejbContextURL;
	
	@Deployment(name="ManagedScheduledExecutorDefinitionTests", testable=false)
	public static WebArchive createDeployment() {
		
		WebArchive war = ShrinkWrap.create(WebArchive.class, "ManagedScheduledExecutorDefinitionTests_web.war")
				.addPackages(false,
						ManagedScheduledExecutorDefinitionWebTests.class.getPackage(),
						getFrameworkPackage(), 
						getContextPackage(),
						getContextProvidersPackage())
				.addClasses(
						ContextServiceDefinitionServlet.class,
						ContextServiceDefinitionInterface.class,
						ContextServiceDefinitionWebBean.class)
				.deleteClasses(ManagedScheduledExecutorDefinitionBean.class)
				.addAsServiceProvider(ThreadContextProvider.class.getName(), IntContextProvider.class.getName(), StringContextProvider.class.getName());
		
		return war;
	}
	
	@Override
	protected String getServletPath() {
		return "ManagedScheduledExecutorDefinitionServlet";
	}
	

	@Test
    public void testAsyncCompletionStageMSE() {
		runTest(baseURL);
    }

	@Test
    public void testAsynchronousMethodRunsWithContext() {
		runTest(baseURL);
    }

	@Test
    public void testAsynchronousMethodWithMaxAsync3() {
		runTest(baseURL);
    }
    
	// Accepted TCK Challenge: https://github.com/jakartaee/concurrency/issues/224
	@Disabled
    public void testCompletedFutureMSE() {
		runTest(baseURL);
    }

	@Test
    public void testIncompleteFutureMSE() {
		runTest(baseURL);
    }
	
	@Test
    public void testIncompleteFutureMSE_EJB() {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL).withPaths("ManagedScheduledExecutorDefinitionOnEJBServlet").withTestName(testName);
		runTest(requestURL);
    }

	@Test
    public void testManagedScheduledExecutorDefinitionAllAttributes() {
		runTest(baseURL);
    }
	
	@Test
    public void testManagedScheduledExecutorDefinitionAllAttributes_EJB() {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL).withPaths("ManagedScheduledExecutorDefinitionOnEJBServlet").withTestName(testName);
		runTest(requestURL);
    }

	@Test
    public void testManagedScheduledExecutorDefinitionDefaults() {
		runTest(baseURL);
    }
	
	@Test
    public void testManagedScheduledExecutorDefinitionDefaults_EJB() {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL).withPaths("ManagedScheduledExecutorDefinitionOnEJBServlet").withTestName(testName);
		runTest(requestURL);
    }

	@Test
    public void testNotAnAsynchronousMethod() {
		runTest(baseURL);
    }

	@Test
    public void testScheduleWithCronTrigger() {
		runTest(baseURL);
    }

	@Test
    public void testScheduleWithZonedTrigger() {
		runTest(baseURL);
    }
}
