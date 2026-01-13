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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Specify that a bean is to be pooled.
 *
 * <p>
 * Pooled beans can have multiple instances that are shared between all threads of an application, but can never be
 * called by more than one thread at the same time. When a method call on a pooled bean is performed, an instance is
 * selected from the pool and is locked until the method call ends. When performing multiple method calls directly after
 * each other, multiple different beans may be used.
 * </p>
 *
 * <p>
 * Example of usage
 *
 * {@snippet lang="java" :
 *     @Pooled
 *     public class SomeBean {
 *
 *        public void myMethod() {
 *
 *        }
 *     }
 *
 *     @Inject
 *     SomeBean someBean;
 *
 *     // myMethod call is directed to a free bean from the pool
 *     // for the duration of the method call.
 *     someBean.myMethod();
 *
 *     // myMethod call is again directed to a any free bean from the pool,
 *     // which may be a totally different instance than the bean used
 *     // to service the first call.
 *     someBean.myMethod();
 * }
 *
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
     * Otherwise, a {@link PoolLockTimeoutException} is thrown.
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
     * {@link PoolLockTimeoutException} is thrown.</p>
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
    TimeUnit unit() default MINUTES;

    /**
     * The types of {@link Throwable Throwables} which must cause the destruction of a pooled bean instance.
     * <p>
     * If the pooled bean instances throws any Throwable which is an instance of a type set in the destroyOn property,
     * then the bean will be destroyed, except if the throwable is also an instance of a type set in the {@link
     * #dontDestroyOn()} property.
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
     * When the {@link #destroyOn()} property is not empty, then the dontDestroyOn property takes precedence.
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
     * @since 3.0
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
