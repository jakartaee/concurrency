/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
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
import org.testng.annotations.Test;

import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionServlet;
import ee.jakarta.tck.concurrent.spi.context.IntContextProvider;
import ee.jakarta.tck.concurrent.spi.context.StringContextProvider;
import jakarta.enterprise.concurrent.spi.ThreadContextProvider;


public class ManagedScheduledExecutorDefinitionTests extends TestClient {
	
	@ArquillianResource(ManagedScheduledExecutorDefinitionServlet.class)
	URL baseURL;
	
	@Deployment(name="ManagedScheduledExecutorDefinitionTests", testable=false)
	public static WebArchive createDeployment() {
		return ShrinkWrap.create(WebArchive.class)
				.addPackages(false, ManagedScheduledExecutorDefinitionTests.class.getPackage(),
						getFrameworkPackage(), 
						getContextPackage(),
						getContextProvidersPackage())
				.addClasses(ContextServiceDefinitionServlet.class)
				.addAsServiceProvider(ThreadContextProvider.class.getName(), IntContextProvider.class.getName(), StringContextProvider.class.getName());
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

	@Test
    public void testCompletedFutureMSE() {
		runTest(baseURL);
    }

	@Test
    public void testIncompleteFutureMSE() {
		runTest(baseURL);
    }

	@Test
    public void testManagedScheduledExecutorDefinitionAllAttributes() {
		runTest(baseURL);
    }

	@Test
    public void testManagedScheduledExecutorDefinitionDefaults() {
		runTest(baseURL);
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
