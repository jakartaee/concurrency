/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.enterprise.concurrent.spec.ManagedExecutorService.tx;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.testng.annotations.Test;

import jakarta.enterprise.concurrent.tck.framework.TestClient;
import jakarta.enterprise.concurrent.tck.framework.TestUtil;
import jakarta.enterprise.concurrent.tck.framework.URLBuilder;

public class TransactionTests extends TestClient {

	@ArquillianResource
	URL baseURL;

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
	@Test
	public void testCommitTransactionWithManagedExecutorService() {
		URL url;
		String resp = null;
		try {
			url = URLBuilder.get().withBaseURL(baseURL).withQueries(Constants.PARAM_COMMIT + "=true").withTestName("invokeTest").build();
			resp = TestUtil.getResponse(url.openConnection());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals(testName + " failed to get successful result.", Message.SUCCESSMESSAGE, // expected
				resp); // actual
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
		URL url;
		String resp = null;
		try {
			url = URLBuilder.get().withBaseURL(baseURL).withQueries(Constants.PARAM_COMMIT + "=false").withTestName("invokeTest").build();
			resp = TestUtil.getResponse(url.openConnection());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals(testName + " failed to get successful result.", Message.SUCCESSMESSAGE, // expected
				resp); // actual
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
		URL url;
		String resp = null;
		try {
			url = URLBuilder.get().withBaseURL(baseURL).withQueries(Constants.PARAM_COMMIT + "=" + Constants.PARAM_VALUE_CANCEL).withTestName("invokeTest").build();
			resp = TestUtil.getResponse(url.openConnection());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals(testName + " failed to get successful result.", Message.SUCCESSMESSAGE, // expected
				resp); // actual
	}

}
