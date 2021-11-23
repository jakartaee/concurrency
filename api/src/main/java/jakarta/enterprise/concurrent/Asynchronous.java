/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.CompletableFuture;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

/**
 * Annotates a CDI managed bean method to run asynchronously.
 * The CDI managed bean must not be a Jakarta Enterprise Bean,
 * and neither the method nor its class can be annotated with
 * the MicroProfile Asynchronous annotation.
 * <p>
 * The Jakarta EE Product Provider runs the method on a {@link ManagedExecutorService}
 * and returns to the caller a {@link java.util.concurrent.CompletableFuture CompletableFuture}
 * that is backed by the same <code>ManagedExecutorService</code>
 * to represent the execution of the method. The <code>ManagedExecutorService</code>
 * is the default asynchronous execution facility for the <code>CompletableFuture</code>
 * and and all dependent stages that are created from those, and so on,
 * as defined by the <code>ManagedExecutorService</code> JavaDoc API.
 * The Jakarta EE Product Provider makes this <code>CompletableFuture</code> available
 * to the asynchronous method implementation via the
 * {@link Result#getFuture Asynchronous.Result.getFuture} and
 * {@link Result#complete Asynchronous.Result.complete} methods.
 * <p>
 * For example,
 *
 * <pre>
 * {@literal @}Asynchronous
 * public CompletableFuture{@literal <Double>} hoursWorked(LocalDate from, LocalDate to) {
 *     // Application component's context is made available to the async method,
 *     try (Connection con = ((DataSource) InitialContext.doLookup(
 *         "java:comp/env/jdbc/timesheetDB")).getConnection()) {
 *         ...
 *         return Asynchronous.Result.complete(total);
 *     } catch (NamingException | SQLException x) {
 *         throw new CompletionException(x);
 *     }
 * }
 * </pre>
 *
 * with usage,
 *
 * <pre>
 * hoursWorked(mon, fri).thenAccept(total {@literal ->} {
 *     // Application component's context is made available to dependent stage actions,
 *     DataSource ds = InitialContext.doLookup(
 *         "java:comp/env/jdbc/payrollDB");
 *     ...
 * });
 * </pre>
 *
 * When the asynchronous method implementation returns a different
 * <code>CompletableFuture</code> instance, the Jakarta EE Product Provider
 * uses the completion of that instance to complete the <code>CompletableFuture</code>
 * that the Jakarta EE Product Provider returns to the caller,
 * completing it with the same result or exception.
 * <p>
 * For example,
 *
 * <pre>
 * {@literal @}Asynchronous
 * public CompletableFuture{@literal <List<Itinerary>>} findSingleLayoverFlights(Location source, Location dest) {
 *     try {
 *         ManagedExecutorService executor = InitialContext.doLookup(
 *             "java:comp/DefaultManagedExecutorService");
 *
 *         return executor.supplyAsync(source::flightsFrom)
 *                        .thenCombine(executor.completedFuture(dest.flightsTo()),
 *                                     Itinerary::sourceMatchingDest);
 *     } catch (NamingException x) {
 *         throw new CompletionException(x);
 *     }
 * }
 * </pre>
 *
 * with usage,
 *
 * <pre>
 * findSingleLayoverFlights(RST, DEN).thenApply(Itinerary::sortByPrice);
 * </pre>
 *
 * <p>
 * Methods with the following return types can be annotated to be
 * asynchronous methods:
 * <ul>
 * <li>{@link java.util.concurrent.CompletableFuture CompletableFuture}</li>
 * <li>{@link java.util.concurrent.CompletionStage CompletionStage}</li>
 * <li><code>void</code></li>
 * </ul>
 * <p>
 * The Jakarta EE Product Provider raises
 * {@link java.lang.UnsupportedOperationException UnsupportedOperationException}
 * if other return types are used or if the annotation is placed at the class
 * level. The injection target of <code>ElementType.TYPE</code> is to be used only
 * by the CDI extension that is implemented by the Jakarta EE Product Provider to
 * register the asynchronous method interceptor. Applications must only use the
 * asynchronous method annotation at method level.
 * <p>
 * Exceptions that are raised by asynchronous methods are not raised directly
 * to the caller because the method runs asynchronously to the caller.
 * Instead, the <code>CompletableFuture</code> that represents the result
 * is completed with the raised exception. Asynchronous methods are
 * discouraged from raising checked exceptions because checked exceptions
 * force the caller to write exception handling code that is unreachable.
 * When a checked exception occurs, the asynchronous method implementation
 * can flow the exception back to the resulting <code>CompletableFuture</code>
 * either by raising a
 * {@link java.util.concurrent.CompletionException CompletionException}
 * with the original exception as the cause, or it can take the equivalent
 * approach of exceptionally completing the <code>CompletableFuture</code>, using
 * {@link java.util.concurrent.CompletableFuture#completeExceptionally completeExceptionally}
 * to supply the original exception as the cause.
 * <p>
 * Except where otherwise stated, the Jakarta EE Product Provider raises
 * {@link java.util.concurrent.RejectedExecutionException RejectedExecutionException}
 * upon invocation of the asynchronous method if evident upfront that it cannot
 * be accepted, for example if the JNDI name is not valid or points to something
 * other than a managed executor resource. If determined at a later point that the
 * asynchronous method cannot run (for example, if unable to establish thread context),
 * then the Jakarta EE Product Provider completes the <code>CompletableFuture</code>
 * exceptionally with {@link java.util.concurrent.CancellationException CancellationException},
 * and chains a cause exception if there is any.
 * <p>
 * The Jakarta EE Product Provider must assign the interceptor for asynchronous methods
 * to have priority of <code>Interceptor.Priority.PLATFORM_BEFORE + 5</code>.
 * Interceptors with a lower priority, such as <code>Transactional</code>, must run on
 * the thread where the asynchronous method executes, rather than on the submitting thread.
 * When an asynchronous method is annotated as <code>Transactional</code>,
 * the transactional types which can be used are:
 * <code>TxType.REQUIRES_NEW</code>, which causes the method to run in a new transaction, and
 * <code>TxType.NOT_SUPPORTED</code>, which causes the method to run with no transaction.
 * All other transaction attributes must result in
 * {@link java.lang.UnsupportedOperationException UnsupportedOperationException}
 * upon invocation of the asynchronous method.
 *
 * @since 3.0
 */
