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
package ee.jakarta.tck.concurrent.spec.ManagedThreadFactory.resourcedef;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.framework.URLBuilder;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionBean;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionInterface;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionServlet;
import ee.jakarta.tck.concurrent.spi.context.IntContextProvider;
import ee.jakarta.tck.concurrent.spi.context.StringContextProvider;
import jakarta.enterprise.concurrent.spi.ThreadContextProvider;


public class ManagedThreadFactoryDefinitionTests extends TestClient {
	
	@ArquillianResource(ManagedThreadFactoryDefinitionServlet.class)
	URL baseURL;
	
	@ArquillianResource(ManagedThreadFactoryDefinitionOnEJBServlet.class)
	URL ejbContextURL;
	
	@Deployment(name="ManagedThreadFactoryDefinitionTests", testable=false)
	public static WebArchive createDeployment() {
		
		JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "ManagedThreadFactoryDefinitionTests_ejb.jar")
				.addPackages(false, getFrameworkPackage(), ManagedThreadFactoryDefinitionTests.class.getPackage())
				.deleteClasses(
						ManagedThreadFactoryDefinitionOnEJBServlet.class,
						ManagedThreadFactoryDefinitionServlet.class)
				.addClasses(
						ContextServiceDefinitionInterface.class,
						ContextServiceDefinitionBean.class)
				.addAsManifestResource(ManagedThreadFactoryDefinitionTests.class.getPackage(), "ejb-jar.xml", "ejb-jar.xml");
//				TODO document how users can dynamically inject vendor specific deployment descriptors into this archive

		WebArchive war = ShrinkWrap.create(WebArchive.class, "ManagedThreadFactoryDefinitionTests_web.war")
				.addPackages(false,
							 getFrameworkPackage(),
							 getContextPackage(),
							 getContextProvidersPackage())
				.addClasses(
						ManagedThreadFactoryDefinitionOnEJBServlet.class,
						ManagedThreadFactoryDefinitionServlet.class,
						ContextServiceDefinitionServlet.class)
				.addAsWebInfResource(ManagedThreadFactoryDefinitionTests.class.getPackage(), "web.xml", "web.xml")
				.addAsServiceProvider(ThreadContextProvider.class.getName(), IntContextProvider.class.getName(), StringContextProvider.class.getName())
				.addAsLibrary(jar)
				;
		
		return war;
	}
	
	@Override
	protected String getServletPath() {
		return "ManagedThreadFactoryDefinitionServlet";
	}
	
        // Accepted TCK challenge: https://github.com/jakartaee/concurrency/issues/226
	@Test(enabled = false)
    public void testManagedThreadFactoryDefinitionAllAttributes() throws Throwable {
		runTest(baseURL);
    }
	
        // Accepted TCK challenge: https://github.com/jakartaee/concurrency/issues/226
	@Test(enabled = false)
    public void testManagedThreadFactoryDefinitionAllAttributesEJB() throws Throwable {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL).withPaths("ManagedThreadFactoryDefinitionOnEJBServlet").withTestName(testName);
		runTest(requestURL);
    }

	@Test
    public void testManagedThreadFactoryDefinitionDefaults() throws Throwable {
		runTest(baseURL);
    }
	
	@Test
    public void testManagedThreadFactoryDefinitionDefaultsEJB() throws Throwable {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL).withPaths("ManagedThreadFactoryDefinitionOnEJBServlet").withTestName(testName);
		runTest(requestURL);
    }

        // Accepted TCK challenge: https://github.com/jakartaee/concurrency/issues/226
	@Test(enabled = false)
    public void testParallelStreamBackedByManagedThreadFactory() throws Throwable {
		runTest(baseURL);
    }
	
        // Accepted TCK challenge: https://github.com/jakartaee/concurrency/issues/226
	@Test(enabled = false)
    public void testParallelStreamBackedByManagedThreadFactoryEJB() throws Throwable {
		URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL).withPaths("ManagedThreadFactoryDefinitionOnEJBServlet").withTestName(testName);
		runTest(requestURL);
	}
}
