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

package jakarta.enterprise.concurrent.spec.ContextService.tx;

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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jakarta.enterprise.concurrent.tck.framework.TestLogger;
import jakarta.enterprise.concurrent.util.TestClient;
import jakarta.enterprise.concurrent.util.TestUtil;

import jakarta.annotation.Resource;

public class TransactionTests extends TestClient {

	private static final TestLogger log = TestLogger.get(TransactionTests.class);

	private String appendedURL = "";

	private String username, password, tablename, testURL;

	@Resource(lookup = Constants.DS_JNDI_NAME)
	private static DataSource ds;

	private Connection conn;

	/*
	 * @class.setup_props: webServerHost; webServerPort; ts_home; Driver, the Driver
	 * name; db1, the database name with url; user1, the database user name;
	 * password1, the database password; db2, the database name with url; user2, the
	 * database user name; password2, the database password; DriverManager, flag for
	 * DriverManager; ptable, the primary table; ftable, the foreign table; cofSize,
	 * the initial size of the ptable; cofTypeSize, the initial size of the ftable;
	 * binarySize, size of binary data type; varbinarySize, size of varbinary data
	 * type; longvarbinarySize, size of longvarbinary data type;
	 *
	 * @class.testArgs: -ap tssql.stmt
	 */
	@BeforeClass // TODO BeforeClass or BeforeTest
	public void setup() {
		loadServerProperties();
		setURLContext("/concurrency_spec_ContextService_tx_web");

		try {
			tablename = props.getProperty(Constants.TABLE_P);
			appendedURL = appendedURL(props);
			username = props.getProperty(Constants.USERNAME);
			password = props.getProperty(Constants.PASSWORD);
			testURL = "http://" + host + ":" + port + getURLContext() + "/TxServlet" + appendedURL;
			removeTestData();
		} catch (Exception e) {
			setupFailure(e);
		}
	}

	@AfterClass // TODO AfterClass or AfterTest
	public void cleanup() {
		try {
			removeTestData();
		} catch (Exception e) {
			cleanupFailure(e);
		}
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
		URL url;
		String resp = null;
		try {
			url = new URL(testURL + "&methodname=TransactionOfExecuteThreadAndCommitTest");
			resp = TestUtil.getResponse(url.openConnection());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("The result is : " + resp);
		assertNotNull("Response should not be null.", resp);
		assertEquals(testName + " failed to get successful result.", "3", // expected
				resp.trim()); // actual
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
		URL url;
		String resp = null;
		try {
			url = new URL(testURL + "&methodname=TransactionOfExecuteThreadAndRollbackTest");
			resp = TestUtil.getResponse(url.openConnection());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("The result is : " + resp);
		assertNotNull("Response should not be null.", resp);
		assertEquals(testName + " failed to get successful result.", "0", // expected
				resp.trim()); // actual
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
		URL url;
		String resp = null;
		try {
			url = new URL(testURL + "&methodname=SuspendAndCommitTest");
			resp = TestUtil.getResponse(url.openConnection());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("The result is : " + resp);
		assertNotNull("Response should not be null.", resp);
		assertEquals(testName + " failed to get successful result.", "1", // expected
				resp.trim()); // actual
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
		URL url;
		String resp = null;
		try {
			url = new URL(testURL + "&methodname=SuspendAndRollbackTest");
			resp = TestUtil.getResponse(url.openConnection());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("The result is : " + resp);
		assertNotNull("Response should not be null.", resp);
		assertEquals(testName + " failed to get successful result.", "2", // expected
				resp.trim()); // actual
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
		URL url;
		String resp = null;
		try {
			url = new URL(testURL + "&methodname=DefaultAndCommitTest");
			resp = TestUtil.getResponse(url.openConnection());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("The result is : " + resp);
		assertNotNull("Response should not be null.", resp);
		assertEquals(testName + " failed to get successful result.", "1", // expected
				resp.trim()); // actual
	}

	private String appendedURL(Properties p) throws UnsupportedEncodingException {
		StringBuffer sb = new StringBuffer("?");
		sb.append(Constants.USERNAME + "=" + p.get(Constants.USERNAME));
		sb.append("&");
		sb.append(Constants.PASSWORD + "=" + p.get(Constants.PASSWORD));
		sb.append("&");
		sb.append(Constants.TABLE_P + "=" + Constants.TABLE_P);
		sb.append("&");
		sb.append(Constants.SQL_TEMPLATE + "=" + URLEncoder.encode(p.get(Constants.SQL_TEMPLATE).toString(), "utf8"));
		return sb.toString();
	}

	private void removeTestData() throws RemoteException {
		log.info("removeTestData");

		// init connection.
		conn = Util.getConnection(ds, username, password, true);
		String removeString = props.getProperty("Dbschema_Concur_Delete", "");
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(removeString);
			stmt.close();
		} catch (Exception e) {
			throw new RemoteException(e.getMessage());
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}
}
