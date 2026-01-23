/*
 * Copyright (c) 2021,2026 Contributors to the Eclipse Foundation
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
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import jakarta.enterprise.util.AnnotationLiteral;
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
 * <h2>Automatic submit on startup</h2>
 *
 * <p>Asynchronous methods can be submitted automaticaly on application startup
 * by observing the application's {@code jakarta.enterprise.Startup} event.
 * For example,</p>
 *
 * <pre>
 * {@literal @}Asynchronous(runAt = {@literal @}Schedule(cron = "0 9 * SEP-MAY TUE,THU", zone = "America/Chicago"))
 * public void tuesdaysAndThursdaysAt9({@literal @}Observes Startup event) {
 *     // ...
 * }
 * </pre>
 *
 * <h2>Return types</h2>
 *
 * <p>
 * Methods with the following return types can be annotated to be
 * asynchronous methods:
 * <ul>
 * <li>{@link java.util.concurrent.CompletableFuture CompletableFuture}</li>
 * <li>{@link java.util.concurrent.CompletionStage CompletionStage}</li>
 * <li><code>void</code></li>
 * </ul>
 *
 * <h2>Exceptions</h2>
 *
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
 *
 * <h2>Interceptor and Transactional</h2>
 *
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
     * <p>Establishes a schedule for repeated execution of the method.
     * A single future represents the completion of all executions in the schedule.
     * The Jakarta EE product attempts to run the method at the scheduled times
     * until its future is completed or the method returns a non-null result value
     * or raises an exception. The future is always accessible from within the method via
     * {@code Asynchronous.Result} which returns a typed {@code CompletableFuture} which matches the return
     * type of the method. If the method return type is {@code void} then {@code Asynchronous.Result}
     * will return a {@code CompletableFuture<Void>}.</p>
     *
     * <p>Computation of the start time for the next execution occurs
     * after the completion of the current execution. This prevents overlap
     * of executions from the same asynchronous method request. Scheduled
     * execution times that overlap a prior execution that is still running
     * are skipped. For example, if an asynchronous method is scheduled to
     * run every minute on the minute and execution of the method starts at
     * 8:00 AM, lasting for 2 minutes and 10 seconds, then the Jakarta EE product
     * attempts to start the next execution of the method at 8:03 AM.</p>
     *
     * <p>Scheduled asynchronous methods are treated similar to other scheduled
     * tasks in that they are not subject to {@code max-async} constraints of
     * {@code managed-scheduled-executor-definition} and
     * {@code managed-executor-definition} and the corresponding
     * {@link ManagedScheduledExecutorDefinition#maxAsync()} and
     * {@link ManagedExecutorDefinition#maxAsync()}.</p>
     *
     * <p>When a list of multiple {@link Schedule} annotations is specified,
     * the next execution time is computed according to each, choosing the
     * closest future time after the current time. This allows composite
     * schedules such as,
     * </p>
     *
     * <pre>
     * {@literal @}Asynchronous(runAt = {
     *     {@literal @}Schedule(daysOfWeek = { DayOfWeek.TUESDAY, DayOfWeek.THURSDAY }, hours = 8),
     *     {@literal @}Schedule(daysOfweek = DayOfWeek.WEDNESDAY, hours = 10, minutes = 30)
     * })
     * public CompletableFuture{@literal <String>} attendLectureAndLab(String course) {
     *     ...
     *     if (endOfSemester)
     *         return Asynchronous.Result.complete(courseRecord);
     *     else
     *         return null; // continue at next scheduled time
     * }
     *
     * ...
     *
     * student.attendLectureAndLab(courseName).thenApply(this::assignGrade);
     * </pre>
     *
     * <p>The default value of empty array indicates that the task does not
     * run on a schedule and instead runs one time.</p>
     *
     * @return a schedule for the task or an empty array, where the latter indicates
     *         to run once without a schedule.
     */
    @Nonbinding
    Schedule[] runAt() default {};

    /**
     * Enables instances of the {@link Asynchronous} annotation to be created
     * at run time.
     */
    public static final class Literal
            extends AnnotationLiteral<Asynchronous>
            implements Asynchronous {

        /**
         * Instance of the {@link Asynchronous} annotation with all values
         * set to their defaults.
         */
        public static final Literal INSTANCE =
                new Literal("java:comp/DefaultManagedExecutorService",
                            new Schedule[0]);

        private static final long serialVersionUID = 1L;

        private final String executor;

        private final Schedule[] runAt;

        private Literal(final String executor,
                        final Schedule[] runAt) {
            this.executor = executor;
            this.runAt = runAt;
        }

        /**
         * The JNDI name of the {@link ManagedExecutorService} or
         * {@link ManagedScheduledExecutorService} upon which to run the
         * asynchronous method.
         *
         * @return managed executor service JNDI name.
         */
        @Override
        public String executor() {
            return executor;
        }

        /**
         * Construct a new instance of the {@link Asynchronous} annotation.
         *
         * @param executor JNDI name of the {@link ManagedExecutorService} or
         *                 {@link ManagedScheduledExecutorService} upon which
         *                 to run the asynchronous method.
         * @param runAt    A schedule for repeated execution of the method.
         *                 Otherwise an empty array.
         * @return a new instance of the {@code Asynchronous} annotation.
         */
        public static Literal of(final String executor,
                                 final Schedule[] runAt) {

            Objects.requireNonNull(executor, "executor: null");
            Objects.requireNonNull(runAt, "runAt: null");

            return new Literal(
                    executor,
                    runAt.length == 0
                            ? runAt
                            : Arrays.copyOf(runAt, runAt.length));
        }

        /**
         * A schedule for repeated execution of the method.
         * Otherwise an empty array.
         *
         * @return the schedule for the asynchronous method.
         */
        @Override
        public Schedule[] runAt() {
            return runAt.length == 0
                    ? runAt
                    : Arrays.copyOf(runAt, runAt.length);
        }
    }

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
    public static final class Result {
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
