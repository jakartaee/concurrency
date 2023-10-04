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

package ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.servlet;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URL;
import java.util.Properties;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.URLBuilder;
import ee.jakarta.tck.concurrent.framework.junit.anno.Assertion;
import ee.jakarta.tck.concurrent.framework.junit.anno.TestName;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;

@Web
@RunAsClient // Requires client testing due to POST requests
public class ContextPropagationServletTests extends TestClient {

    private static final String APP_NAME_PROXY = "ContextPropagationServletTests.Proxy";
    private static final String APP_NAME_WORK = "ContextPropagationServletTests.Work";
    private static final String APP_NAME_DESERIALIZE = "ContextPropagationServletTests.Deserialize";

    @Deployment(name = APP_NAME_PROXY)
    public static WebArchive createDeployment1() {
        return ShrinkWrap.create(WebArchive.class, APP_NAME_PROXY + ".war")
                .addPackages(true, ContextPropagationServletTests.class.getPackage())
                .deleteClass(WorkInterfaceServlet.class)
                .addAsWebInfResource(ContextPropagationServletTests.class.getPackage(), "web.xml", "web.xml");
    }

    @Deployment(name = APP_NAME_WORK)
    public static WebArchive createDeployment2() {
        return ShrinkWrap.create(WebArchive.class, APP_NAME_WORK + ".war")
                .addPackages(true, ContextPropagationServletTests.class.getPackage())
                .deleteClass(ProxyCreatorServlet.class);
    }

    @Deployment(name = APP_NAME_DESERIALIZE)
    public static WebArchive createDeployment3() {
        return ShrinkWrap.create(WebArchive.class, APP_NAME_DESERIALIZE + ".war")
                .addPackages(true, ContextPropagationServletTests.class.getPackage())
                .deleteClasses(ProxyCreatorServlet.class, WorkInterfaceServlet.class);
    }

    @TestName
    private String testname;

    @ArquillianResource
    @OperateOnDeployment(APP_NAME_PROXY)
    private URL baseURL;

    @ArquillianResource
    @OperateOnDeployment(APP_NAME_WORK)
    private URL workInterfaceURL;

    @Override
    protected String getServletPath() {
        return "ProxyCreatorServlet";
    }

    @Assertion(id = "SPEC:85 SPEC:76 SPEC:76.1 SPEC:76.2 SPEC:76.3 SPEC:77 SPEC:78 SPEC:82 SPEC:84",
            strategy = "Create proxy in servlet and pass it to other servlet in other web module, then verify JNDI Context.")
    public void testJNDIContextInServlet() {
        URL proxyURL = URLBuilder.get().withBaseURL(workInterfaceURL).withPaths("WorkInterfaceServlet").build();
        URLBuilder requestURL = URLBuilder.get().withBaseURL(baseURL).withPaths(getServletPath())
                .withTestName(testname);

        Properties props = new Properties();
        props.put("proxyURL", proxyURL.toString());
        props.put(TEST_METHOD, testname);

        String resp = runTestWithResponse(requestURL, props);
        assertNotNull("Response should not be null", resp);
        assertStringInResponse(testname + " failed to get correct result.", "JNDIContextWeb", resp.trim());
    }

    @Assertion(id = "SPEC:85 SPEC:76 SPEC:76.1 SPEC:76.2 SPEC:76.3 SPEC:77 SPEC:78 SPEC:82 SPEC:84",
            strategy = "Create proxy in servlet and pass it into other servlet in other web module, then verify classloader.")
    public void testClassloaderInServlet() {
        URL proxyURL = URLBuilder.get().withBaseURL(workInterfaceURL).withPaths("WorkInterfaceServlet").build();
        URLBuilder requestURL = URLBuilder.get().withBaseURL(baseURL).withPaths(getServletPath())
                .withTestName(testname);

        Properties props = new Properties();
        props.put("proxyURL", proxyURL.toString());
        props.put(TEST_METHOD, testname);

        String resp = runTestWithResponse(requestURL, props);
        assertNotNull("Response should not be null", resp);
        assertStringInResponse(testname + " failed to get correct result.", TestConstants.complexReturnValue,
                resp.trim());
    }
}
