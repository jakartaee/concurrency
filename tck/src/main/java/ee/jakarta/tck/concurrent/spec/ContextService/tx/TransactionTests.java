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
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.framework.TestClient;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;

@Web
public class TransactionTests extends TestClient {

	@ArquillianResource
	URL baseURL;
	
	@Deployment(name="TransactionTests", testable=false)
	public static WebArchive createDeployment() {
		return ShrinkWrap.create(WebArchive.class)
				.addPackages(true, getFrameworkPackage(), TransactionTests.class.getPackage());
	}
	
	@Override
	protected String getServletPath() {
		return "TransactionServlet";
	}

	/*
	 * @testName: testTransactionOfExecuteThreadAndCommit
	 *
	 * @assertion_ids: CONCURRENCY:SPEC:86; CONCURRENCY:SPEC:87;
	 * CONCURRENCY:SPEC:88; CONCURRENCY:SPEC:89; CONCURRENCY:SPEC:90;
	 * CONCURRENCY:SPEC:31.2; CONCURRENCY:SPEC:32; CONCURRENCY:SPEC:34;
	 * CONCURRENCY:SPEC:8.1; CONCURRENCY:SPEC:9;
	 *
	 * @test_Strategy: Get UserTransaction in Servlet and insert 2 row data. Create
	 * a proxy with Transaction property "TransactionOfExecuteThread". Invoke proxy
	 * in Servlet. In proxy, insert 1 row data commit in Servlet. Expect insert
	 * actions in servlet and in proxy will be committed.
	 */
	@Test
	public void testTransactionOfExecuteThreadAndCommit() {		
		runTest(baseURL);
	}

	/*
	 * @testName: testTransactionOfExecuteThreadAndRollback
	 *
	 * @assertion_ids: CONCURRENCY:SPEC:86; CONCURRENCY:SPEC:87;
	 * CONCURRENCY:SPEC:88; CONCURRENCY:SPEC:89; CONCURRENCY:SPEC:90;
	 * CONCURRENCY:SPEC:31.2; CONCURRENCY:SPEC:32; CONCURRENCY:SPEC:34;
	 * CONCURRENCY:SPEC:8.1; CONCURRENCY:SPEC:9;
	 *
	 * @test_Strategy: Get UserTransaction in Servlet and insert 2 row data. Create
	 * a proxy with Transaction property "TransactionOfExecuteThread". Invoke proxy
	 * in Servlet. In proxy, insert 1 row data rollback in Servlet. Expect insert
	 * actions in servlet and in proxy will be roll backed.
	 */
	@Test
	public void testTransactionOfExecuteThreadAndRollback() {
		runTest(baseURL);
	}

	/*
	 * @testName: testSuspendAndCommit
	 *
	 * @assertion_ids: CONCURRENCY:SPEC:86; CONCURRENCY:SPEC:87;
	 * CONCURRENCY:SPEC:88; CONCURRENCY:SPEC:89; CONCURRENCY:SPEC:90;
	 * CONCURRENCY:SPEC:31.2; CONCURRENCY:SPEC:32; CONCURRENCY:SPEC:34;
	 * CONCURRENCY:SPEC:8.1; CONCURRENCY:SPEC:9;
	 *
	 * @test_Strategy: Get UserTransaction in Servlet and insert 2 row data. Create
	 * a proxy with Transaction property "SUSPEND". Invoke proxy in Servlet. In
	 * proxy, get UserTransaction then insert 1 row data and commit Rollback in
	 * Servlet. Expect insert action in servlet will be roll backed and insert
	 * action in proxy will be committed.
	 */
	@Test
	public void testSuspendAndCommit() {
		runTest(baseURL);
	}

	/*
	 * @testName: testSuspendAndRollback
	 *
	 * @assertion_ids: CONCURRENCY:SPEC:86; CONCURRENCY:SPEC:87;
	 * CONCURRENCY:SPEC:88; CONCURRENCY:SPEC:89; CONCURRENCY:SPEC:90;
	 * CONCURRENCY:SPEC:31.2; CONCURRENCY:SPEC:32; CONCURRENCY:SPEC:34;
	 * CONCURRENCY:SPEC:8.1; CONCURRENCY:SPEC:9;
	 *
	 * @test_Strategy: Get UserTransaction in Servlet and insert 2 row data. Create
	 * a proxy with Transaction property "SUSPEND". Invoke proxy in Servlet. In
	 * proxy, get UserTransaction then insert 1 row data and rollback Commit in
	 * Servlet. Expect insert action in servlet will be committed and insert action
	 * in proxy will be roll backed.
	 */
	@Test
	public void testSuspendAndRollback() {
		runTest(baseURL);
	}

	/*
	 * @testName: testDefaultAndCommit
	 *
	 * @assertion_ids: CONCURRENCY:SPEC:86; CONCURRENCY:SPEC:87;
	 * CONCURRENCY:SPEC:88; CONCURRENCY:SPEC:89; CONCURRENCY:SPEC:90;
	 * CONCURRENCY:SPEC:91; CONCURRENCY:SPEC:31.2; CONCURRENCY:SPEC:32;
	 * CONCURRENCY:SPEC:34; CONCURRENCY:SPEC:8.1; CONCURRENCY:SPEC:9;
	 *
	 * @test_Strategy: Get UserTransaction in Servlet and insert 2 row data. Create
	 * a proxy with default Transaction property. Invoke proxy in Servlet. In proxy,
	 * get UserTransaction then insert 1 row data and commit Rollback in Servlet.
	 * Expect insert action in servlet will be roll backed and insert action in
	 * proxy will be committed.
	 */
	@Test
	public void testDefaultAndCommit() {
		runTest(baseURL);
	}

}
