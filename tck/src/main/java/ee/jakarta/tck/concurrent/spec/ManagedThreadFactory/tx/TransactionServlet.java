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
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import jakarta.annotation.Resource;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.enterprise.concurrent.tck.framework.TestLogger;
import jakarta.enterprise.concurrent.tck.framework.TestServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("/TransactionServlet")
@DataSourceDefinition(name = "java:comp/env/jdbc/DB1", className = "org.apache.derby.jdbc.EmbeddedDataSource", databaseName = "memory:DB1", user = "user1", password = "password1", properties = {
		"createDatabase=create" })
public class TransactionServlet extends TestServlet {

	private static final TestLogger log = TestLogger.get(TransactionServlet.class);

	@Resource(lookup = "java:comp/env/jdbc/DB1")
	private static DataSource ds;

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
		// FIXME String removeString = props.getProperty("Dbschema_Concur_Delete", "");
		String removeString = "FIXME";
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

	/**
	 * A basic implementation of the <code>doGet</code> method.
	 * 
	 * @param req - <code>HttpServletRequest</code>
	 * @param res - <code>HttpServletResponse</code>
	 * @exception ServletException if an error occurs
	 * @exception IOException      if an IO error occurs
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		invokeTest(req, res);
	}

	/**
	 * A basic implementation of the <code>doPost</code> method.
	 * 
	 * @param req - <code>HttpServletRequest</code>
	 * @param res - <code>HttpServletResponse</code>
	 * @exception ServletException if an error occurs
	 * @exception IOException      if an IO error occurs
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		invokeTest(req, res);
	}

	private void invokeTest(HttpServletRequest req, HttpServletResponse res) {
		boolean passed = false;
		String param = req.getParameter(Constants.PARAM_COMMIT);
		if (Constants.PARAM_VALUE_CANCEL.equals(param)) {
			passed = cancelTest(res);
		} else {
			boolean isCommit = Boolean.parseBoolean(param);
			ManagedThreadFactory factory = Util.getManagedThreadFactory();
			Thread thread = factory.newThread(new TransactedTask(isCommit, Constants.USERNAME, Constants.PASSWORD,
					Constants.SQL_TEMPLATE_INSERT));
			thread.start();
			try {
				Util.waitTillThreadFinish(thread);
			} catch (Exception e) {
				e.printStackTrace();
				print(res, Message.FAILMESSAGE);
				return;
			}
			passed = true;
		}
		if (passed) {
			print(res, Message.SUCCESSMESSAGE);
		} else {
			print(res, Message.FAILMESSAGE);
		}
	}

	private boolean cancelTest(HttpServletResponse res) {
		int originTableCount = Util.getCount(Constants.TABLE_P, Constants.USERNAME, Constants.PASSWORD);
		CancelledTransactedTask cancelledTask = new CancelledTransactedTask(Constants.USERNAME, Constants.PASSWORD,
				Constants.SQL_TEMPLATE_INSERT);
		ManagedThreadFactory factory = Util.getManagedThreadFactory();
		Thread thread = factory.newThread(cancelledTask);
		thread.start();
		// then cancel it after transaction begin and
		Util.waitForTransactionBegan(cancelledTask, 3000, 100);
		// before it commit.
		cancelledTask.cancelTask();
		// continue to run if possible.
		cancelledTask.resume();
		int afterTransacted = Util.getCount(Constants.TABLE_P, Constants.USERNAME, Constants.PASSWORD);
		return originTableCount == afterTransacted;
	}

	private void print(HttpServletResponse res, String msg) {
		PrintWriter pw = null;
		try {
			pw = res.getWriter();
			pw.print(msg);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			pw.close();
		}
	}
}
