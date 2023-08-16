/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Disabled;

import ee.jakarta.tck.concurrent.common.context.providers.IntContextProvider;
import ee.jakarta.tck.concurrent.common.context.providers.StringContextProvider;
import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.framework.URLBuilder;
import ee.jakarta.tck.concurrent.framework.junit.anno.Assertion;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common.PACKAGE;
import ee.jakarta.tck.concurrent.framework.junit.anno.TestName;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionInterface;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionServlet;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionWebBean;
import jakarta.enterprise.concurrent.spi.ThreadContextProvider;;

@Web
@RunAsClient // Requires client testing due to multiple servlets and annotation configuration
@Common({ PACKAGE.CONTEXT, PACKAGE.CONTEXT_PROVIDERS })
public class ManagedExecutorDefinitionWebTests extends TestClient {

    @ArquillianResource(ManagedExecutorDefinitionServlet.class)
    private URL baseURL;

    @ArquillianResource(ManagedExecutorDefinitionOnEJBServlet.class)
    private URL ejbContextURL;

    @Deployment(name = "ManagedExecutorDefinitionTests")
    public static WebArchive createDeployment() {

        WebArchive war = ShrinkWrap.create(WebArchive.class, "ManagedExecutorDefinitionTests_web.war")
                .addPackages(false, ManagedExecutorDefinitionWebTests.class.getPackage())
                .addClasses(ContextServiceDefinitionInterface.class, ContextServiceDefinitionWebBean.class,
                        ContextServiceDefinitionServlet.class)
                .deleteClasses(ManagedExecutorDefinitionBean.class)
                .addAsServiceProvider(ThreadContextProvider.class.getName(), IntContextProvider.class.getName(),
                        StringContextProvider.class.getName());

        return war;
    }

    @TestName
    private String testname;

    @Override
    protected String getServletPath() {
        return "ManagedExecutorDefinitionServlet";
    }

    @Assertion(id = "GIT:154",
            strategy = "ManagedExecutorService submits an action to run asynchronously as a CompletionStage."
                    + " Dependent stages can be chained to the CompletionStage,"
                    + " and all stages run with the thread context of the thread from which they were created, per ManagedExecutorDefinition config.")
    public void testAsyncCompletionStage() {
        runTest(baseURL, testname);
    }

    @Assertion(id = "GIT:154", strategy = "Asynchronous method that returns CompletableFuture")
    public void testAsynchronousMethodReturnsCompletableFuture() {
        runTest(baseURL, testname);
    }

    @Assertion(id = "GIT:154", strategy = "Asynchronous method that returns CompletionStage")
    public void testAsynchronousMethodReturnsCompletionStage() {
        runTest(baseURL, testname);
    }

    @Assertion(id = "GIT:154", strategy = "Asynchronous method that returns void")
    public void testAsynchronousMethodVoidReturnType() {
        runTest(baseURL, testname);
    }

    @Disabled("https://github.com/jakartaee/concurrency/issues/224")
    @Assertion(id = "GIT:154",
        strategy = "ManagedExecutorService creates a completed CompletableFuture to which async dependent stages can be chained."
                + " The dependent stages all run with the thread context of the thread from which they were created,"
                + " per ManagedExecutorDefinition config.")
    public void testCompletedFuture() {
        runTest(baseURL, testname);
    }

    @Assertion(id = "GIT:154", strategy = "ManagedExecutorService can create a contextualized copy of an unmanaged CompletableFuture.")
    public void testCopyCompletableFuture() {
        runTest(baseURL, testname);
    }

    @Assertion(id = "GIT:154", strategy = "Use another managed executor from ManagedExecutorDefinition that was defined in an EJB")
    public void testCopyCompletableFutureEJB() {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL)
                .withPaths("ManagedExecutorDefinitionOnEJBServlet").withTestName(testname);
        runTest(requestURL);
    }

    @Assertion(id = "GIT:154",
            strategy = "ManagedExecutorService creates an incomplete CompletableFuture to which dependent stages can be chained."
                    + " The CompletableFuture can be completed from another thread lacking the same context,"
                    + " but the dependent stages all run with the thread context of the thread from which they were created,"
                    + " per ManagedExecutorDefinition config.")
    public void testIncompleteFuture() {
        runTest(baseURL, testname);
    }

    @Assertion(id = "GIT:154", strategy = "Use managed executor from ManagedExecutorDefinition that was defined in an EJB")
    public void testIncompleteFutureEJB() {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL)
                .withPaths("ManagedExecutorDefinitionOnEJBServlet").withTestName(testname);
        runTest(requestURL);
    }

    @Assertion(id = "GIT:154", strategy = "ManagedExecutorDefinition with all attributes configured")
    public void testManagedExecutorDefinitionAllAttributes() {
        runTest(baseURL, testname);
    }

    @Assertion(id = "GIT:154",
            strategy = "ManagedExecutorDefinition with minimal attributes can run multiple async tasks concurrently"
                    + " and uses java:comp/DefaultContextService to determine context propagation and clearing.")
    public void testManagedExecutorDefinitionDefaults() {
        runTest(baseURL, testname);
    }

}
