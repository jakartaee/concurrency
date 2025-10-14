/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation
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
package ee.jakarta.tck.concurrent.spec.ManagedThreadFactory.injected;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import ee.jakarta.tck.concurrent.common.context.providers.IntContextProvider;
import ee.jakarta.tck.concurrent.common.context.providers.StringContextProvider;
import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.framework.URLBuilder;
import ee.jakarta.tck.concurrent.framework.junit.anno.Assertion;
import ee.jakarta.tck.concurrent.framework.junit.anno.Challenge;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common.PACKAGE;
import ee.jakarta.tck.concurrent.framework.junit.anno.Full;
import ee.jakarta.tck.concurrent.framework.junit.anno.TestName;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionBean;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionInterface;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionServlet;
import ee.jakarta.tck.concurrent.spec.ManagedThreadFactory.resourcedef.ManagedThreadFactoryDefinitionFullTests;
import jakarta.enterprise.concurrent.spi.ThreadContextProvider;

@Full
@RunAsClient // Requires client testing due to multiple servlets and annotation configuration
public class ManagedThreadFactoryDefinitionInjectedFullTests extends TestClient {

    @ArquillianResource(ManagedThreadFactoryDefinitionInjectedServlet.class)
    private URL baseURL;

    @ArquillianResource(ManagedThreadFactoryDefinitionOnEJBInjectedServlet.class)
    private URL ejbContextURL;

    @Deployment(name = "ManagedThreadFactoryDefinitionTests")
    public static EnterpriseArchive createDeployment() {

        WebArchive war = ShrinkWrap.create(WebArchive.class, "ManagedThreadFactoryDefinitionTests_web.war")
                .addPackages(false, PACKAGE.CONTEXT.getPackageName(), PACKAGE.CONTEXT_PROVIDERS.getPackageName())
                .addClasses(ManagedThreadFactoryDefinitionOnEJBInjectedServlet.class,
                        ManagedThreadFactoryDefinitionInjectedServlet.class, ContextServiceDefinitionServlet.class)
                .addAsWebInfResource(ManagedThreadFactoryDefinitionFullTests.class.getPackage(), "web.xml", "web.xml")
                .addAsServiceProvider(ThreadContextProvider.class.getName(), IntContextProvider.class.getName(),
                        StringContextProvider.class.getName());

        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "ManagedThreadFactoryDefinitionInjectedTests_ejb.jar")
                .addPackages(false, ManagedThreadFactoryDefinitionInjectedFullTests.class.getPackage())
                .deleteClasses(ManagedThreadFactoryDefinitionOnEJBInjectedServlet.class,
                        ManagedThreadFactoryDefinitionInjectedServlet.class)
                .addClasses(ContextServiceDefinitionInterface.class, ContextServiceDefinitionBean.class)
                .addAsManifestResource(ManagedThreadFactoryDefinitionFullTests.class.getPackage(), "ejb-jar.xml",
                        "ejb-jar.xml");

        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "ManagedThreadFactoryDefinitionInjectedTests.ear")
                .addAsModules(war, jar);

        return ear;
    }

    @TestName
    private String testname;

    @Override
    protected String getServletPath() {
        return "ManagedThreadFactoryDefinitionInjectedServlet";
    }

    @Challenge(link = "https://github.com/jakartaee/concurrency/issues/226", version = "3.0.0")
    @Assertion(id = "GIT:156", strategy = "ManagedThreadFactoryDefinition with all attributes configured")
    public void testManagedThreadFactoryDefinitionAllAttributes() throws Throwable {
        runTest(baseURL, testname);
    }

    @Challenge(link = "https://github.com/jakartaee/concurrency/issues/226", version = "3.0.0")
    @Assertion(id = "GIT:156",
        strategy = "A ManagedThreadFactoryDefinition defined on an EJB with all attributes configured enforces priority and propagates context.")
    public void testManagedThreadFactoryDefinitionAllAttributesEJB() throws Throwable {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL)
                .withPaths("ManagedThreadFactoryDefinitionOnEJBInjectedServlet").withTestName(testname);
        runTest(requestURL);
    }

    @Assertion(id = "GIT:156", strategy = "ManagedThreadFactoryDefinition with minimal attributes configured")
    public void testManagedThreadFactoryDefinitionDefaults() throws Throwable {
        runTest(baseURL, testname);
    }

    @Assertion(id = "GIT:156",
            strategy = "ManagedThreadFactoryDefinition defined on an EJB with minimal attributes creates threads with normal priority"
                    + " and uses java:comp/DefaultContextService to determine context propagation and clearing")
    public void testManagedThreadFactoryDefinitionDefaultsEJB() throws Throwable {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL)
                .withPaths("ManagedThreadFactoryDefinitionOnEJBInjectedServlet").withTestName(testname);
        runTest(requestURL);
    }

    //@Challenge(link = "https://github.com/jakartaee/concurrency/issues/226", version = "3.0.0")
    @Assertion(id = "GIT:156",
        strategy = "ManagedThreadFactory can be supplied to a ForkJoinPool, which manages thread context and priority as configured")
    public void testParallelStreamBackedByManagedThreadFactory() throws Throwable {
        runTest(baseURL, testname);
    }

    //@Challenge(link = "https://github.com/jakartaee/concurrency/issues/226", version = "3.0.0")
    @Assertion(id = "GIT:156",
        strategy = "ManagedThreadFactoryDefinition defined on an EJB is supplied to a ForkJoinPool"
                + " and uses java:comp/DefaultContextService to determine context propagation and priority.")
    public void testParallelStreamBackedByManagedThreadFactoryEJB() throws Throwable {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL)
                .withPaths("ManagedThreadFactoryDefinitionOnEJBInjectedServlet").withTestName(testname);
        runTest(requestURL);
    }
}
