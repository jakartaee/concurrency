/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package ee.jakarta.tck.concurrent.spec.Platform.virtual;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.framework.junit.anno.Assertion;
import ee.jakarta.tck.concurrent.framework.junit.anno.Full;
import ee.jakarta.tck.concurrent.framework.junit.anno.TestName;

@Full
@RunAsClient // Requires client testing due to annotation configuration
public class VirtualFullTests extends TestClient {

    @ArquillianResource(VirtualThreadServlet.class)
    private URL baseURL;

    @Deployment(name = "VirtualThreadTests")
    public static EnterpriseArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "VirtualTests_web.war")
                .addClasses(VirtualThreadServlet.class);

        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "VirtualTests.ear")
                .addAsManifestResource(VirtualFullTests.class.getPackage(), "application.xml", "application.xml")
                .addAsModules(war);

        return ear;
    }

    @TestName
    private String testname;

    @Override
    protected String getServletPath() {
        return "VirtualThreadServlet";
    }

    @Assertion(id = "GIT:414", strategy = "Tests that a managed executor with virtual = false never uses a virtual thread.")
    public void testPlatformExecutor() throws Exception {
        runTest(baseURL, testname);
    }

    @Assertion(id = "GIT:414", strategy = "Tests that a managed executor with virtual = true can use a virtual thread,"
            + " or returns a platform thread if it is not capable of providing virtual threads.")
    public void testVirtualExecutor() throws Exception {
        runTest(baseURL, testname);
    }

    @Assertion(id = "GIT:414", strategy = "Tests that a managed scheduled executor with virtual = false never uses a virtual thread.")
    public void testPlatformScheduledExecutor() throws Exception {
        runTest(baseURL, testname);
    }

    @Assertion(id = "GIT:414", strategy = "Tests that a managed scheduled executor with virtual = true can use a virtual thread,"
            + " or returns a platform thread if it is not capable of providing virtual threads.")
    public void testVirtualScheduledExecutor() throws Exception {
        runTest(baseURL, testname);
    }

    @Assertion(id = "GIT:414", strategy = "Tests that a thread factory with virtual = false never returns a virtual thread.")
    public void testPlatformThreadFactory() {
        runTest(baseURL, testname);
    }

    @Assertion(id = "GIT:414", strategy = "Tests that a thread factory with virtual = true can return and use a virtual thread,"
            + " or returns a platform thread if it is not capable of providing virtual threads.")
    public void testVirtualThreadFactory() {
        runTest(baseURL, testname);
    }
    
    @Assertion(id = "GIT:414", strategy = "Tests that a ForkJoinPool created from a thread factory with virtual = true"
            + " or virtual = false always uses a platform thread which is a restriction of Java.")
    public void testVirtualThreadFactoryForkJoinPool() {
        runTest(baseURL, testname);
    }
}