// TODO the above restrictions on Transactional interceptors could be eliminated
// if transaction context propagation is later added to the spec.
@Documented
@Inherited
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Asynchronous {
    /**
     * JNDI name of a {@link ManagedExecutorService} or {@link ManagedScheduledExecutorService}
     * upon which to run the asynchronous method.
     * <p>
     * The default value is the JNDI name of the built-in <code>ManagedExecutorService</code>
     * that is provided by the Jakarta EE platform provider:<br>
     * <code>java:comp/DefaultManagedExecutorService</code>
     *
     * @return managed executor service JNDI name.
     */
    @Nonbinding
    String executor() default "java:comp/DefaultManagedExecutorService";

    /**
     * Mechanism by which the Jakarta EE Product Provider makes available
     * to the asynchronous method implementation the same
     * {@link java.util.concurrent.CompletableFuture CompletableFuture}
     * instance that the Jakarta EE Product Provider supplies to the caller
     * of the asynchronous method.
     * <p>
     * Before invoking the asynchronous method implementation on a thread,
     * the Jakarta EE Product Provider invokes the {@link #setFuture} method
     * which makes available to the asynchronous method implementation
     * the same <code>CompletableFuture</code> that the Jakarta EE Product Provider
     * returns to the caller.
     * <p>
     * The asynchronous method implementation invokes the {@link #getFuture} method
     * to obtain the same <code>CompletableFuture</code> that the
     * Jakarta EE Product Provider returns to the caller.
     * The asynchronous method implementation can choose to complete
     * this future (normally or exceptionally) or otherwise arrange for its
     * completion, for example upon completion of a pipeline of completion stages.
     * Having this same <code>CompletableFuture</code> also enables the asynchronous
     * method implementation to determine if the caller has forcibly completed
     * (such as by cancellation or any other means) the <code>CompletableFuture</code>,
     * in which case the asynchronous method implementation could decide to end
     * immediately rather than continue processing.
     * <p>
     * For example,
     *
     * <pre>
     * {@literal @}Asynchronous
     * public CompletableFuture{@literal <Double>} hoursWorked(LocalDateTime from, LocalDateTime to) {
     *     CompletableFuture{@literal <Double>} future = Asynchronous.Result.getFuture();
     *     if (future.isDone())
     *         return future;
     *
     *     try (Connection con = ((DataSource) InitialContext.doLookup(
     *         "java:comp/env/jdbc/timesheetDB")).getConnection()) {
     *         ...
     *         for (ResultSet result = stmt.executeQuery(); result.next() {@literal &&} !future.isDone(); )
     *             ...
     *         future.complete(total);
     *     } catch (NamingException | SQLException x) {
     *         future.completeExceptionally(x);
     *     }
     *     return future;
     * }
     * </pre>
     *
     * After the asynchronous method completes, the Jakarta EE Product Provider
     * invokes the {@link #setFuture} method with a <code>null</code> value
     * to clear it from the thread.
     *
     * @since 3.0
     */
    public static class Result {
        private static final ThreadLocal<CompletableFuture<?>> FUTURES = new ThreadLocal<CompletableFuture<?>>();

        // Prevent instantiation
        private Result() {
        }

        /**
         * Completes the {@link java.util.concurrent.CompletableFuture CompletableFuture}
         * instance that the Jakarta EE Product Provider supplies to the caller of the
         * asynchronous method.
         * <p>
         * This method must only be invoked by the asynchronous method implementation.
         *
         * @param <T>    type of result returned by the asynchronous method's <code>CompletableFuture</code>.
         * @param result result with which to complete the asynchronous method's <code>CompletableFuture</code>.
         * @return the same <code>CompletableFuture</code> that the container returns to the caller.
         * @throws IllegalStateException if the <code>CompletableFuture</code> for an asynchronous
         *                                   method is not present on the thread.
         */
        public static <T> CompletableFuture<T> complete(final T result) {
            @SuppressWarnings("unchecked")
            CompletableFuture<T> future = (CompletableFuture<T>) FUTURES.get();
            if (future == null) {
                throw new IllegalStateException();
            }
            future.complete(result);
            return future;
        }

        /**
         * Obtains the same {@link java.util.concurrent.CompletableFuture CompletableFuture}
         * instance that the Jakarta EE Product Provider supplies to the caller of the
         * asynchronous method.
         * <p>
         * This method must only be invoked by the asynchronous method implementation.
         *
         * @param <T>  type of result returned by the asynchronous method's <code>CompletableFuture</code>.
         * @return the same <code>CompletableFuture</code> that the container returns to the caller.
         * @throws IllegalStateException if the <code>CompletableFuture</code> for an asynchronous
         *                                   method is not present on the thread.
         */
        public static <T> CompletableFuture<T> getFuture() {
            @SuppressWarnings("unchecked")
            CompletableFuture<T> future = (CompletableFuture<T>) FUTURES.get();
            if (future == null) {
                throw new IllegalStateException();
            }
            return future;
        }

        /**
         * Before invoking the asynchronous method implementation on a thread,
         * the Jakarta EE Product Provider invokes this method to make available
         * to the asynchronous method implementation the same <code>CompletableFuture</code>
         * that the Jakarta EE Product Provider returns to the caller.
         * <p>
         * After the asynchronous method completes, the Jakarta EE Product Provider
         * invokes this method with a <code>null</code> value
         * to clear it from the thread.
         * <p>
         * This method must only be invoked by the Jakarta EE Product Provider.
         *
         * @param <T>    type of result returned by the asynchronous method's <code>CompletableFuture</code>.
         * @param future <code>CompletableFuture</code> that the container returns to the caller,
         *               or <code>null</code> to clear it.
         */
        public static <T> void setFuture(final CompletableFuture<T> future) {
            if (future == null) {
                FUTURES.remove();
            } else {
                FUTURES.set(future);
            }
        }
    }
}
