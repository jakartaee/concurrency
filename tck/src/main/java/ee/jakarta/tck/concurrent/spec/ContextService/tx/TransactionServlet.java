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

package ee.jakarta.tck.concurrent.spec.ContextService.tx;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import ee.jakarta.tck.concurrent.framework.TestLogger;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import jakarta.annotation.Resource;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ManagedTask;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.transaction.UserTransaction;

@SuppressWarnings({"serial", "unused"})
@WebServlet("/TransactionServlet")
@DataSourceDefinition(
	name = Constants.DS_JNDI_NAME, 
	className = "org.apache.derby.jdbc.EmbeddedDataSource", 
	databaseName = Constants.DS_DB_NAME, 
	properties = {
			"createDatabase=create" 
			}
)
public class TransactionServlet extends TestServlet {

	private static final TestLogger log = TestLogger.get(TransactionServlet.class);

	@Resource(lookup = Constants.DS_JNDI_NAME)
	private DataSource ds;

	@Resource(lookup = Constants.CONTEXT_SVC_JNDI_NAME)
	private ContextService cx;

	@Resource(lookup = Constants.UT_JNDI_NAME)
	private UserTransaction ut;

	@Override
	protected void before() throws RemoteException {
		log.enter("before");
		
		try (Connection conn = Util.getConnection(ds, Constants.USERNAME, Constants.PASSWORD, true); Statement stmt = conn.createStatement()) {
			try {
				stmt.executeUpdate(Constants.SQL_TEMPLATE_DROP);
			} catch (SQLException e) {
				log.finest("Could not drop table, assume table did not exist.");
			}
			stmt.executeUpdate(Constants.SQL_TEMPLATE_CREATE);
			log.exit("before");
		} catch (Exception e) {
			throw new RemoteException(e.getMessage());
		}
	}

