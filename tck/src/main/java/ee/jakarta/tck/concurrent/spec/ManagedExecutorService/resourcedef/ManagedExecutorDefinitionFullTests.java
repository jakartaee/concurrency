/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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
import ee.jakarta.tck.concurrent.framework.junit.anno.Platform;
import ee.jakarta.tck.concurrent.framework.junit.anno.TestName;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionBean;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionInterface;
import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionServlet;
import jakarta.enterprise.concurrent.spi.ThreadContextProvider;

@Platform
@RunAsClient // Requires client testing due to multiple servlets and annotation configuration
public class ManagedExecutorDefinitionFullTests extends TestClient {

    @ArquillianResource(ManagedExecutorDefinitionServlet.class)
    private URL baseURL;

    @ArquillianResource(ManagedExecutorDefinitionOnEJBServlet.class)
    private URL ejbContextURL;

    @Deployment(name = "ManagedExecutorDefinitionTests")
    public static EnterpriseArchive createDeployment() {

        WebArchive war = ShrinkWrap.create(WebArchive.class, "ManagedExecutorDefinitionTests_web.war")
                .addPackages(true, PACKAGE.CONTEXT.getPackageName(), PACKAGE.CONTEXT_PROVIDERS.getPackageName())
                .addClasses(AppBean.class, ManagedExecutorDefinitionServlet.class,
                        ManagedExecutorDefinitionOnEJBServlet.class, ContextServiceDefinitionServlet.class)
                .addAsServiceProvider(ThreadContextProvider.class.getName(), IntContextProvider.class.getName(),
                        StringContextProvider.class.getName());

        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "ManagedExecutorDefinitionTests_ejb.jar")
                .addPackages(false, ManagedExecutorDefinitionFullTests.class.getPackage())
                .deleteClasses(AppBean.class, ManagedExecutorDefinitionWebBean.class,
                        ManagedExecutorDefinitionServlet.class, ManagedExecutorDefinitionOnEJBServlet.class,
                        ContextServiceDefinitionServlet.class)
                .addClasses(ContextServiceDefinitionInterface.class, ContextServiceDefinitionBean.class);

        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "ManagedExecutorDefinitionTests.ear")
                .addAsModules(war, jar);

        return ear;
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

    @Challenge(link = "https://github.com/jakartaee/concurrency/issues/224", version = "3.0.0")
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
    
    @Assertion(id = "GIT:439", strategy = "Ensure scheduled asynchronous methods are completed when future is completed.")
    public void testScheduledAsynchCompletedFuture() {
        runTest(baseURL, testname);
    }
    
    @Assertion(id = "GIT:439", strategy = "Ensure scheduled asynchronous methods are completed when a non-null result is returned.")
    public void testScheduledAsynchCompletedResult() {
        runTest(baseURL, testname);
    }
    
    @Assertion(id = "GIT:439", strategy = "Ensure scheduled asynchronous methods are completed when an exception is thrown.")
    public void testScheduledAsynchCompletedExceptionally() {
        runTest(baseURL, testname);
    }

    @Assertion(id = "GIT:439", strategy = "Ensure overlapping scheduled asynchronous methods are skipped.")
    public void testScheduledAsynchOverlapSkipping() {
        runTest(baseURL, testname);
    }
    
    @Assertion(id = "GIT:439", strategy = "Ensure scheduled asynchronous methods ignore the max-async configuration."
            + " Ensure scheduled asynchronous methods honor cleared context configuration")
    public void testScheduledAsynchIgnoresMaxAsync() {
        runTest(baseURL, testname);
    }
    
    @Assertion(id = "GIT:439", strategy = "Ensure scheduled asynchronous methods choose closest execution time when multiple schedules are provided."
            + " Ensure scheduled asynchronous methods honor propogated context configuration")
    public void testScheduledAsynchWithMultipleSchedules() {
        runTest(baseURL, testname);
    }
    
    @Assertion(id = "GIT:439", strategy = "Ensure scheduled asynchronous methods are not executed when an invalid JNDI name is provided.")
    public void testScheduledAsynchWithInvalidJNDIName() {
        runTest(baseURL, testname);
    }
    
    @Assertion(id = "GIT:439", strategy = "Ensure scheduled asynchronous methods with void return type stop execution via completable future or exception.")
    public void testScheduledAsynchVoidReturn() {
        runTest(baseURL, testname);
    }
}
