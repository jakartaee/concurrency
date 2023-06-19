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
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.common.context.providers.IntContextProvider;
import ee.jakarta.tck.concurrent.common.context.providers.StringContextProvider;
import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.framework.URLBuilder;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common.PACKAGE;
import ee.jakarta.tck.concurrent.framework.junit.anno.Full;
import ee.jakarta.tck.concurrent.framework.junit.anno.TestName;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionBean;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionInterface;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionServlet;
import jakarta.enterprise.concurrent.spi.ThreadContextProvider;

@Full @RunAsClient
public class ManagedScheduledExecutorDefinitionTests extends TestClient {
	
	@ArquillianResource(ManagedScheduledExecutorDefinitionServlet.class)
	URL baseURL;
	
	@ArquillianResource(ManagedScheduledExecutorDefinitionOnEJBServlet.class)
	URL ejbContextURL;
	
	@Deployment(name="ManagedScheduledExecutorDefinitionTests")
	public static EnterpriseArchive createDeployment() {
		
		WebArchive war = ShrinkWrap.create(WebArchive.class, "ManagedScheduledExecutorDefinitionTests_web.war")
		        .addPackages(true, PACKAGE.CONTEXT.getPackageName(), PACKAGE.CONTEXT_PROVIDERS.getPackageName())
				.addClasses(
						ReqBean.class,
						ManagedScheduledExecutorDefinitionServlet.class,
						ManagedScheduledExecutorDefinitionOnEJBServlet.class,
						ContextServiceDefinitionServlet.class)
				.addAsServiceProvider(ThreadContextProvider.class.getName(), IntContextProvider.class.getName(), StringContextProvider.class.getName());
		
		JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "ManagedScheduledExecutorDefinitionTests_ejb.jar")
				.addPackages(false,  ManagedScheduledExecutorDefinitionTests.class.getPackage())
				.deleteClasses(
						ReqBean.class,
						ManagedScheduledExecutorDefinitionWebBean.class,
						ManagedScheduledExecutorDefinitionServlet.class,
						ManagedScheduledExecutorDefinitionOnEJBServlet.class)
				.addClasses(
						ContextServiceDefinitionInterface.class,
						ContextServiceDefinitionBean.class)
				.addAsManifestResource(ManagedScheduledExecutorDefinitionTests.class.getPackage(), "ejb-jar.xml", "ejb-jar.xml");
		
		EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "ManagedScheduledExecutorDefinitionTests.ear").addAsModules(war, jar);
		
		return ear;
	}
	
    @TestName
    String testname;
	
	@Override
	protected String getServletPath() {
		return "ManagedScheduledExecutorDefinitionServlet";
	}
	

	@Test
    public void testAsyncCompletionStageMSE() {
		runTest(baseURL, testname);
    }

	@Test
    public void testAsynchronousMethodRunsWithContext() {
		runTest(baseURL, testname);
    }

	@Test
    public void testAsynchronousMethodWithMaxAsync3() {
		runTest(baseURL, testname);
    }
    
	// Accepted TCK Challenge: https://github.com/jakartaee/concurrency/issues/224
	@Disabled
    public void testCompletedFutureMSE() {
		runTest(baseURL, testname);
    }

	@Test
    public void testIncompleteFutureMSE() {
		runTest(baseURL, testname);
    }
	
	@Test
    public void testIncompleteFutureMSE_EJB() {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL).withPaths("ManagedScheduledExecutorDefinitionOnEJBServlet").withTestName(testname);
		runTest(requestURL);
    }

	@Test
    public void testManagedScheduledExecutorDefinitionAllAttributes() {
		runTest(baseURL, testname);
    }
	
	@Test
    public void testManagedScheduledExecutorDefinitionAllAttributes_EJB() {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL).withPaths("ManagedScheduledExecutorDefinitionOnEJBServlet").withTestName(testname);
		runTest(requestURL);
    }

	@Test
    public void testManagedScheduledExecutorDefinitionDefaults() {
		runTest(baseURL, testname);
    }
	
	@Test
    public void testManagedScheduledExecutorDefinitionDefaults_EJB() {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL).withPaths("ManagedScheduledExecutorDefinitionOnEJBServlet").withTestName(testname);
		runTest(requestURL);
    }

	@Test
    public void testNotAnAsynchronousMethod() {
		runTest(baseURL, testname);
    }

	@Test
    public void testScheduleWithCronTrigger() {
		runTest(baseURL, testname);
    }

	@Test
    public void testScheduleWithZonedTrigger() {
		runTest(baseURL, testname);
    }
}
