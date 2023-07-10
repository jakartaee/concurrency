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

package ee.jakarta.tck.concurrent.spec.ManagedThreadFactory.tx;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.framework.URLBuilder;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;

@Web @RunAsClient //Requires client testing due to annotation configuration
public class TransactionTests extends TestClient {

	@ArquillianResource
	URL baseURL;
	
	@Deployment(name="TransactionTests")
	public static WebArchive createDeployment() {
		return ShrinkWrap.create(WebArchive.class)
				.addPackages(true,  TransactionTests.class.getPackage());
	}

	/*
	 * @testName: testRollbackTransactionWithManagedThreadFactory
	 * 
	 * @assertion_ids:
	 * CONCURRENCY:SPEC:107;CONCURRENCY:SPEC:108;CONCURRENCY:SPEC:110;
	 * CONCURRENCY:SPEC:111;CONCURRENCY:SPEC:115;CONCURRENCY:SPEC:116;
	 * CONCURRENCY:SPEC:8.1;CONCURRENCY:SPEC:9;CONCURRENCY:SPEC:107;
	 * CONCURRENCY:SPEC:108;CONCURRENCY:SPEC:109;
	 *
	 * 
	 * @test_Strategy: get UserTransaction inside one task submitted by
	 * ManagedThreadFactory. test roll back function in the submitted task.
	 */
	@Test
	@Order(1)
	public void testRollbackTransactionWithManagedThreadFactory() {
		runTest(URLBuilder.get().withBaseURL(baseURL).withPaths("TransactionServlet").withQueries(Constants.COMMIT_FALSE).withTestName("transactionTest"));
	}
	
	   /*
     * @testName: testCommitTransactionWithManagedThreadFactory
     * 
     * @assertion_ids:
     * CONCURRENCY:SPEC:107;CONCURRENCY:SPEC:108;CONCURRENCY:SPEC:110;
     * CONCURRENCY:SPEC:111;CONCURRENCY:SPEC:115;CONCURRENCY:SPEC:116;
     * CONCURRENCY:SPEC:8.1;CONCURRENCY:SPEC:9;CONCURRENCY:SPEC:107;
     * CONCURRENCY:SPEC:109;CONCURRENCY:SPEC:113;CONCURRENCY:SPEC:118;
     * CONCURRENCY:SPEC:113;CONCURRENCY:SPEC:118;
     *
     * @test_Strategy: get UserTransaction inside one task submitted by
     * ManagedThreadFactory.it support user-managed global transaction demarcation
     * using the jakarta.transaction.UserTransaction interface.
     */
    @Test //TODO rewrite test logic to avoid duplicate key violation
    @Order(2)
    public void testCommitTransactionWithManagedThreadFactory() throws InterruptedException {
        runTest(URLBuilder.get().withBaseURL(baseURL).withPaths("TransactionServlet").withQueries(Constants.COMMIT_TRUE).withTestName("transactionTest"));
    }

	/*
	 * @testName: testCancelTransactionWithManagedThreadFactory
	 * 
	 * @assertion_ids:
	 * CONCURRENCY:SPEC:107;CONCURRENCY:SPEC:108;CONCURRENCY:SPEC:110;
	 * CONCURRENCY:SPEC:111;CONCURRENCY:SPEC:115;CONCURRENCY:SPEC:116;
	 * CONCURRENCY:SPEC:8.1;CONCURRENCY:SPEC:9;
	 * 
	 * @test_Strategy: get UserTransaction inside one task submitted by
	 * ManagedThreadFactory.cancel the task after submit one task.
	 */
	@Test
	@Order(3)
	public void testCancelTransactionWithManagedThreadFactory() {
		runTest(URLBuilder.get().withBaseURL(baseURL).withPaths("TransactionServlet").withQueries(Constants.COMMIT_CANCEL).withTestName("cancelTest"));
	}
}
