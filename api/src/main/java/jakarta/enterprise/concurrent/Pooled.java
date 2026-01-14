/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

import jakarta.enterprise.context.NormalScope;
import jakarta.enterprise.util.AnnotationLiteral;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Specify that a bean is to be pooled.
 *
 * <p>
 * A pooled bean provides <em>max concurrency</em> (throttling) semantics. Instead of allowing an unbounded number of
 * concurrent method invocations, the container exposes a single contextual reference (a normal-scoped client proxy)
 * backed by a bounded pool of underlying bean instances. Each underlying instance is used by at most one thread at a
 * time.
 * </p>
 *
 * <p>
 * For each invocation of a business method through the pooled bean proxy, the container must first obtain an available
 * underlying instance from the pool. If an instance is available, the invocation proceeds using that instance and the
 * instance remains reserved for the duration of the invocation. When the invocation completes (normally or
 * exceptionally), the instance is returned to the pool and becomes available for other callers, or, depending on the
 * configured exception policy, is destroyed and replaced.
 * </p>
 *
 * <p>
 * The pool size, configured by {@link #value()}, defines the maximum number of concurrent invocations that may execute
 * at any one time. If more callers invoke the pooled bean proxy concurrently than there are underlying instances available,
 * callers must wait for an instance to become available, or fail with a {@link TimeoutException} if the configured
 * {@link #accessTimeout()} expires. This makes {@code @Pooled} suitable for protecting concurrency-sensitive resources
 * (for example, rate-limited downstream APIs, or non thread-safe objects). In effect, {@code value()} acts as a concurrency
 * limit (similar to a semaphore with {@code value()} permits) for invocations of the pooled bean.
 * </p>
 *
 * <p>
 * To avoid waiting, use {@link #IMMEDIATE}, which causes a {@link TimeoutException} to be thrown if no instance is
 * immediately available. To wait without timing out, use {@link #UNLIMITED}.
 * </p>
 *
 * <p>
 * Example: limit concurrency to 20 invocations, failing fast when the pool is exhausted.
 *
 * {@snippet lang="java" :
 *     @Pooled(value = 20, accessTimeout = Pooled.IMMEDIATE)
 *     public class ReportService {
 *         public Report generate(...) { ... }
 *     }
 * }
 * </p>
 *
 * <p>
 * Example: limit concurrency to 5 invocations, applying bounded backpressure.
 *
 * {@snippet lang="java" :
 *     @Pooled(value = 5, accessTimeout = 2, unit = TimeUnit.SECONDS)
 *     public class LegacyApiClient {
 *         public Response call(...) { ... }
 *     }
 * }
 * </p>
 *
 * <p>
 * Example: two successive calls through the same injected proxy may be serviced by different underlying instances.
 *
 * {@snippet lang="java" :
 *     @Pooled//
 *     public class SomeBean {
 *         public void myMethod() { }
 *     }
 *
 *     public class UsingBean {
 *
 *          @Inject//
 *          SomeBean someBean;
 *
 *          public void run() {
 *
 *              // myMethod call is directed to a free underlying instance for the duration of the call.
 *              someBean.myMethod();
 *
 *              // A subsequent call may be directed to a different underlying instance.
 *              someBean.myMethod();
 *          }
 *     }
 * }
 * </p>
 *
 * <p>
 * When configured via {@link #destroyOn()} and {@link #keepOn()}, the container may destroy an underlying instance after
 * an exceptional invocation in order to avoid reusing an instance that may have been left in an inconsistent state.
 * </p>
 *
 * <p>
 * The {@code @Pooled} annotation can only be applied to a bean class or a producer method that defines a bean.
 * It must not annotate individual bean methods.
 * </p>
 *
 * @since 3.2
 */
@Inherited
@NormalScope
@Documented
@Target({TYPE, METHOD})
@Retention(RUNTIME)
public @interface Pooled {

    /**
     * Value for {@link #accessTimeout} that indicates that the bean instance
     * running a bean method must be obtained immediately.
     * Otherwise, a {@link TimeoutException} is thrown.
     */
    long IMMEDIATE = 0;

    /**
     * Value for {@link #accessTimeout} that indicates to wait as long as necessary
     * to obtain the bean instance that permits running a bean method.
     */
    long UNLIMITED = -1;

    /**
     * {@return the maximum number of instances in the pool.}
     */
    int value() default 10;

    /**
     * <p>The maximum amount of time to wait to obtain a bean instance instance
     * from the pool to run a single bean method against.
     * If the lock cannot be obtained within the specified amount of time, a
     * {@link TimeoutException} is thrown.</p>
     *
     * <p>Use the {@link #IMMEDIATE} constant to avoid waiting.</p>
     *
     * <p>Use the {@link #UNLIMITED} constant to avoid timing out.</p>
     *
     * <p>The supplied timeout value must be positive or one of the constants
     * described above.</p>
     *
     * @return the time to wait to obtain a bean instance for the duration of
     * a single method call.
     */
    long accessTimeout() default 5;

    /**
     * {@return the {@link TimeUnit} to use for the {@link #accessTimeout()}}
     */
    TimeUnit unit() default SECONDS;

    /**
     * The types of {@link Throwable Throwables} which must cause the destruction of a pooled bean instance.
     * <p>
     * If the pooled bean instances throws any Throwable which is an instance of a type set in the destroyOn property,
     * then the bean will be destroyed, except if the throwable is also an instance of a type set in the {@link
     * #keepOn()} property.
     * </p>
     *
     * @return The types of {@link Throwable Throwables} which must cause the destruction of a pooled bean instance
     */
    Class<? extends Throwable>[] destroyOn() default {};

    /**
     * The types of {@link Throwable Throwables} which must not cause the destruction of a pooled bean instance.
     * <p>
     * If the {@link #destroyOn()} property is empty, but the keepOn property is not, then the bean will be
     * destroyed for any Throwable that isn't an instance of a type in the keepOn property.
     * When the {@link #destroyOn()} property is not empty, then the keepOn property takes precedence.
     * If a pooled bean instance throws a throwable which is an instance of a type in both the
     * destroyOn and keepOn properties, then the bean will not be destroyed.
     * </p>
     *
     * @return The types of {@link Throwable Throwables} which must not cause the destruction of a pooled bean
     * instance.
     */
    Class<? extends Throwable>[] keepOn() default {};

    /**
     * Supports inline instantiation of the Pooled annotation.
     *
     * @since 3.2
     */
    public static final class Literal extends AnnotationLiteral<Pooled> implements Pooled {

        private static final long serialVersionUID = 1L;

        private final int        value;
        private final long       accessTimeout;
        private final TimeUnit   unit;
        private final Class<? extends Throwable>[] destroyOn;
        private final Class<? extends Throwable>[] keepOn;

        /**
         * Default instance of the {@link Pooled} annotation.
         */
        @SuppressWarnings("unchecked")
        public static final Literal INSTANCE = of(
            10,
            5,
            MINUTES,
            new Class[]{},
            new Class[]{}
        );

        /**
         * Instance of the {@link Pooled} annotation.
         *
         * @param value The maximum number of instances in the pool.
         * @param accessTimeout The maximum amount of time to wait attempting to retrieve an instance from the pool.
         * @param unit The {@link TimeUnit} to use for the {@link #accessTimeout()}
         * @param destroyOn The types of {@link Throwable Throwables} which must cause the destruction of a pooled bean instance.
         * @param keepOn The types of {@link Throwable Throwables} which must not cause the destruction of a pooled bean instance.

         * @return instance of the {@link Pooled} annotation
         */
        public static Literal of(
                        final int                             value,
                        final long                            accessTimeout,
                        final TimeUnit                        unit,
                        final Class<? extends Throwable>[]    destroyOn,
                        final Class<? extends Throwable>[]    keepOn


            ) {
            return new Literal(
                                            value,
                                            accessTimeout,
                                            unit,
                                            destroyOn,
                                            keepOn
                );
        }

        private Literal(
                        final int           value,
                        final long          accessTimeout,
                        final TimeUnit      unit,
                        final Class<? extends Throwable>[]    destroyOn,
                        final Class<? extends Throwable>[]    keepOn
                ) {

            this.value =                    value;
            this.accessTimeout =            accessTimeout;
            this.unit =                     unit;
            this.destroyOn =                destroyOn;
            this.keepOn =                   keepOn;
        }


        /**
         * {@return the maximum number of instances in the pool.}
         */
        @Override
        public int value() {
            return value;
        }

        /**
         * {@return the maximum amount of time to wait attempting to retrieve an instance from the pool.}
         */
        @Override
        public long accessTimeout() {
            return accessTimeout;
        }

        /**
         * {@return the {@link TimeUnit} to use for the {@link #accessTimeout()}}
         */
        @Override
        public TimeUnit unit() {
            return unit;
        }

        /**
         * {@return the types of {@link Throwable Throwables} which must cause the destruction of a pooled bean instance.}
         */
        @Override
        public Class<? extends Throwable>[] destroyOn() {
            return destroyOn;
        }

        /**
         * {@return the types of {@link Throwable Throwables} which must not cause the destruction of a pooled bean instance.}
         */
        @Override
        public Class<? extends Throwable>[] keepOn() {
            return keepOn;
        }

    }
}
