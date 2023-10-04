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
package ee.jakarta.tck.concurrent.spec.Platform.dd;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import ee.jakarta.tck.concurrent.common.context.providers.IntContextProvider;
import ee.jakarta.tck.concurrent.common.context.providers.StringContextProvider;
import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.framework.junit.anno.Assertion;
import ee.jakarta.tck.concurrent.framework.junit.anno.Challenge;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common.PACKAGE;
import ee.jakarta.tck.concurrent.framework.junit.anno.TestName;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;
import jakarta.enterprise.concurrent.spi.ThreadContextProvider;

/**
 * Covers context-service, managed-executor, managed-scheduled-executor, and
 * managed-thread-factory defined in a deployment descriptor.
 */
@Web
@RunAsClient // Requires client testing due to annotation configuration
@Common({ PACKAGE.CONTEXT, PACKAGE.CONTEXT_PROVIDERS })
public class DeploymentDescriptorWebTests extends TestClient {

    @ArquillianResource(DeploymentDescriptorServlet.class)
    private URL baseURL;

    @Deployment(name = "DeploymentDescriptorTests")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "DeploymentDescriptorTests_web.war")
                .addPackages(false, DeploymentDescriptorWebTests.class.getPackage())
                .addAsServiceProvider(ThreadContextProvider.class.getName(), IntContextProvider.class.getName(),
                        StringContextProvider.class.getName())
                .addAsWebInfResource(DeploymentDescriptorWebTests.class.getPackage(), "web.xml", "web.xml");
    }

    @TestName
    private String testname;

    @Override
    protected String getServletPath() {
        return "DeploymentDescriptorServlet";
    }

    @Assertion(id = "GIT:186", strategy = "Tests context-service defined in a deployment descriptor.")
    public void testDeploymentDescriptorDefinesContextService() {
        runTest(baseURL, testname);
    }

    @Assertion(id = "GIT:186", strategy = "Tests managed-executor defined in a deployment descriptor.")
    public void testDeploymentDescriptorDefinesManagedExecutor() {
        runTest(baseURL, testname);
    }

    @Assertion(id = "GIT:186", strategy = "Tests managed-scheduled-executor defined in a deployment descriptor.")
    public void testDeploymentDescriptorDefinesManagedScheduledExecutor() {
        runTest(baseURL, testname);
    }

    @Challenge(link = "https://github.com/jakartaee/concurrency/issues/226", version = "3.0.0")
    @Assertion(id = "GIT:186", strategy = "Tests managed-thread-factory defined in a deployment descriptor.")
    public void testDeploymentDescriptorDefinesManagedThreadFactory() {
        runTest(baseURL, testname);
    }
}
