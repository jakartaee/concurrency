/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import ee.jakarta.tck.concurrent.common.context.providers.IntContextProvider;
import ee.jakarta.tck.concurrent.common.context.providers.StringContextProvider;
import ee.jakarta.tck.concurrent.framework.EJBJNDIProvider;
import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.URLBuilder;
import ee.jakarta.tck.concurrent.framework.junit.anno.Assertion;
import ee.jakarta.tck.concurrent.framework.junit.anno.Challenge;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common.PACKAGE;
import ee.jakarta.tck.concurrent.framework.junit.anno.TestName;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;
import jakarta.enterprise.concurrent.spi.ThreadContextProvider;

@Web
@RunAsClient // Requires client testing due to multiple servlets and annotation configuration
@Common({ PACKAGE.CONTEXT, PACKAGE.CONTEXT_PROVIDERS })
public class ContextPropagationWebTests extends TestClient {

    @Deployment(name = "ContextPropagationTests")
    public static WebArchive createDeployment() {

        WebArchive war = ShrinkWrap.create(WebArchive.class, "ContextPropagationTests_web.war")
                .addPackages(true, ContextPropagationWebTests.class.getPackage())
                .deleteClasses(ContextServiceDefinitionBean.class)
                .addAsServiceProvider(ThreadContextProvider.class.getName(), IntContextProvider.class.getName(),
                        StringContextProvider.class.getName())
                .addAsServiceProvider(EJBJNDIProvider.class, ContextEJBProvider.WebProvider.class)
                .addAsWebInfResource(ContextPropagationWebTests.class.getPackage(), "web.xml", "web.xml")
                .addAsWebResource(ContextPropagationWebTests.class.getPackage(), "jspTests.jsp", "jspTests.jsp");

        return war;
    }

    @TestName
    private String testname;

    @ArquillianResource(JNDIServlet.class)
    private URL jndiURL;

    @ArquillianResource(JSPSecurityServlet.class)
    private URL jspURL;

    @ArquillianResource(ClassloaderServlet.class)
    private URL classloaderURL;

    @ArquillianResource(SecurityServlet.class)
    private URL securityURL;

    @ArquillianResource(ContextServiceDefinitionServlet.class)
    private URL contextURL;

    @ArquillianResource(ContextServiceDefinitionFromEJBServlet.class)
    private URL ejbContextURL;

    @Challenge(link = "https://github.com/jakartaee/concurrency/pull/206", version = "3.0.0",
            reason = "HttpServletRequest.getUserPrincipal behavior is unclear when accessed from another thread or the current user is changed")
    @Assertion(id = "GIT:154", strategy = "From a JSP, use a ContextService that is defined by a ContextServiceDefinition"
            + " elsewhere within the application to clear Security context from the thread of execution while the task is running.")
    public void testSecurityClearedContext() {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(jspURL).withPaths("jspTests.jsp").withTestName(testname);
        runTest(requestURL);
    }

    @Challenge(link = "https://github.com/jakartaee/concurrency/pull/206", version = "3.0.0",
            reason = "HttpServletRequest.getUserPrincipal behavior is unclear when accessed from another thread or the current user is changed")
    @Assertion(id = "GIT:154", strategy = "From a JSP, use a ContextService that is defined by a ContextServiceDefinition"
            + " elsewhere within the application to leave Security context unchanged on the executing thread.")
    public void testSecurityUnchangedContext() {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(jspURL).withPaths("jspTests.jsp").withTestName(testname);
        runTest(requestURL);
    }

    @Assertion(id = "GIT:154", strategy = "From a JSP, use a ManagedExecutorService that is defined by a ManagedExecutorDefinition "
            + "elsewhere within the application to propagate Security context.")
    public void testSecurityPropagatedContext() {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(jspURL).withPaths("jspTests.jsp").withTestName(testname);
        runTest(requestURL);
    }

    @Assertion(id = "SPEC:85 SPEC:76 SPEC:76.1 SPEC:76.2 SPEC:76.3 SPEC:77 SPEC:84 SPEC:2 SPEC:4.1",
            strategy = "Create proxy in servlet and pass it into ejb container, then verify JNDI Context.")
    public void testJNDIContextAndCreateProxyInServlet() {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(jndiURL).withPaths("JNDIServlet").withTestName(testname);
        String resp = runTestWithResponse(requestURL, null);
        this.assertStringInResponse(testname + "failed to get correct result.", "JNDIContextWeb", resp);
    }

    @Challenge(link = "https://github.com/jakartaee/concurrency/pull/260", version = "3.0.0",
            reason = "This test will return JNDIContextWeb because we are running with web.xml and not ejb-jar.xml")
    @Assertion(id = "SPEC:85 SPEC:76 SPEC:76.1 SPEC:76.2 SPEC:76.3 SPEC:77 SPEC:84 SPEC:3 SPEC:3.1 SPEC:3.2 SPEC:3.3 SPEC:3.4 SPEC:4",
    strategy = "Create proxy in servlet and pass it into ejb container, then verify JNDI Context.")
    public void testJNDIContextAndCreateProxyInEJB() {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(jndiURL).withPaths("JNDIServlet").withTestName(testname);
        String resp = runTestWithResponse(requestURL, null);
        this.assertStringInResponse(testname + "failed to get correct result.", "JNDIContextEJB", resp);
    }

