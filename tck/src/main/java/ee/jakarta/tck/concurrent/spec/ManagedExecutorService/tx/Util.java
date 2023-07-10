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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import ee.jakarta.tck.concurrent.framework.TestLogger;
import ee.jakarta.tck.concurrent.framework.TestUtil;

public class Util {

	private static final TestLogger log = TestLogger.get(Util.class);

	private Util() {
	}

	public static Connection getConnection(DataSource ds, String user, String pwd, boolean autoCommit) {
		Connection conn = null;
		try {
			conn = ds.getConnection(); // Try without user password for EE case
			if (conn == null) {
				conn = ds.getConnection(user, pwd); // For standalone cases
			}
			if (null != conn) {
				conn.setAutoCommit(autoCommit);
			}
		} catch (SQLException e) {
			log.severe("failed to get connection.", e);
		}
		return conn;
	}

	public static int getCount(String tableName, String username, String password) {
		Connection conn = getConnection(true, username, password);
		Statement stmt = null;
		try {
			final String queryStr = "select count(*) from " + tableName;
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(queryStr);
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}

	/**
	 * get count by specifying connection. the caller should take care of closing
	 * connection.
	 * 
	 * @param conn
	 * @param tableName
	 * @param username
	 * @param password
	 * @return
	 */
	public static int getCount(Connection conn, String tableName, String username, String password) {
		Statement stmt = null;
		try {
			final String queryStr = "select count(*) from " + tableName;
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(queryStr);
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}

	public static Connection getConnection(boolean autoCommit, String username, String password) {
		DataSource ds;
        try {
            ds = InitialContext.doLookup(Constants.DS_JNDI_NAME);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
		Connection conn = Util.getConnection(ds, username, password, autoCommit);
		return conn;
	}

	public static void waitForTransactionBegan(CancelledTransactedTask pp) {
		final long stopTime = System.currentTimeMillis() + Constants.POLL_TIMEOUT.toMillis();
		while (!pp.transactionBegin() && System.currentTimeMillis() < stopTime) {
			try {
				TestUtil.sleep(Constants.POLL_INTERVAL);
			} catch (InterruptedException ignore) {
			}
		}
	}
}
