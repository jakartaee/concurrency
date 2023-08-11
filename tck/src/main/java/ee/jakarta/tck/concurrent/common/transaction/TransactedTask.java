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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.naming.InitialContext;

import ee.jakarta.tck.concurrent.framework.TestConstants;
import jakarta.transaction.UserTransaction;

public class TransactedTask implements WorkInterface {

    private static final long serialVersionUID = 1L;

    private final boolean beginTransaction;

    private final boolean isCommit;

    private final String sqlTemplate;

    public TransactedTask(final boolean commitOrRollback, final boolean beginTransaction, final String sqlTemplate) {
        this.sqlTemplate = sqlTemplate;
        this.beginTransaction = beginTransaction;
        this.isCommit = commitOrRollback;
    }

    public TransactedTask(final boolean commitOrRollback, final String sqlTemplate) {
        this.sqlTemplate = sqlTemplate;
        this.beginTransaction = true;
        this.isCommit = commitOrRollback;
    }

    @Override
    public void run() {
        int originCount = Counter.getCount();
        UserTransaction ut = null;

        try {
            if (beginTransaction) {
                ut = InitialContext.doLookup(TestConstants.userTransaction);
                ut.begin();
            }
            try (Connection conn = Connections.getConnection(false);
                    PreparedStatement pStmt = conn.prepareStatement(sqlTemplate);) {

                String sTypeDesc = "Type-99";
                int newType = 99;
                pStmt.setInt(1, newType);
                pStmt.setString(2, sTypeDesc);
                pStmt.executeUpdate();

                // commit or roll back transaction.
                if (beginTransaction && isCommit) {
                    ut.commit();
                }

                if (beginTransaction && !isCommit) {
                    ut.rollback();
                }

                // check status.
                int afterTransacted = Counter.getCount();

                if (isCommit) {
                    assertEquals(originCount + 1, afterTransacted);
                } else {
                    assertEquals(originCount, afterTransacted);
                }
            } catch (Exception e) {
                try {
                    ut.rollback();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doWork() {

        UserTransaction ut = null;

        try {
            if (beginTransaction) {
                ut = InitialContext.doLookup(TestConstants.userTransaction);
                ut.begin();
            }

            try (Connection conn = Connections.getConnection(false);
                    PreparedStatement pStmt = conn.prepareStatement(sqlTemplate);) {

                String sTypeDesc = "Type-98";
                int newType = 98;
                pStmt.setInt(1, newType);
                pStmt.setString(2, sTypeDesc);
                pStmt.executeUpdate();

                // commit or roll back transaction.
                if (beginTransaction && isCommit) {
                    ut.commit();
                }

                if (beginTransaction && !isCommit) {
                    ut.rollback();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
