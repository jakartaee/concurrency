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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.common.context.providers.IntContextProvider;
import ee.jakarta.tck.concurrent.common.context.providers.StringContextProvider;
import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.framework.URLBuilder;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common.PACKAGE;
import ee.jakarta.tck.concurrent.framework.junit.anno.TestName;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionInterface;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionServlet;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionWebBean;
import jakarta.enterprise.concurrent.spi.ThreadContextProvider;;

@Web
@RunAsClient // Requires client testing due to annotation configuration
@Common({ PACKAGE.CONTEXT, PACKAGE.CONTEXT_PROVIDERS })
public class ManagedScheduledExecutorDefinitionWebTests extends TestClient {

    @ArquillianResource(ManagedScheduledExecutorDefinitionServlet.class)
    private URL baseURL;

    @ArquillianResource(ManagedScheduledExecutorDefinitionOnEJBServlet.class)
    private URL ejbContextURL;

    @Deployment(name = "ManagedScheduledExecutorDefinitionTests")
    public static WebArchive createDeployment() {

        WebArchive war = ShrinkWrap.create(WebArchive.class, "ManagedScheduledExecutorDefinitionTests_web.war")
                .addPackages(false, ManagedScheduledExecutorDefinitionWebTests.class.getPackage())
                .addClasses(ContextServiceDefinitionServlet.class, ContextServiceDefinitionInterface.class,
                        ContextServiceDefinitionWebBean.class)
                .deleteClasses(ManagedScheduledExecutorDefinitionBean.class)
                .addAsServiceProvider(ThreadContextProvider.class.getName(), IntContextProvider.class.getName(),
                        StringContextProvider.class.getName());

        return war;
    }

    @TestName
    private String testname;

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
    public void testIncompleteFutureMSEEJB() {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL)
                .withPaths("ManagedScheduledExecutorDefinitionOnEJBServlet").withTestName(testname);
        runTest(requestURL);
    }

    @Test
    public void testManagedScheduledExecutorDefinitionAllAttributes() {
        runTest(baseURL, testname);
    }

    @Test
    public void testManagedScheduledExecutorDefinitionAllAttributesEJB() {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL)
                .withPaths("ManagedScheduledExecutorDefinitionOnEJBServlet").withTestName(testname);
        runTest(requestURL);
    }

    @Test
    public void testManagedScheduledExecutorDefinitionDefaults() {
        runTest(baseURL, testname);
    }

    @Test
    public void testManagedScheduledExecutorDefinitionDefaultsEJB() {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL)
                .withPaths("ManagedScheduledExecutorDefinitionOnEJBServlet").withTestName(testname);
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
