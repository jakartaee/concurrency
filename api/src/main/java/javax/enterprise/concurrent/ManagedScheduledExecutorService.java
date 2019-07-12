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

import java.util.concurrent.ScheduledExecutorService;

/**
 * A manageable version of a {@link java.util.concurrent.ScheduledExecutorService}.<p>
 *
 * A ManagedScheduledExecutorService extends the Java&trade; SE ScheduledExecutorService
 * to provide methods for submitting delayed or periodic tasks for execution in
 * a Jakarta&trade; EE environment.
 * Implementations of the ManagedScheduledExecutorService are
 * provided by a Jakarta EE Product Provider.  Application Component Providers
 * use the Java Naming and Directory Interface&trade; (JNDI) to look-up instances of one
 * or more ManagedScheduledExecutorService objects using resource environment references.
 * ManagedScheduledExecutorService instances can also be injected into application
 * components through the use of the {@code Resource} annotation.<p>
 *
 * The Jakarta Concurrency specification describes several
 * behaviors that a ManagedScheduledExecutorService can implement.  The Application
 * Component Provider and Deployer identify these requirements and map the
 * resource environment reference appropriately.<p>
 *
 * Tasks are run in managed threads provided by the Jakarta&trade; EE Product Provider
 * and are run within the application component context that submitted the task.
 * All tasks run without an explicit transaction (they do not enlist in the application
 * component's transaction).  If a transaction is required, use a
 * {@code javax.transaction.UserTransaction} instance.  A UserTransaction instance is
 * available in JNDI using the name: &QUOT;java:comp/UserTransaction&QUOT or by
 * requesting an injection of a {@link javax.transaction.UserTransaction} object
 * using the {@code Resource} annotation.<p>
 *
 * Example:
 * <pre>
 *
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
 * </PRE>
 * Tasks can optionally provide an {@link ManagedTaskListener} to receive 
 * notifications of lifecycle events, through the use of {@link ManagedTask}
 * interface.
 * <p>
 *
 * Asynchronous tasks are typically submitted to the ManagedScheduledExecutorService using one
 * of the <code>submit</code> or <code>schedule</code>methods, each of which return a <CODE>Future</CODE>
 * instance.  The Future represents the result of the task and can also be used to
 * check if the task is complete or wait for its completion.<p>
 *
 * If the task is cancelled, the result for the task is a
 * <CODE>CancellationException</CODE> exception.  If the task is unable
 * to run due to start due to a reason other than cancellation, the result is a
 * {@link AbortedException} exception.  If the task is scheduled
 * with a {@link Trigger} and the Trigger forces the task to be skipped,
 * the result will be a {@link SkippedException} exception.<p>
 *
 * Tasks can be scheduled to run periodically using the <code>schedule</code> methods that
 * take a <code>Trigger</code> as an argument and the <code>scheduleAtFixedRate</code> and
 * <code>scheduleWithFixedDelay</code> methods.  The result of the <code>Future</code> will
 * be represented by the currently scheduled or running instance of the task.  Future and past executions
 * of the task are not represented by the Future.  The state of the <code>Future</code> will therefore change
 * and multiple results are expected.<p>
 *
 * For example, if a task is repeating, the lifecycle of the task would be:<br>
 * (Note:  See {@link ManagedTaskListener} for task lifecycle management details.)<p>
 *
 * <table>
 * <tr><td valign="top"><strong>Sequence</strong></td><td valign="top"><strong>State</strong></td><td valign="top"><strong>Action</strong></td><td valign="top"><strong>Listener</strong></td><td valign="top"><strong>Next state</strong></td></tr>
 *
 * <tr><td valign="top">1A.</td><td valign="top">None</td><td valign="top">submit()</td><td valign="top">taskSubmitted</td><td valign="top">Submitted</td></tr>
 * <tr><td valign="top">2A.</td><td valign="top">Submitted</td><td valign="top">About to call run()</td><td valign="top">taskStarting</td><td valign="top">Started</td></tr>
 * <tr><td valign="top">3A.</td><td valign="top">Started</td><td valign="top">Exit run()</td><td valign="top">taskDone</td><td valign="top">Reschedule</td></tr>
 *
 * <tr><td valign="top">1B.</td><td valign="top">Reschedule</td><td valign="top"></td><td valign="top">taskSubmitted</td><td valign="top">Submitted</td></tr>
 * <tr><td valign="top">2B.</td><td valign="top">Submitted</td><td valign="top">About to call run()</td><td valign="top">taskStarting</td><td valign="top">Started</td></tr>
 * <tr><td valign="top">3B.</td><td valign="top">Started</td><td valign="top">Exit run()</td><td valign="top">taskDone</td><td valign="top">Reschedule</td></tr>
 *
 * </table>
 * <P>
 *
 * @since 1.0
 */
public interface ManagedScheduledExecutorService extends
    ManagedExecutorService, ScheduledExecutorService {

  /**
   * Creates and executes a task based on a Trigger. The Trigger determines when the task 
   * should run and how often.
   *  
   * @param command the task to execute.
   * @param trigger the trigger that determines when the task should fire.
   * 
   * @return a Future representing pending completion of the task, and whose <code>get()</code> 
   *         method will return <code>null</code> upon completion. 
   * 
   * @throws java.util.concurrent.RejectedExecutionException if task cannot be scheduled for execution.
   * @throws java.lang.NullPointerException if command is null.
   */
  public java.util.concurrent.ScheduledFuture<?> schedule(java.lang.Runnable command,
	                                                  Trigger trigger);
  
  /**
   * Creates and executes a task based on a Trigger. The Trigger determines when the task should 
   * run and how often. 
   * 
   * @param callable the function to execute.
   * @param trigger the trigger that determines when the task should fire.
   * 
   * @return a ScheduledFuture that can be used to extract result or cancel.
   * 
   * @throws java.util.concurrent.RejectedExecutionException if task cannot be scheduled for execution.
   * @throws java.lang.NullPointerException if callable is null.
   * 
   */
  public <V> java.util.concurrent.ScheduledFuture<V> schedule(java.util.concurrent.Callable<V> callable,
                                                              Trigger trigger);
    
}
