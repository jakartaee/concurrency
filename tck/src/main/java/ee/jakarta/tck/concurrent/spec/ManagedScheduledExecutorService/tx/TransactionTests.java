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

package jakarta.enterprise.concurrent.spec.ManagedScheduledExecutorService.tx;

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
	 * @testName: testCommitTransactionWithManagedScheduledExecutorService
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:63;CONCURRENCY:SPEC:64;
	 * CONCURRENCY:SPEC:65;CONCURRENCY:SPEC:66;CONCURRENCY:SPEC:67;
	 * CONCURRENCY:SPEC:69;CONCURRENCY:SPEC:8.1;CONCURRENCY:SPEC:9;
	 * CONCURRENCY:SPEC:71;CONCURRENCY:SPEC:72;
	 * 
	 * @test_Strategy: get UserTransaction inside one task submitted by
	 * ManagedScheduledExecutorService.it support user-managed global transaction
	 * demarcation using the jakarta.transaction.UserTransaction interface.
	 */
	@Test
	public void testCommitTransactionWithManagedScheduledExecutorService() {
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
	 * @testName: testRollbackTransactionWithManagedScheduledExecutorService
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:63;CONCURRENCY:SPEC:64;
	 * CONCURRENCY:SPEC:65;CONCURRENCY:SPEC:66;
	 * CONCURRENCY:SPEC:67;CONCURRENCY:SPEC:69;
	 * CONCURRENCY:SPEC:71;CONCURRENCY:SPEC:72;
	 * 
	 * @test_Strategy: get UserTransaction inside one task submitted by
	 * ManagedScheduledExecutorService. test roll back function in the submitted
	 * task.
	 */
	@Test
	public void testRollbackTransactionWithManagedScheduledExecutorService() {
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
	 * @testName: testCancelTransactionWithManagedScheduledExecutorService
	 * 
	 * @assertion_ids: CONCURRENCY:SPEC:42.1;CONCURRENCY:SPEC:42.3;
	 * CONCURRENCY:SPEC:63;CONCURRENCY:SPEC:64;
	 * CONCURRENCY:SPEC:65;CONCURRENCY:SPEC:66;
	 * CONCURRENCY:SPEC:67;CONCURRENCY:SPEC:69;
	 * CONCURRENCY:SPEC:71;CONCURRENCY:SPEC:72;
	 * 
	 * @test_Strategy: get UserTransaction inside one task submitted by
	 * ManagedScheduledExecutorService.cancel the task after submit one task.
	 */
	@Test
	public void testCancelTransactionWithManagedScheduledExecutorService() {
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