    @Assertion(id = "SPEC:85 SPEC:76 SPEC:76.1 SPEC:76.2 SPEC:76.3 SPEC:77 SPEC:84 SPEC:4.2 SPEC:4.4;",
            strategy = "Create proxy in servlet and pass it into ejb container, then verify classloader.")
    public void testClassloaderAndCreateProxyInServlet() {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(securityURL).withPaths("ClassloaderServlet")
                .withTestName(testname);
        String resp = runTestWithResponse(requestURL, null);
        this.assertStringInResponse(testname + "failed to get correct result.", TestConstants.complexReturnValue, resp);
    }

    @Assertion(id = "SPEC:85 SPEC:76 SPEC:76.1 SPEC:76.2 SPEC:76.3 SPEC:77 SPEC:84 SPEC:4.3 SPEC:4.4",
            strategy = "Create proxy in servlet and pass it into ejb container, then verify permission.")
    public void testSecurityAndCreateProxyInServlet() {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(classloaderURL).withPaths("SecurityServlet")
                .withTestName(testname);
        String resp = runTestWithResponse(requestURL, null);
        this.assertStringInResponse(testname + "failed to get correct result.", TestConstants.complexReturnValue, resp);
    }

    @Assertion(id = "GIT:154", strategy = "ContextServiceDefinition with all attributes configured propagates/clears/ignores context types as configured")
    public void testContextServiceDefinitionAllAttributes() throws Throwable {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(contextURL).withPaths("ContextServiceDefinitionServlet")
                .withTestName(testname);
        runTest(requestURL);
    }

    @Assertion(id = "GIT:154", strategy = "ContextServiceDefinition with all attributes configured propagates/clears/ignores context types as configured")
    public void testContextServiceDefinitionFromEJBAllAttributes() throws Throwable {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL)
                .withPaths("ContextServiceDefinitionFromEJBServlet").withTestName(testname);
        runTest(requestURL);
    }

    @Assertion(id = "GIT:154", strategy = "A ContextServiceDefinition with minimal attributes configured clears transaction context and propagates other types.")
    public void testContextServiceDefinitionDefaults() throws Throwable {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(contextURL).withPaths("ContextServiceDefinitionServlet")
                .withTestName(testname);
        runTest(requestURL);
    }

    @Assertion(id = "GIT:154", strategy = "A ContextServiceDefinition defined in an EJB with minimal attributes configured clears transaction context and propagates other types.")
    public void testContextServiceDefinitionFromEJBDefaults() throws Throwable {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(ejbContextURL)
                .withPaths("ContextServiceDefinitionFromEJBServlet").withTestName(testname);
        runTest(requestURL);
    }

    @Assertion(id = "GIT:154", strategy = "A ContextServiceDefinition can specify a third-party context type to be propagated/cleared/ignored")
    public void testContextServiceDefinitionWithThirdPartyContext() throws Throwable {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(contextURL).withPaths("ContextServiceDefinitionServlet")
                .withTestName(testname);
        runTest(requestURL);
    }

    @Assertion(id = "GIT:154",
            strategy = "ContextService contextualizes a java.util.function.Consumer, which can be supplied as a dependent stage action to an unmanaged CompletableFuture."
            + " The dependent stage action runs with the thread context of the thread that contextualizes the Consumer, per the configuration of the ContextServiceDefinition.")
    public void testContextualConsumer() throws Throwable {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(contextURL).withPaths("ContextServiceDefinitionServlet")
                .withTestName(testname);
        runTest(requestURL);
    }


    @Challenge(link = "https://github.com/jakartaee/concurrency/issues/253", version = "3.0.1",
            reason = "Assertions on results[0] and results[1] are both invalid because treating those two UNCHANGED context types as though they were CLEARED.")
    @Assertion(id = "GIT:154",
        strategy = "A ContextService contextualizes a Function, which can be supplied as a dependent stage action to an unmanaged CompletableFuture."
            + " The dependent stage action runs with the thread context of the thread that contextualizes the Function,"
            + " per the configuration of the ContextServiceDefinition.")
    public void testContextualFunction() throws Throwable {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(contextURL).withPaths("ContextServiceDefinitionServlet")
                .withTestName(testname);
        runTest(requestURL);
    }

    @Assertion(id = "GIT:154",
            strategy = "A ContextService contextualizes a Supplier, which can be supplied as a dependent stage action to an unmanaged CompletableFuture."
            + " The dependent stage action runs with the thread context of the thread that contextualizes the Supplier,"
            + " per the configuration of the ContextServiceDefinition.")
    public void testContextualSupplier() throws Throwable {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(contextURL).withPaths("ContextServiceDefinitionServlet")
                .withTestName(testname);
        runTest(requestURL);
        requestURL = URLBuilder.get().withBaseURL(ejbContextURL).withPaths("ContextServiceDefinitionFromEJBServlet")
                .withTestName(testname);
        runTest(requestURL);
    }

    @Assertion(id = "GIT:154", strategy = "ContextService can create a contextualized copy of an unmanaged CompletableFuture.")
    public void testCopyWithContextCapture() throws Throwable {
        URLBuilder requestURL = URLBuilder.get().withBaseURL(contextURL).withPaths("ContextServiceDefinitionServlet")
                .withTestName(testname);
        runTest(requestURL);
    }
}
