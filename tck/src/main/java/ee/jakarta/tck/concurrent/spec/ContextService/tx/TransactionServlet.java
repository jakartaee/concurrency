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

import ee.jakarta.tck.concurrent.common.transaction.Connections;
import ee.jakarta.tck.concurrent.common.transaction.Constants;
import ee.jakarta.tck.concurrent.common.transaction.Counter;
import ee.jakarta.tck.concurrent.common.transaction.TransactedTask;
import ee.jakarta.tck.concurrent.common.transaction.WorkInterface;
import ee.jakarta.tck.concurrent.framework.TestConstants;
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
@WebServlet(Constants.CONTEXT_PATH)
@DataSourceDefinition(
	name = "java:comp/env/jdbc/ContextServiceDB", 
	className = "org.apache.derby.jdbc.EmbeddedDataSource", 
	databaseName = "memory:ContextServiceDB", 
	properties = {
			"createDatabase=create" 
			}
)
public class TransactionServlet extends TestServlet {

	private static final TestLogger log = TestLogger.get(TransactionServlet.class);

	@Resource(lookup = "java:comp/env/jdbc/ContextServiceDB")
	private DataSource ds;

	@Resource(lookup = TestConstants.DefaultContextService)
	private ContextService cx;

	@Resource(lookup = TestConstants.UserTransaction)
	private UserTransaction ut;

	@Override
	protected void before() throws RemoteException {
		log.enter("before");
		
		Connections.setDataSource(ds);
		
		try (Connection conn = Connections.getConnection(true); Statement stmt = conn.createStatement()) {
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
	    
        int originCount = Counter.getCount();

        try {
            ut.begin();
    		try (Connection conn = Connections.getConnection(false); PreparedStatement pStmt = conn.prepareStatement(Constants.SQL_TEMPLATE_INSERT);) {
    			pStmt.setInt(1, 99);
    			pStmt.setString(2, "Type-99");
    			pStmt.addBatch();
    			pStmt.setInt(1, 100);
    			pStmt.setString(2, "Type-100");
    			pStmt.addBatch();
    			pStmt.executeBatch();
    
    			WorkInterface work = new TransactedTask(false, false, Constants.SQL_TEMPLATE_INSERT);
    
    			Map<String, String> m = new HashMap<>();
    			m.put(ManagedTask.TRANSACTION, ManagedTask.USE_TRANSACTION_OF_EXECUTION_THREAD);
    			
    			WorkInterface proxy = cx.createContextualProxy(work, m, WorkInterface.class);
    			proxy.doWork();
    			
    			ut.commit();
    			
    			int afterTransacted = Counter.getCount();
    
    			return String.valueOf(afterTransacted - originCount);
    		}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	public String testTransactionOfExecuteThreadAndRollback() throws ServletException {
		
		int originCount = Counter.getCount();
		
		try {
		    ut.begin();
		    try ( Connection conn = Connections.getConnection(false); PreparedStatement pStmt = conn.prepareStatement(Constants.SQL_TEMPLATE_INSERT);) {
		        pStmt.setInt(1, 99);
	            pStmt.setString(2, "Type-99");
	            pStmt.addBatch();
	            pStmt.setInt(1, 100);
	            pStmt.setString(2, "Type-100");
	            pStmt.addBatch();
	            pStmt.executeBatch();
	            
	            WorkInterface work = new TransactedTask(false, false, Constants.SQL_TEMPLATE_INSERT);

	            Map<String, String> m = new HashMap<>();
	            m.put(ManagedTask.TRANSACTION, ManagedTask.USE_TRANSACTION_OF_EXECUTION_THREAD);
	            
	            WorkInterface proxy = cx.createContextualProxy(work, m, WorkInterface.class);
	            proxy.doWork();
	            
	            ut.rollback();
	            
	            int afterTransacted = Counter.getCount();

	            return String.valueOf(afterTransacted - originCount);
		    }
        } catch (Exception e) {
            throw new ServletException(e);
        }
	}

	public String testSuspendAndCommit() throws ServletException {
		
		int originCount = Counter.getCount();
		
		try {
		    ut.begin();
		    try (Connection conn = Connections.getConnection(false); PreparedStatement pStmt = conn.prepareStatement(Constants.SQL_TEMPLATE_INSERT);) {
		        
		        pStmt.setInt(1, 99);
	            pStmt.setString(2, "Type-99");
	            pStmt.addBatch();
	            pStmt.setInt(1, 100);
	            pStmt.setString(2, "Type-100");
	            pStmt.addBatch();
	            pStmt.executeBatch();
	            
	            WorkInterface work = new TransactedTask(true, true, Constants.SQL_TEMPLATE_INSERT);
	            
	            Map<String, String> m = new HashMap<>();
	            m.put(ManagedTask.TRANSACTION, ManagedTask.SUSPEND);
	            
	            WorkInterface proxy = cx.createContextualProxy(work, m, WorkInterface.class);
	            proxy.doWork();
	            
	            ut.rollback();
	            
	            int afterTransacted = Counter.getCount();

	            return String.valueOf(afterTransacted - originCount);
		    }
        } catch (Exception e) {
            throw new ServletException(e);
        }
		
	}

	public String testSuspendAndRollback() throws ServletException {		
		
		int originCount = Counter.getCount();
		
		try {
		    ut.begin();
		    
		    try (Connection conn = Connections.getConnection(false); PreparedStatement pStmt = conn.prepareStatement(Constants.SQL_TEMPLATE_INSERT);) {
		            pStmt.setInt(1, 99);
		            pStmt.setString(2, "Type-99");
		            pStmt.addBatch();
		            pStmt.setInt(1, 100);
		            pStmt.setString(2, "Type-100");
		            pStmt.addBatch();
		            pStmt.executeBatch();

		            WorkInterface work = new TransactedTask(false, true, Constants.SQL_TEMPLATE_INSERT);

		            Map<String, String> m = new HashMap<>();
		            m.put(ManagedTask.TRANSACTION, ManagedTask.SUSPEND);
		            
		            WorkInterface proxy = cx.createContextualProxy(work, m, WorkInterface.class);
		            proxy.doWork();
		            
		            ut.commit();
		            
		            int afterTransacted = Counter.getCount();

		            return String.valueOf(afterTransacted - originCount);
		    }
        } catch (Exception e) {
            throw new ServletException(e);
        }
		
	}

	public String testDefaultAndCommit() throws ServletException {
	    
	       int originCount = Counter.getCount();
	        
	        try {
	            ut.begin();
	            
	            try (Connection conn = Connections.getConnection(false); PreparedStatement pStmt = conn.prepareStatement(Constants.SQL_TEMPLATE_INSERT);) {
	                
	                pStmt.setInt(1, 99);
	                pStmt.setString(2, "Type-99");
	                pStmt.addBatch();
	                pStmt.setInt(1, 100);
	                pStmt.setString(2, "Type-100");
	                pStmt.addBatch();
	                pStmt.executeBatch();
	                
	                WorkInterface work = new TransactedTask(true, true, Constants.SQL_TEMPLATE_INSERT);
	                
	                WorkInterface proxy = cx.createContextualProxy(work, WorkInterface.class);
	                proxy.doWork();
	                
	                ut.rollback();
	                
	                int afterTransacted = Counter.getCount();

	                return String.valueOf(afterTransacted - originCount);
	            }
	        } catch (Exception e) {
	            throw new ServletException(e);
	        }
	   
	}
}
