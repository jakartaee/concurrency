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

package ee.jakarta.tck.concurrent.spec.ManagedExecutorService.tx;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Order;

import ee.jakarta.tck.concurrent.common.transaction.Constants;
import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.framework.URLBuilder;
import ee.jakarta.tck.concurrent.framework.junit.anno.Assertion;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common.PACKAGE;
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
    
    // TODO rewrite test logic to avoid duplicate key violation

    @Order(1)
    @Assertion(id = "SPEC:31.3 SPEC:39.3 SPEC:92.2 SPEC:92.3",
    strategy = "Get UserTransaction inside one task submitted by ManagedExecutorService."
            + " Test rollback function in the submitted task.")
    public void testRollbackTransactionWithManagedExecutorService() {
        runTest(URLBuilder.get().withBaseURL(baseURL).withPaths(Constants.CONTEXT_PATH)
                .withQueries(Constants.COMMIT_FALSE).withTestName("transactionTest"));
    }

    @Order(2)
    @Assertion(id = "SPEC:30 SPEC:31 SPEC:31.1 SPEC:31.2 SPEC:32 SPEC:33 SPEC:34 SPEC:36 SPEC:38 SPEC:8.1"
            + " SPEC:9 SPEC:39 SPEC:39.1 SPEC:39.2 SPEC:4.1 SPEC:4.4 SPEC:92.2 SPEC:92.3 SPEC:92.5 SPEC:41",
            strategy = "Get UserTransaction inside one task submitted by ManagedExecutorService."
                    + " It support user-managed global transaction demarcation using the jakarta.transaction.UserTransaction interface.")
    public void testCommitTransactionWithManagedExecutorService() {
        runTest(URLBuilder.get().withBaseURL(baseURL).withPaths(Constants.CONTEXT_PATH)
                .withQueries(Constants.COMMIT_TRUE).withTestName("transactionTest"));
    }

    @Order(3)
    @Assertion(id = " SPEC:112 SPEC:35 SPEC:68 SPEC:91.4",
            strategy = "Get UserTransaction inside one task submitted by ManagedExecutorService.cancel the task after submit one task.")
    public void testCancelTransactionWithManagedExecutorService() {
        runTest(URLBuilder.get().withBaseURL(baseURL).withPaths(Constants.CONTEXT_PATH)
                .withQueries(Constants.COMMIT_CANCEL).withTestName("cancelTest"));
    }
}
