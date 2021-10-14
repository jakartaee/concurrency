/*
 * Copyright (c) 2010, 2021 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.enterprise.concurrent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

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
 * {@code jakarta.transaction.UserTransaction} instance.  A UserTransaction instance is
 * available in JNDI using the name: &quot;java:comp/UserTransaction&quot; or by
 * requesting an injection of a {@code jakarta.transaction.UserTransaction} object
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
 *  *  type:      jakarta.enterprise.concurrent.ManagedExecutorService
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
 * public class RDBAccountRetriever implements Callable&lt;Account&gt; {
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
 *
 * <p>ManagedExecutorService provides various methods which correspond to the
 * static methods of {@link java.util.concurrent.CompletableFuture} and its
 * constructor/<code>newIncompleteFuture</code> method,
 * enabling you to create completion stages that are backed by the <code>ManagedExecutorService</code>
 * as the default asynchronous execution facility, both for those stages
 * as well as all dependent stages that are created from those, and so on.
 * This allows you to create pipelines of completion stage actions that run
 * with consistent and predictable thread context, regardless of which thread each
 * dependent action ends up running on.</p>
 *
 * <p>Example:</p>
 * <pre>
 * <code>ManagedExectorService executor = InitialContext.doLookup("java:comp/DefaultManagedExecutorService");
 * ...
 * CompletableFuture&lt;Integer&gt; future = executor
 *    .supplyAsync(supplier)
 *    .thenApply(function1)
 *    .thenApplyAsync(function2)
 *    ...
 * </code>
 * </pre>
 *
 * <p>Context propagation to completion stages that are backed by a
 * <code>ManagedExecutorService</code> must be done in a consistent
 * and predictable manner, which is defined as follows,</p>
 *
 * <ul>
 * <li>
 * If the supplied action is already contextual, (for example,
 * <code>contextService.createContextualProxy(action, Runnable.class)</code>),
 * then the action runs with the already-captured context.
 * </li>
 * <li>
 * Otherwise, each type of thread context is either propagated from the thread
 * that creates the completion stage or the context is marked to be cleared, according to the
 * configuration of the <code>ManagedExecutorService</code> that is the default asynchronous execution facility
 * for the new stage and its parent stage. In the case that a <code>ManagedExecutorService</code> is supplied
 * as the <code>executor</code> argument to a <code>*Async</code> method, the supplied
 * <code>ManagedExecutorService</code> is used to run the action, but not to determine the thread context
 * propagation and clearing.
 * </li>
 * </ul>
 *
 * <p>Each type of thread context is applied (either as cleared or previously captured)
 * to the thread that runs the action. The applied thread context is removed after the action
 * completes, whether successfully or exceptionally, restoring the thread's prior context.</p>
 *
 * <p>When dependent stages are created from the completion stage, and likewise from any dependent stages
 * created from those, and so on, thread context is captured or cleared in the same manner.
 * This guarantees that the action performed by each stage always runs under the thread context
 * of the code that creates the completion stage, unless the user explicitly overrides this by supplying a
 * pre-contextualized action.</p>
 *
 * <p>Completion stages that are backed by a <code>ManagedExecutorService</code> must raise
 * {@link java.lang.IllegalArgumentException} if supplied with an action that implements
 * {@link ManagedTask}.</p>
 *
 * @since 1.0
 */
public interface ManagedExecutorService extends ExecutorService {
    /**
     * <p>Returns a new {@link java.util.concurrent.CompletableFuture}
     * that is already completed with the specified value.</p>
     *
     * <p>This executor is the default asynchronous execution facility for the new completion stage
     * that is returned by this method and all dependent stages that are created from it,
     * and all dependent stages that are created from those, as so forth.</p>
     *
     * @param value result with which the completable future is completed.
     * @param <U> result type of the completable future.
     * @return the new completable future.
     * @since 3.0
     */
    <U> CompletableFuture<U> completedFuture(U value);

    /**
     * <p>Returns a new {@link java.util.concurrent.CompletionStage}
     * that is already completed with the specified value.</p>
     *
     * <p>This executor is the default asynchronous execution facility for the new completion stage
     * that is returned by this method and all dependent stages that are created from it,
     * and all dependent stages that are created from those, as so forth.</p>
     *
     * @param value result with which the completion stage is completed.
     * @param <U> result type of the completion stage.
     * @return the new completion stage.
     * @since 3.0
     */
    <U> CompletionStage<U> completedStage(U value);

    /**
     * <p>
     * Returns a new {@link java.util.concurrent.CompletableFuture}
     * that is completed by the completion of the
     * specified stage.
     * </p>
     *
     * <p>
     * The new completable future is backed by the <code>ManagedExecutorService</code> upon which copy is invoked,
     * which serves as the default asynchronous execution facility
     * for the new stage and all dependent stages created from it, and so forth.
     * </p>
     *
     * <p>
     * When dependent stages are created from the new completable future, thread context is captured
     * and/or cleared by the <code>ManagedExecutorService</code>. This guarantees that the action
     * performed by each stage always runs under the thread context of the code that creates the stage,
     * unless the user explicitly overrides by supplying a pre-contextualized action.
     * </p>
     *
     * <p>
     * Invocation of this method does not impact thread context propagation for the supplied
     * completable future or any other dependent stages directly created from it.
     * </p>
     *
     * @param <T> completable future result type.
     * @param stage a completable future whose completion triggers completion of the new completable
     *        future that is created by this method.
     * @return the new completable future.
     * @since 3.0
     */
    <T> CompletableFuture<T> copy(CompletableFuture<T> stage);

