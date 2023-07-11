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

package ee.jakarta.tck.concurrent.common.transaction;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.naming.InitialContext;

import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.junit.extensions.Wait;
import jakarta.transaction.UserTransaction;

public class CancelledTransactedTask implements Runnable {

    public AtomicBoolean runQuery = new AtomicBoolean(false);

    public AtomicBoolean beginTransaction = new AtomicBoolean(false);

    public AtomicBoolean cancelTransaction = new AtomicBoolean(false);

    private final String sqlTemplate;

    public CancelledTransactedTask(String sqlTemplate) {
        this.sqlTemplate = sqlTemplate;
    }

    private void waitForRun() {
        assertTimeoutPreemptively(TestConstants.WaitTimeout, () -> {
            for (; !runQuery.get(); Wait.sleep(TestConstants.PollInterval))
                ;
        });
    }

    @Override
    public void run() {
        try {
            UserTransaction ut = InitialContext.doLookup(TestConstants.UserTransaction);
            ut.begin();
            beginTransaction.set(true);
            waitForRun();
            try (Connection conn = Connections.getConnection(false);
                    PreparedStatement pStmt = conn.prepareStatement(sqlTemplate);) {
                String sTypeDesc = "Type-Cancelled-99";
                int newType = 991;
                pStmt.setInt(1, newType);
                pStmt.setString(2, sTypeDesc);
                pStmt.executeUpdate();

                // check if it is cancelled here
                if (cancelTransaction.get()) {
                    ut.rollback();
                    return;
                }

                ut.commit();
            } catch (Exception e) {
                try {
                    ut.rollback();
                } catch (Exception sqle) {
                    sqle.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
