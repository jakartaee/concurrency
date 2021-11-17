/*
 * Copyright (c) 2013, 2021, 2020 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import jakarta.annotation.Resource;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ManagedTask;
import jakarta.enterprise.concurrent.tck.framework.TestLogger;
import jakarta.enterprise.concurrent.tck.framework.TestServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.transaction.UserTransaction;

@SuppressWarnings("serial")
@WebServlet("/TransactionServlet")
@DataSourceDefinition(name = "java:comp/env/jdbc/DB1", className = "org.apache.derby.jdbc.EmbeddedDataSource", databaseName = "memory:DB1", user = "user1", password = "password1", properties = {
		"createDatabase=create" })
public class TransactionServlet extends TestServlet {

	private static final TestLogger log = TestLogger.get(TransactionServlet.class);

	@Resource(lookup = "java:comp/env/jdbc/DB1")
	DataSource ds;

	@Resource(lookup = "java:comp/DefaultContextService")
	private ContextService cx;

	@Resource(lookup = "java:comp/UserTransaction")
	private UserTransaction ut;

	@Override
	protected void before() throws Exception {
		removeTestData();
	}

	@Override
	protected void after() throws Exception {
		removeTestData();
	}

	private void removeTestData() throws RemoteException {
		log.info("removeTestData");

		// init connection.
		Connection conn = Util.getConnection(ds, Constants.USERNAME, Constants.PASSWORD, true);
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(Constants.SQL_TEMPLATE_DELETE);
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

	public String TransactionOfExecuteThreadAndCommitTest() throws ServletException {
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

			TestWorkInterface work = new TestTxWork();
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

	public String TransactionOfExecuteThreadAndRollbackTest() throws ServletException {
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

			TestWorkInterface work = new TestTxWork();
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

	public String SuspendAndCommitTest() throws ServletException {
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
			TestWorkInterface work = new TestTxWork();
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

	public String SuspendAndRollbackTest() throws ServletException {
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
			TestWorkInterface work = new TestTxWork();
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

	public String DefaultAndCommitTest() throws ServletException {
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
			TestWorkInterface work = new TestTxWork();
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

	public static Object invoke(Object o, String methodName, Class[] paramTypes, Object[] args)
			throws ServletException {

		try {
			if (o == null || methodName == null || "".equals(methodName.trim())) {
				throw new IllegalArgumentException("Object and methodName must not be null");
			}
			Method method = null;
			if (paramTypes != null && paramTypes.length > 0) {
				method = o.getClass().getMethod(methodName, paramTypes);
			} else {
				method = o.getClass().getMethod(methodName);
			}

			Object result = null;
			if (method != null) {
				if (args != null && args.length > 0) {
					result = method.invoke(o, args);
				} else {
					result = method.invoke(o);
				}
			}

			return result;

		} catch (NoSuchMethodException e) {
			throw new ServletException(e);
		} catch (InvocationTargetException e) {
			throw new ServletException(e);
		} catch (IllegalArgumentException e) {
			throw new ServletException(e);
		} catch (IllegalAccessException e) {
			throw new ServletException(e);
		}
	}
}
