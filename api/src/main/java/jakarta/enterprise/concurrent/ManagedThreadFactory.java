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

import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ThreadFactory;

/**
 * A manageable version of a <code>ThreadFactory</code>.<p>
 *
 * A ManagedThreadFactory extends the Java&trade; SE ThreadFactory to provide
 * a method for creating threads for execution in a Jakarta&trade; EE environment.
 * Implementations of the ManagedThreadFactory are
 * provided by a Jakarta EE Product Provider.  Application Component Providers
 * use the Java Naming and Directory Interface&trade; (JNDI) to look-up instances of one
 * or more ManagedThreadFactory objects using resource environment references.<p>
 *
 * The Jakarta Concurrency specification describes several
 * behaviors that a ManagedThreadFactory can implement.  The Application
 * Component Provider and Deployer identify these requirements and map the
 * resource environment reference appropriately.<p>
 *
 * Threads returned from the {@code newThread()} method should implement the
 * {@link ManageableThread} interface.
 *
 * The Runnable task that is allocated to the new thread using the
 * {@link ThreadFactory#newThread(Runnable)} method
 * will run with the application component context of the component instance
 * that created (looked-up) this ManagedThreadFactory instance.<p>
 *
 * The {@link java.util.concurrent.ForkJoinWorkerThread} that is created by the
 * {@link ForkJoinWorkerThreadFactory#newThread(ForkJoinPool)} method
 * runs tasks with the application component context of the component instance
 * that created (looked-up) this ManagedThreadFactory instance.
 * The Jakarta EE Product Provider establishes the context once per
 * <code>ForkJoinWorkerThread</code> and does not reset the context
 * between operations that run on the {@link java.util.concurrent.ForkJoinWorkerThread}.<p>
 *
 * The task runs without an explicit transaction (they do not enlist in the application
 * component's transaction).  If a transaction is required, use a
 * <code>jakarta.transaction.UserTransaction</code> instance.  A UserTransaction instance is
 * available in JNDI using the name: &quot;java:comp/UserTransaction&quot;<p>
 *
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
 * }</pre>
 *
 * A ManagedThreadFactory can be used with Java SE ExecutorService implementations directly.<p>
 *
 * Example:
 * <pre>
 * &#47;**
 * * Create a ThreadPoolExecutor using a ManagedThreadFactory.
 * * Resource Mappings:
 * *  type:      jakarta.enterprise.concurrent.ManagedThreadFactory
 * *  jndi-name: concurrent/tf/DefaultThreadFactory
 * *&#47;
 *
 * &#64;Resource(name="concurrent/tf/DefaultThreadFactory")
 * ManagedThreadFactory tf;
 *
 * public ExecutorService getManagedThreadPool() {
 *
 *   // All threads will run as part of this application component.
 *   return new ThreadPoolExecutor(5, 10, 5, TimeUnit.SECONDS,
 *       new ArrayBlockingQueue&lt;Runnable&gt;(10), tf);
 * }
 * </pre>
 * <P>
 *
 * ForkJoinPool Example:
 * <pre>
 * ManagedThreadFactory threadFactory =
 *   InitialContext.doLookup("java:comp/DefaultManagedThreadFactory");
 *
 * ForkJoinPool pool = new ForkJoinPool(
 *     Runtime.getRuntime().availableProcessors(), threadFactory, null, false);
 *
 * ForkJoinTask&lt;Double&gt; totals = pool.submit(() -&gt; orders
 *     .parallelStream()
 *     .map(order -&gt; {
 *         if (order.total == 0.0) {
 *             // lookups require application component namespace:
 *             try (Connection con = ((DataSource)
 *                     InitialContext.doLookup(
 *                             "java:comp/env/jdbc/ds1"))
 *                     .getConnection()) {
 *                 order.total = ...
 *             } catch (NamingException | SQLException x) {
 *                 throw new CompletionException(x);
 *             }
 *         }
 *         return order.total;
 *      })
 *     .reduce(0.0, Double::sum));
 *
 * System.out.println("Sum is: " + totals.join());
 * pool.shutdown();
 * </pre>
 * <P>
 *
 * @since 1.0
 */
public interface ManagedThreadFactory extends ThreadFactory, ForkJoinWorkerThreadFactory {

}