    /**
     * <p>
     * Returns a new {@link java.util.concurrent.CompletionStage}
     * that is completed by the completion of the
     * specified stage.
     * </p>
     *
     * <p>
     * The new completion stage is backed by the <code>ManagedExecutorService</code> upon which copy is invoked,
     * which serves as the default asynchronous execution facility
     * for the new stage and all dependent stages created from it, and so forth.
     * </p>
     *
     * <p>
     * When dependent stages are created from the new completion stage, thread context is captured
     * and/or cleared by the <code>ManagedExecutorService</code>. This guarantees that the action
     * performed by each stage always runs under the thread context of the code that creates the stage,
     * unless the user explicitly overrides by supplying a pre-contextualized action.
     * </p>
     *
     * <p>
     * Invocation of this method does not impact thread context propagation for the supplied
     * stage or any other dependent stages directly created from it.
     * </p>
     *
     * @param <T> completion stage result type.
     * @param stage a completion stage whose completion triggers completion of the new stage
     *        that is created by this method.
     * @return the new completion stage.
     * @since 3.0
     */
    <T> CompletionStage<T> copy(CompletionStage<T> stage);

    /**
     * <p>Returns a new {@link java.util.concurrent.CompletableFuture}
     * that is already exceptionally completed with the specified <code>Throwable</code>.</p>
     *
     * <p>This executor is the default asynchronous execution facility for the new completion stage
     * that is returned by this method and all dependent stages that are created from it,
     * and all dependent stages that are created from those, as so forth.</p>
     *
     * @param ex exception or error with which the completable future is completed.
     * @param <U> result type of the completable future.
     * @return the new completable future.
     * @since 3.0
     */
    <U> CompletableFuture<U> failedFuture(Throwable ex);

    /**
     * <p>Returns a new {@link java.util.concurrent.CompletionStage}
     * that is already exceptionally completed with the specified <code>Throwable</code>.</p>
     *
     * <p>This executor is the default asynchronous execution facility for the new completion stage
     * that is returned by this method and all dependent stages that are created from it,
     * and all dependent stages that are created from those, as so forth.</p>
     *
     * @param ex exception or error with which the completion stage is completed.
     * @param <U> result type of the completion stage.
     * @return the new completion stage.
     * @since 3.0
     */
    <U> CompletionStage<U> failedStage(Throwable ex);

    /**
     * Returns a {@link ContextService} which has the same propagation settings as this <code>ManagedExecutorService</code>
     * and uses this <code>ManagedExecutorService</code> as the default asynchronous execution facility for
     * {@link java.util.concurrent.CompletionStage} and {@link java.util.concurrent.CompletableFuture} instances
     * that it creates via the <code>withContextCapture</code> methods.
     *
     * @return a <code>ContextService</code> with the same propagation settings
     *         as this <code>ManagedExecutorService</code>.
     * @since 3.0
     */
    public ContextService getContextService();

    /**
     * <p>Returns a new incomplete {@link java.util.concurrent.CompletableFuture}.</p>
     *
     * <p>This executor is the default asynchronous execution facility for the new completion stage
     * that is returned by this method and all dependent stages that are created from it,
     * and all dependent stages that are created from those, as so forth.</p>
     *
     * @param <U> result type of the completable future.
     * @return the new completable future.
     * @since 3.0
     */
    <U> CompletableFuture<U> newIncompleteFuture();

    /**
     * <p>Returns a new {@link java.util.concurrent.CompletableFuture}
     * that is completed by a task running in this executor
     * after it runs the given action.</p>
     *
     * <p>This executor is the default asynchronous execution facility for the new completion stage
     * that is returned by this method and all dependent stages that are created from it,
     * and all dependent stages that are created from those, as so forth.</p>
     *
     * @param runnable the action to run before completing the returned completion stage.
     * @return the new completable future.
     * @since 3.0
     */
    CompletableFuture<Void> runAsync(Runnable runnable);

    /**
     * <p>Returns a new {@link java.util.concurrent.CompletableFuture}
     * that is completed by a task running in this executor
     * after it runs the given action.</p>
     *
     * <p>This executor is the default asynchronous execution facility for the new completion stage
     * that is returned by this method and all dependent stages that are created from it,
     * and all dependent stages that are created from those, as so forth.</p>
     *
     * @param supplier an action returning the value to be used to complete the returned completion stage.
     * @param <U> result type of the supplier and returned completable stage.
     * @return the new completable future.
     * @since 3.0
     */
    <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier);
}
