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

package ee.jakarta.tck.concurrent.spec.ManagedThreadFactory.tx;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.TestLogger;
import jakarta.transaction.UserTransaction;

public class TransactedTask implements Runnable {
	
	private static final TestLogger log = TestLogger.get(TransactedTask.class);
	
	private final boolean isCommit;

	private final String username, password, sqlTemplate;

	public TransactedTask(boolean commitOrRollback, String username, String password, String sqlTemplate) {
		this.username = username;
		this.password = password;
		this.sqlTemplate = sqlTemplate;
		isCommit = commitOrRollback;
	}

	@Override
	public void run() {
		boolean pass = false;
		String tableName = Constants.TABLE_P;
		int originCount = Util.getCount(tableName, username, password);

		UserTransaction ut;
        try {
            ut = InitialContext.doLookup(TestConstants.UserTransaction);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
		assertNotNull(ut, "didn't get user transaction inside the submitted task.");

		Connection conn = Util.getConnection(false, username, password);
		PreparedStatement pStmt = null;
		try {
			ut.begin();
			pStmt = conn.prepareStatement(sqlTemplate);
			String sTypeDesc = "Type-99";
			int newType = 99;
			pStmt.setInt(1, newType);
			pStmt.setString(2, sTypeDesc);
			pStmt.executeUpdate();
			// commit or roll back transaction.
			if (isCommit) {
				ut.commit();
			} else {
				ut.rollback();
			}
			// check status.
			int afterTransacted = Util.getCount(tableName, username, password);
			if (isCommit) {
				pass = (afterTransacted == originCount + 1);
			} else {
				pass = (afterTransacted == originCount);
			}
		} catch (Exception e) {
			try {
				ut.rollback();
			} catch (Exception e1) {
				log.finer("Got exception when trying to do rollback on failed test", e1);
			}
			fail("Got exception when trying to run TransactedTask", e);
		} finally {
			try {
				pStmt.close();
				conn.close();
			} catch (Exception e) {
				log.finer("Got exception when trying to close connection and statment", e);
			}
		}
		
		assertTrue(pass, "didn't get expected result with transacted task.");
	}

}
