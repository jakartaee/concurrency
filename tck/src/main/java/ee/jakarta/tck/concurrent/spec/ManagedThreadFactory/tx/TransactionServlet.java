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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.TestLogger;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import ee.jakarta.tck.concurrent.framework.junit.extensions.Wait;
import jakarta.annotation.Resource;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
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
	
    @Resource(lookup = TestConstants.DefaultManagedThreadFactory)
    public ManagedThreadFactory threadFactory;

	@Override
	protected void beforeClass() throws RemoteException {
		log.enter("beforeClass");
		
		try (Connection conn = Util.getConnection(ds, Constants.USERNAME, Constants.PASSWORD, true); Statement stmt = conn.createStatement()) {
			try {
				stmt.executeUpdate(Constants.SQL_TEMPLATE_DROP);
			} catch (SQLException e) {
				log.finest("Could not drop table, assume table did not exist.");
			}
			stmt.executeUpdate(Constants.SQL_TEMPLATE_CREATE);
			log.exit("beforeClass");
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void transactionTest(HttpServletRequest req, HttpServletResponse res) throws Exception {
		boolean isCommit = Boolean.parseBoolean(req.getParameter(Constants.PARAM_COMMIT));
		Thread thread = threadFactory.newThread(new TransactedTask(isCommit, Constants.USERNAME, Constants.PASSWORD,
				Constants.SQL_TEMPLATE_INSERT));
		thread.start();
		Wait.waitTillThreadFinish(thread);
	}

	public void cancelTest() {
		int originTableCount = Util.getCount(Constants.TABLE_P, Constants.USERNAME, Constants.PASSWORD);
		CancelledTransactedTask cancelledTask = new CancelledTransactedTask(Constants.USERNAME, Constants.PASSWORD,
				Constants.SQL_TEMPLATE_INSERT);
		Thread thread = threadFactory.newThread(cancelledTask);
		thread.start();
		// then cancel it after transaction begin and
		Util.waitForTransactionBegan(cancelledTask);
		// before it commit.
		cancelledTask.cancelTask();
		// continue to run if possible.
		cancelledTask.resume();
		int afterTransacted = Util.getCount(Constants.TABLE_P, Constants.USERNAME, Constants.PASSWORD);
		assertEquals(originTableCount, afterTransacted, "task was not properly cancelled");
	}
}
