/*
 * Copyright (c) 2013, 2024 Oracle and/or its affiliates. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Future;

import javax.sql.DataSource;

import ee.jakarta.tck.concurrent.common.transaction.CancelledTransactedTask;
import ee.jakarta.tck.concurrent.common.transaction.Connections;
import ee.jakarta.tck.concurrent.common.transaction.Constants;
import ee.jakarta.tck.concurrent.common.transaction.Counter;
import ee.jakarta.tck.concurrent.common.transaction.TransactedTask;
import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.TestLogger;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import ee.jakarta.tck.concurrent.framework.junit.extensions.Wait;
import jakarta.annotation.Resource;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings({ "serial" })
@WebServlet(Constants.CONTEXT_PATH)
@DataSourceDefinition(
        name = "java:comp/env/jdbc/ManagedExecutorServiceDB",
        className = "org.apache.derby.jdbc.EmbeddedDataSource",
        databaseName = "memory:ManagedExecutorServiceDB",
        properties = {
                "createDatabase=create"
        })
public class TransactionServlet extends TestServlet {

    private static final TestLogger log = TestLogger.get(TransactionServlet.class);

    @Resource(lookup = "java:comp/env/jdbc/ManagedExecutorServiceDB")
    private DataSource ds;

    @Resource(lookup = TestConstants.defaultManagedExecutorService)
    private ManagedExecutorService executor;

    @Override
    protected void beforeClass() throws RemoteException {
        log.enter("beforeClass");

        Connections.setDataSource(ds);

        try (Connection conn = Connections.getConnection(true); Statement stmt = conn.createStatement()) {
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

    public void transactionTest(final HttpServletRequest req, final HttpServletResponse res) throws Exception {
        boolean isCommit = Boolean.parseBoolean(req.getParameter(Constants.PARAM_COMMIT));

        Future<?> taskResult = executor.submit(new TransactedTask(isCommit, Constants.SQL_TEMPLATE_INSERT));

        Wait.waitForTaskComplete(taskResult);
    }

    public void cancelTest() {
        int originTableCount = Counter.getCount();

        CancelledTransactedTask cancelledTask = new CancelledTransactedTask(Constants.SQL_TEMPLATE_INSERT);
        Future<?> future = executor.submit(cancelledTask);

        // wait for transaction to begin
        Wait.waitForTransactionBegan(cancelledTask);

        // set flag to rollback transaction
        cancelledTask.getCancelTransaction().set(true);

        // continue query
        cancelledTask.getRunQuery().set(true);
        
        // wait for transaction to finish
        Wait.waitForTaskComplete(future);

        // verify transaction rolled back
        int afterTransacted = Counter.getCount();
        assertEquals(originTableCount, afterTransacted, "task was not properly cancelled");
    }
}
