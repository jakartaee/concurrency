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
package ee.jakarta.tck.concurrent.spec.ManagedScheduledExecutorService.resourcedef;

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
@RunAsClient // Requires client testing due to annotation configuration
public class ManagedScheduledExecutorDefinitionFullTests extends TestClient {

    @ArquillianResource(ManagedScheduledExecutorDefinitionServlet.class)
    private URL baseURL;

    @ArquillianResource(ManagedScheduledExecutorDefinitionOnEJBServlet.class)
    private URL ejbContextURL;

    @Deployment(name = "ManagedScheduledExecutorDefinitionTests")
    public static EnterpriseArchive createDeployment() {

        WebArchive war = ShrinkWrap.create(WebArchive.class, "ManagedScheduledExecutorDefinitionTests_web.war")
                .addPackages(true, PACKAGE.CONTEXT.getPackageName(), PACKAGE.CONTEXT_PROVIDERS.getPackageName())
                .addClasses(ReqBean.class, ManagedScheduledExecutorDefinitionServlet.class,
                        ManagedScheduledExecutorDefinitionOnEJBServlet.class, ContextServiceDefinitionServlet.class)
                .addAsServiceProvider(ThreadContextProvider.class.getName(), IntContextProvider.class.getName(),
                        StringContextProvider.class.getName());

        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "ManagedScheduledExecutorDefinitionTests_ejb.jar")
                .addPackages(false, ManagedScheduledExecutorDefinitionFullTests.class.getPackage())
                .deleteClasses(ReqBean.class, ManagedScheduledExecutorDefinitionWebBean.class,
                        ManagedScheduledExecutorDefinitionServlet.class,
                        ManagedScheduledExecutorDefinitionOnEJBServlet.class)
                .addClasses(ContextServiceDefinitionInterface.class, ContextServiceDefinitionBean.class)
                .addAsManifestResource(ManagedScheduledExecutorDefinitionFullTests.class.getPackage(), "ejb-jar.xml",
                        "ejb-jar.xml");

        EnterpriseArchive ear = ShrinkWrap
                .create(EnterpriseArchive.class, "ManagedScheduledExecutorDefinitionTests.ear").addAsModules(war, jar);

        return ear;
    }

    @TestName
    private String testname;

    @Override
    protected String getServletPath() {
        return "ManagedScheduledExecutorDefinitionServlet";
    }

    @Assertion(id = "GIT:154",
            strategy = "ManagedScheduledExecutorService submits an action to run asynchronously as a CompletionStage."
                    + " Dependent stages can be chained to the CompletionStage,"
                    + " and all stages run with the thread context of the thread from which they were created,"
                    + " per ManagedScheduledExecutorDefinition config.")
    public void testAsyncCompletionStageMSE() {
        runTest(baseURL, testname);
    }

    @Assertion(id = "GIT:154", strategy = "Asynchronous method runs with thread context captured from caller")
    public void testAsynchronousMethodRunsWithContext() {
        runTest(baseURL, testname);
    }

    @Assertion(id = "GIT:154", strategy = "Asynchronous method execution is constrained by executor's maxAsync")
    public void testAsynchronousMethodWithMaxAsync3() {
        runTest(baseURL, testname);
    }

    @Challenge(link = "https://github.com/jakartaee/concurrency/issues/224", version = "3.0.0")
    @Assertion(id = "GIT:154",
        strategy = "ManagedScheduledExecutorService creates a completed CompletableFuture to which async dependent stages can be chained."
                + " The dependent stages all run with the thread context of the thread from which they were created,"
                + " per ManagedScheduledExecutorDefinition config.")
    public void testCompletedFutureMSE() {
        runTest(baseURL, testname);
    }

    @Assertion(id = "GIT:154",
                strategy = "ManagedScheduledExecutorService creates an incomplete CompletableFuture to which dependent stages can be chained."
                        + " The CompletableFuture can be completed from another thread lacking the same context,"
                        + " but the dependent stages all run with the thread context of the thread from which they were created,"
                        + " per ManagedScheduledExecutorDefinition config.")
    public void testIncompleteFutureMSE() {
        runTest(baseURL, testname);
    }

    @Assertion(id = "GIT:154",
            strategy = "ManagedScheduledExecutorService creates an incomplete CompletableFuture to which dependent stages can be chained."
                    + " The CompletableFuture can be completed from another thread lacking the same context,"
                    + " but the dependent stages all run with the thread context of the thread from which they were created,"
                    + " per ManagedScheduledExecutorDefinition config.")
    public void testIncompleteFutureMSEEJB() {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL)
                .withPaths("ManagedScheduledExecutorDefinitionOnEJBServlet").withTestName(testname);
        runTest(requestURL);
    }

    @Assertion(id = "GIT:154", strategy = "ManagedScheduledExecutorDefinition with all attributes configured")
    public void testManagedScheduledExecutorDefinitionAllAttributes() {
        runTest(baseURL, testname);
    }

    @Assertion(id = "GIT:154",
            strategy = "ManagedScheduledExecutorDefinition defined on an EJB with all attributes configured enforces maxAsync and propagates context")
    public void testManagedScheduledExecutorDefinitionAllAttributesEJB() {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL)
                .withPaths("ManagedScheduledExecutorDefinitionOnEJBServlet").withTestName(testname);
        runTest(requestURL);
    }

    @Assertion(id = "GIT:154",
            strategy = "ManagedScheduledExecutorDefinition with minimal attributes can run multiple async tasks concurrently"
                    + " and uses java:comp/DefaultContextService to determine context propagation and clearing.")
    public void testManagedScheduledExecutorDefinitionDefaults() {
        runTest(baseURL, testname);
    }

    @Assertion(id = "GIT:154",
            strategy = "ManagedScheduledExecutorDefinition defined on an EJB with minimal attributes can run multiple async tasks concurrently"
                    + " and uses java:comp/DefaultContextService to determine context propagation and clearing")
    public void testManagedScheduledExecutorDefinitionDefaultsEJB() {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL)
                .withPaths("ManagedScheduledExecutorDefinitionOnEJBServlet").withTestName(testname);
        runTest(requestURL);
    }

    @Assertion(id = "GIT:154",
            strategy = "A method that lacks the Asynchronous annotation does not run as an asynchronous method,"
                    + " even if it returns a CompletableFuture.")
    public void testNotAnAsynchronousMethod() {
        runTest(baseURL, testname);
    }

    @Assertion(id = "GIT:154", strategy = "ManagedScheduledExecutorService can schedule a task with a CronTrigger")
    public void testScheduleWithCronTrigger() {
        runTest(baseURL, testname);
    }

    @Assertion(id = "GIT:154",
            strategy = "ManagedScheduledExecutorService can schedule a task with a ZonedTrigger implementation that uses the LastExecution methods with ZonedDateTime parameters")
    public void testScheduleWithZonedTrigger() {
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
