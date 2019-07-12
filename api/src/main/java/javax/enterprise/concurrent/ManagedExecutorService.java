/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package javax.enterprise.concurrent;

import java.util.concurrent.ExecutorService;

/**
 * A manageable version of a {@link java.util.concurrent.ExecutorService}.
 * <p>
 * A ManagedExecutorService extends the Java&trade; SE ExecutorService to provide
 * methods for submitting tasks for execution in a Jakarta&trade; EE environment. 
 * Implementations of the ManagedExecutorService are
 * provided by a Jakarta EE Product Provider.  Application Component Providers
 * use the Java Naming and Directory Interface&trade; (JNDI) to look-up instances of one
 * or more ManagedExecutorService objects using resource environment references.
 * ManagedExecutorService instances can also be injected into application
 * components through the use of the {@code Resource} annotation.
 * <p>
 * The Jakarta Concurrency specification describes several
 * behaviors that a ManagedExecutorService can implement.  The Application
 * Component Provider and Deployer identify these requirements and map the
 * resource environment reference appropriately.
 * <p>
 * The most common uses for a ManagedExecutorService is to run short-duration asynchronous
 * tasks such as for processing of asynchronous methods in Jakarta
 * Enterprise Beans or for processing async tasks for Servlets that
 * supports asynchronous processing.
 * <p>
 * Tasks are run in managed threads provided by the Jakarta EE Product Provider
 * and are run within the application component context that submitted the task. 
 * All tasks run without an explicit transaction (they do not enlist in the application
 * component's transaction).  If a transaction is required, use a
 * {@link javax.transaction.UserTransaction} instance.  A UserTransaction instance is
 * available in JNDI using the name: &quot;java:comp/UserTransaction&quot; or by
 * requesting an injection of a {@link javax.transaction.UserTransaction} object
 * using the {@code Resource} annotation.
 * <p>
 * Example:
 * <pre>
 * public run() {
 *   // Begin of task
 *   InitialContext ctx = new InitialContext();
 *   UserTransaction ut = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
 *   ut.begin();
 *
 *   // Perform transactional business logic
 *
 *   ut.commit();
 * }
 * </pre>
 *
 * Tasks can optionally provide an {@link ManagedTaskListener} to receive 
 * notifications of lifecycle events, through the use of {@link ManagedTask}
 * interface.
 * <p>
 * Example:
 * <pre>
 * public class MyRunnable implements Runnable, ManagedTask {
 *   ...
 *   public void run() {
 *     ...
 *   }
 * 
 *   public ManagedTaskListener getManagedTaskListener() {
 *     return myManagedTaskListener;
 *   }
 *   ...
 * }
 * 
 * MyRunnable task = ...;
 * ManagedExecutorService executor = ...;
 * 
 * executor.submit(task); // lifecycle events will be notified to myManagedTaskListener
 * </pre>
 * 
 * Asynchronous tasks are typically submitted to the ManagedExecutorService using one
 * of the {@code submit} methods, each of which return a {@link java.util.concurrent.Future}
 * instance.  The {@code Future} represents the result of the task and can also be used to
 * check if the task is complete or wait for its completion.
 * <p>
 *
 * If the task is canceled, the result for the task is a
 * {@link java.util.concurrent.CancellationException} exception.  If the task is unable
 * to run due to a reason other than cancellation, the result is a
 * {@link AbortedException} exception.
 * <p>
 *
 *Example:
 *<pre>
 * &#47;**
 *  * Retrieve all accounts from several account databases in parallel.
 *  * Resource Mappings:
 *  *  type:      javax.enterprise.concurrent.ManagedExecutorService
 *  *  jndi-name: concurrent/ThreadPool
 *  *&#47;
 * public List&lt;Account&gt; getAccounts(long accountId) {
 *   try {
 *       javax.naming.InitialContext ctx = new InitialContext();
 *       <b>ManagedExecutorService mes = (ManagedExecutorService)
 *           ctx.lookup("java:comp/env/concurrent/ThreadPool");</b>
 *
 *       // Create a set of tasks to perform the account retrieval.
 *       ArrayList&lt;Callable&lt;Account&gt;&gt; retrieverTasks = new ArrayList&lt;Callable&lt;Account&gt;&gt;();
 *       retrieverTasks.add(new EISAccountRetriever());
 *       retrieverTasks.add(new RDBAccountRetriever());
 *
 *       // Submit the tasks to the thread pool and wait for them
 *       // to complete (successfully or otherwise).
 *       <b>List&lt;Future&lt;Account&gt;&gt; taskResults= mes.invokeAll(retrieverTasks);</b>
 *
 *       // Retrieve the results from the resulting Future list.
 *       ArrayList&lt;Account&gt; results = new ArrayList&lt;Account&gt;();
 *       for(Future&lt;Account&gt; taskResult : taskResults) {
 *           try {
 *               <b>results.add(taskResult.get());</b>
 *           } catch (ExecutionException e) {
 *               Throwable cause = e.getCause();
 *               // Handle the AccountRetrieverError.
 *           }
 *       }
 *
 *       return results;
 *
 *   } catch (NamingException e) {
 *       // Throw exception for fatal error.
 *   } catch (InterruptedException e) {
 *       // Throw exception for shutdown or other interrupt condition.
 *   }
 * }
 *
 *
 * public class EISAccountRetriever implements Callable&lt;Account&gt; {
 *     public Account call() {
 *         // Connect to our eis system and retrieve the info for the account.
 *         //...
 *         return null;
 *   }
 * }
 *
 * public class RDBAccountRetriever implements Callable&lt;Account>&gt; {
 *     public Account call() {
 *         // Connect to our database and retrieve the info for the account.
 *         //...
 *   }
 * }
 *
 * public class Account {
 *     // Some account data...
 * }
 * </pre>
 * <P>
 * 
 * @since 1.0
 */
public interface ManagedExecutorService extends ExecutorService {

}
