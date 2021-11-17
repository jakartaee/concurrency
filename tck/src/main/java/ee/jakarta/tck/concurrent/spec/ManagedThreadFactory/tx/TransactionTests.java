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

package jakarta.enterprise.concurrent.spec.ManagedThreadFactory.tx;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import jakarta.enterprise.concurrent.tck.framework.TestLogger;
import jakarta.enterprise.concurrent.tck.framework.TestClient;
import jakarta.enterprise.concurrent.tck.framework.TestUtil;
import jakarta.enterprise.concurrent.tck.framework.URLBuilder;
import jakarta.annotation.Resource;

public class TransactionTests extends TestClient {

	@ArquillianResource
	URL baseURL;

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
	@Test
	public void testCommitTransactionWithManagedThreadFactory() {
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
		assertEquals(testName + " failed to get successful result.", Message.SUCCESSMESSAGE, resp);
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
	public void testRollbackTransactionWithManagedThreadFactory() {
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
		assertEquals(testName + " failed to get successful result.", Message.SUCCESSMESSAGE, resp);
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
	public void testCancelTransactionWithManagedThreadFactory() {
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
		assertEquals(testName + " failed to get successful result.", Message.SUCCESSMESSAGE, resp);
	}
}
