/*
 * Copyright (c) 2013, 2022 Oracle and/or its affiliates. All rights reserved.
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
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.framework.URLBuilder;

public class TransactionTests extends TestClient {

	@ArquillianResource
	URL baseURL;
	
	@Deployment(name="TransactionTests", testable=false)
	public static WebArchive createDeployment() {
		return ShrinkWrap.create(WebArchive.class)
				.addPackages(true, getFrameworkPackage(), TransactionTests.class.getPackage());
	}

	/*
	 * @testName: testCommitTransactionWithManagedExecutorService
	 * 
	 * @assertion_ids:
	 * CONCURRENCY:SPEC:30;CONCURRENCY:SPEC:31;CONCURRENCY:SPEC:31.1;
	 * CONCURRENCY:SPEC:31.2;CONCURRENCY:SPEC:32;CONCURRENCY:SPEC:33;
	 * CONCURRENCY:SPEC:34;CONCURRENCY:SPEC:36; CONCURRENCY:SPEC:38;
	 * CONCURRENCY:SPEC:8.1;CONCURRENCY:SPEC:9;CONCURRENCY:SPEC:39;
	 * CONCURRENCY:SPEC:39.1;CONCURRENCY:SPEC:39.2;CONCURRENCY:SPEC:4.1;
	 * CONCURRENCY:SPEC:4.4;CONCURRENCY:SPEC:92.2;CONCURRENCY:SPEC:92.3;
	 * CONCURRENCY:SPEC:92.5;CONCURRENCY:SPEC:41;
	 * 
	 * @test_Strategy: get UserTransaction inside one task submitted by
	 * ManagedExecutorService.it support user-managed global transaction demarcation
	 * using the jakarta.transaction.UserTransaction interface.
	 */
	@Test(dependsOnMethods= {"testRollbackTransactionWithManagedExecutorService"}) //TODO rewrite test logic to avoid duplicate key violation
	public void testCommitTransactionWithManagedExecutorService() {
		runTest(URLBuilder.get().withBaseURL(baseURL).withPaths(Constants.CONTEXT_PATH).withQueries(Constants.COMMIT_TRUE).withTestName("transactionTest"));
	}

	/*
	 * @testName: testRollbackTransactionWithManagedExecutorService
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:31.3;CONCURRENCY:SPEC:39.3;
	 * CONCURRENCY:SPEC:92.2;CONCURRENCY:SPEC:92.3;
	 * 
	 * @test_Strategy: get UserTransaction inside one task submitted by
	 * ManagedExecutorService. test roll back function in the submitted task.
	 */
	@Test
	public void testRollbackTransactionWithManagedExecutorService() {
		runTest(URLBuilder.get().withBaseURL(baseURL).withPaths(Constants.CONTEXT_PATH).withQueries(Constants.COMMIT_FALSE).withTestName("transactionTest"));
	}

	/*
	 * @testName: testCancelTransactionWithManagedExecutorService
	 * 
	 * @assertion_ids:
	 * CONCURRENCY:SPEC:112;CONCURRENCY:SPEC:35;CONCURRENCY:SPEC:68;CONCURRENCY:
	 * SPEC:91.4;
	 * 
	 * @test_Strategy: get UserTransaction inside one task submitted by
	 * ManagedExecutorService.cancel the task after submit one task.
	 */
	@Test
	public void testCancelTransactionWithManagedExecutorService() {
		runTest(URLBuilder.get().withBaseURL(baseURL).withPaths(Constants.CONTEXT_PATH).withQueries(Constants.COMMIT_CANCEL).withTestName("cancelTest"));
	}
}
