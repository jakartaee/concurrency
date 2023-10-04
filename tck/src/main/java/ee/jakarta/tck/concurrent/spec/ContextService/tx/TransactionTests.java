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

package ee.jakarta.tck.concurrent.spec.ContextService.tx;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.framework.junit.anno.Assertion;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common.PACKAGE;
import ee.jakarta.tck.concurrent.framework.junit.anno.TestName;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;

@Web
@RunAsClient // Requires client testing due to annotation configuration
@Common({ PACKAGE.TRANSACTION })
public class TransactionTests extends TestClient {

    @ArquillianResource
    private URL baseURL;

    @Deployment(name = "TransactionTests")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addPackages(true, TransactionTests.class.getPackage());
    }

    @Override
    protected String getServletPath() {
        return "TransactionServlet";
    }

    @TestName
    private String testname;

    @Assertion(id = "SPEC:86 SPEC:87 SPEC:88 SPEC:89 SPEC:90 SPEC:31.2 SPEC:32 SPEC:34 SPEC:8.1 SPEC:9",
            strategy = "Get UserTransaction in Servlet and insert 2 row data."
                    + " Create a proxy with Transaction property TransactionOfExecuteThread."
                    + " Invoke proxy in Servlet. In proxy, insert 1 row data commit in Servlet."
                    + " Expect insert actions in servlet and in proxy will be committed.")
    public void testTransactionOfExecuteThreadAndCommit() {
        runTest(baseURL, testname);
    }
    
    @Assertion(id = "SPEC:86 SPEC:87 SPEC:88 SPEC:89 SPEC:90 SPEC:31.2 SPEC:32 SPEC:34 SPEC:8.1 SPEC:9",
            strategy = "Get UserTransaction in Servlet and insert 2 row data."
                    + " Create a proxy with Transaction property TransactionOfExecuteThread."
                    + " Invoke proxy in Servlet. In proxy, insert 1 row data rollback in Servlet."
                    + " Expect insert actions in servlet and in proxy will be roll backed.")
    public void testTransactionOfExecuteThreadAndRollback() {
        runTest(baseURL, testname);
    }

    @Assertion(id = "SPEC:86 SPEC:87 SPEC:88 SPEC:89 SPEC:90 SPEC:31.2 SPEC:32 SPEC:34 SPEC:8.1 SPEC:9",
            strategy = "Get UserTransaction in Servlet and insert 2 row data. "
                    + "Create a proxy with Transaction property SUSPEND. Invoke proxy in Servlet."
                    + " In proxy, get UserTransaction then insert 1 row data and commit Rollback in Servlet."
                    + " Expect insert action in servlet will be roll backed and insert action in proxy will be committed.")
    public void testSuspendAndCommit() {
        runTest(baseURL, testname);
    }

    @Assertion(id = "SPEC:86 SPEC:87 SPEC:88 SPEC:89 SPEC:90 SPEC:31.2 SPEC:32 SPEC:34 SPEC:8.1 SPEC:9",
            strategy = "Get UserTransaction in Servlet and insert 2 row data. Create a proxy with Transaction property SUSPEND."
                    + " Invoke proxy in Servlet. In proxy, get UserTransaction then insert 1 row data and rollback Commit in Servlet."
                    + " Expect insert action in servlet will be committed and insert action in proxy will be roll backed.")
    public void testSuspendAndRollback() {
        runTest(baseURL, testname);
    }

    @Assertion(id = "SPEC:86; SPEC:87; SPEC:88; SPEC:89; SPEC:90; SPEC:91; SPEC:31.2; SPEC:32; SPEC:34; SPEC:8.1; SPEC:9",
            strategy = "Get UserTransaction in Servlet and insert 2 row data. Create a proxy with default Transaction property."
                    + " Invoke proxy in Servlet. In proxy, get UserTransaction then insert 1 row data and commit Rollback in Servlet."
                    + " Expect insert action in servlet will be roll backed and insert action in proxy will be committed.")
    public void testDefaultAndCommit() {
        runTest(baseURL, testname);
    }

}