	public String testTransactionOfExecuteThreadAndCommit() throws ServletException {
		PreparedStatement pStmt = null;
		Connection conn = null;
		Connection conn2 = null;

		try {
			int originCount = Util.getCount(Constants.TABLE_P, Constants.USERNAME, Constants.PASSWORD);
			ut.begin();
			conn = Util.getConnection(false, Constants.USERNAME, Constants.PASSWORD);
			pStmt = conn.prepareStatement(Constants.SQL_TEMPLATE_INSERT);
			pStmt.setInt(1, 99);
			pStmt.setString(2, "Type-99");
			pStmt.addBatch();
			pStmt.setInt(1, 100);
			pStmt.setString(2, "Type-100");
			pStmt.addBatch();
			pStmt.executeBatch();

			TestWorkInterface work = new TestTransactionWork();
			work.setUserName(Constants.USERNAME);
			work.setPassword(Constants.PASSWORD);
			work.setSQLTemplate(Constants.SQL_TEMPLATE_INSERT);
			Map<String, String> m = new HashMap();
			m.put(ManagedTask.TRANSACTION, ManagedTask.USE_TRANSACTION_OF_EXECUTION_THREAD);
			TestWorkInterface proxy = cx.createContextualProxy(work, m, TestWorkInterface.class);
			proxy.doSomeWork();
			ut.commit();
			int afterTransacted = Util.getCount(Constants.TABLE_P, Constants.USERNAME, Constants.PASSWORD);

			return String.valueOf(afterTransacted - originCount);
		} catch (Exception e) {
			throw new ServletException(e);
		} finally {
			try {
				if (pStmt != null)
					pStmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String testTransactionOfExecuteThreadAndRollback() throws ServletException {
		PreparedStatement pStmt = null;
		Connection conn = null;
		Connection conn2 = null;

		try {
			int originCount = Util.getCount(Constants.TABLE_P, Constants.USERNAME, Constants.PASSWORD);
			ut.begin();
			conn = Util.getConnection(false, Constants.USERNAME, Constants.PASSWORD);
			pStmt = conn.prepareStatement(Constants.SQL_TEMPLATE_INSERT);
			pStmt.setInt(1, 99);
			pStmt.setString(2, "Type-99");
			pStmt.addBatch();
			pStmt.setInt(1, 100);
			pStmt.setString(2, "Type-100");
			pStmt.addBatch();
			pStmt.executeBatch();

			TestWorkInterface work = new TestTransactionWork();
			work.setUserName(Constants.USERNAME);
			work.setPassword(Constants.PASSWORD);
			work.setSQLTemplate(Constants.SQL_TEMPLATE_INSERT);
			Map<String, String> m = new HashMap();
			m.put(ManagedTask.TRANSACTION, ManagedTask.USE_TRANSACTION_OF_EXECUTION_THREAD);
			TestWorkInterface proxy = cx.createContextualProxy(work, m, TestWorkInterface.class);
			proxy.doSomeWork();
			ut.rollback();
			int afterTransacted = Util.getCount(Constants.TABLE_P, Constants.USERNAME, Constants.PASSWORD);

			return String.valueOf(afterTransacted - originCount);
		} catch (Exception e) {
			throw new ServletException(e);
		} finally {
			try {
				if (pStmt != null)
					pStmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String testSuspendAndCommit() throws ServletException {
		PreparedStatement pStmt = null;
		Connection conn = null;
		Connection conn2 = null;

		try {
			int originCount = Util.getCount(Constants.TABLE_P, Constants.USERNAME, Constants.PASSWORD);
			ut.begin();
			conn = Util.getConnection(false, Constants.USERNAME, Constants.PASSWORD);
			pStmt = conn.prepareStatement(Constants.SQL_TEMPLATE_INSERT);
			pStmt.setInt(1, 99);
			pStmt.setString(2, "Type-99");
			pStmt.addBatch();
			pStmt.setInt(1, 100);
			pStmt.setString(2, "Type-100");
			pStmt.addBatch();
			pStmt.executeBatch();
			TestWorkInterface work = new TestTransactionWork();
			work.setUserName(Constants.USERNAME);
			work.setPassword(Constants.PASSWORD);
			work.setSQLTemplate(Constants.SQL_TEMPLATE_INSERT);
			work.needBeginTx(true);
			work.needCommit(true);
			Map<String, String> m = new HashMap();
			m.put(ManagedTask.TRANSACTION, ManagedTask.SUSPEND);
			TestWorkInterface proxy = cx.createContextualProxy(work, m, TestWorkInterface.class);
			proxy.doSomeWork();
			ut.rollback();
			int afterTransacted = Util.getCount(Constants.TABLE_P, Constants.USERNAME, Constants.PASSWORD);

			return String.valueOf(afterTransacted - originCount);
		} catch (Exception e) {
			throw new ServletException(e);
		} finally {
			try {
				if (pStmt != null)
					pStmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String testSuspendAndRollback() throws ServletException {
		PreparedStatement pStmt = null;
		Connection conn = null;
		Connection conn2 = null;

		try {
			int originCount = Util.getCount(Constants.TABLE_P, Constants.USERNAME, Constants.PASSWORD);
			ut.begin();
			conn = Util.getConnection(false, Constants.USERNAME, Constants.PASSWORD);
			pStmt = conn.prepareStatement(Constants.SQL_TEMPLATE_INSERT);
			pStmt.setInt(1, 99);
			pStmt.setString(2, "Type-99");
			pStmt.addBatch();
			pStmt.setInt(1, 100);
			pStmt.setString(2, "Type-100");
			pStmt.addBatch();
			pStmt.executeBatch();
			TestWorkInterface work = new TestTransactionWork();
			work.setUserName(Constants.USERNAME);
			work.setPassword(Constants.PASSWORD);
			work.setSQLTemplate(Constants.SQL_TEMPLATE_INSERT);
			work.needBeginTx(true);
			work.needRollback(true);
			Map<String, String> m = new HashMap();
			m.put(ManagedTask.TRANSACTION, ManagedTask.SUSPEND);
			TestWorkInterface proxy = cx.createContextualProxy(work, m, TestWorkInterface.class);
			proxy.doSomeWork();
			ut.commit();
			int afterTransacted = Util.getCount(Constants.TABLE_P, Constants.USERNAME, Constants.PASSWORD);

			return String.valueOf(afterTransacted - originCount);
		} catch (Exception e) {
			throw new ServletException(e);
		} finally {
			try {
				if (pStmt != null)
					pStmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String testDefaultAndCommit() throws ServletException {
		PreparedStatement pStmt = null;
		Connection conn = null;
		Connection conn2 = null;

		try {
			int originCount = Util.getCount(Constants.TABLE_P, Constants.USERNAME, Constants.PASSWORD);
			ut.begin();
			conn = Util.getConnection(false, Constants.USERNAME, Constants.PASSWORD);
			pStmt = conn.prepareStatement(Constants.SQL_TEMPLATE_INSERT);
			pStmt.setInt(1, 99);
			pStmt.setString(2, "Type-99");
			pStmt.addBatch();
			pStmt.setInt(1, 100);
			pStmt.setString(2, "Type-100");
			pStmt.addBatch();
			pStmt.executeBatch();
			TestWorkInterface work = new TestTransactionWork();
			work.setUserName(Constants.USERNAME);
			work.setPassword(Constants.PASSWORD);
			work.setSQLTemplate(Constants.SQL_TEMPLATE_INSERT);
			work.needBeginTx(true);
			work.needCommit(true);
			TestWorkInterface proxy = cx.createContextualProxy(work, TestWorkInterface.class);
			proxy.doSomeWork();
			ut.rollback();
			// int afterTransacted = Util.getCount(Constants.TABLE_P, conn);
			int afterTransacted = Util.getCount(Constants.TABLE_P, Constants.USERNAME, Constants.PASSWORD);

			return String.valueOf(afterTransacted - originCount);
		} catch (Exception e) {
			throw new ServletException(e);
		} finally {
			try {
				if (pStmt != null)
					pStmt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
