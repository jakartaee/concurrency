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

public final class Constants {
    
    private Constants() {
        //utility class
    }

    public static final String CONTEXT_PATH = "/TransactionServlet";

    public static final String PARAM_COMMIT = "isCommit";

    public static final String COMMIT_TRUE = "isCommit=true";

    public static final String COMMIT_FALSE = "isCommit=false";

    public static final String COMMIT_CANCEL = "isCommit=cancel";

    public static final String TABLE_P = "concurrencetable";

    public static final String USERNAME = "user1";

    public static final String PASSWORD = "password1";

    public static final String SQL_TEMPLATE_DROP = "drop table concurrencetable";

    public static final String SQL_TEMPLATE_CREATE = "create table concurrencetable (TYPE_ID int NOT NULL, TYPE_DESC varchar(32), primary key(TYPE_ID))";

    public static final String SQL_TEMPLATE_INSERT = "insert into concurrencetable values(?, ?)";

    public static final String SQL_TEMPLATE_DELETE = "delete from concurrencetable";

}
