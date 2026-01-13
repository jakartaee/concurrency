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
 * @since 3.2
 */
@Inherited
@NormalScope
@Documented
@Target({TYPE, METHOD})
@Retention(RUNTIME)
public @interface Pooled {

    /**
     * {@return the maximum number of instances in the pool.}
     */
    int value() default 10;

    /**
     * {@return the maximum amount of time to attempt to obtain a lock on an instance from the pool.}
     */
    long accessTimeout() default 5;

    /**
     * {@return the {@link TimeUnit} to use for the {@link #instanceLockTimeout()}}
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
     * If the {@link #destroyOn()} property is empty, but the dontDestroyOn property is not, then the bean will be
     * destroyed for any Throwable that isn't an instance of a type in the dontDestroyOn property. When the {@link #destroyOn()} property is not empty, then the dontDestroyOn property takes precedence. If a pooled bean instance throws a throwable which is an instance of a type in both the destroyOn and dontDestroyOn properties, then the bean will not be destroyed.
     * </p>
     *
     * @return The types of {@link Throwable Throwables} which must not cause the destruction of a pooled bean instance.
     */
    Class<? extends Throwable>[] keepOn() default {};

    /**
     * Supports inline instantiation of the Pooled annotation.
     *
     * @since 3.0
     */
    public static final class Literal extends AnnotationLiteral<Pooled> implements Pooled {

        private static final long serialVersionUID = 1L;

        private final int        maxNumberOfInstances;
        private final long       instanceLockTimeout;
        private final TimeUnit   instanceLockTimeoutUnit;
        private final Class<?>[] destroyOn;
        private final Class<?>[] dontDestroyOn;

        /**
         * Default instance of the {@link Pooled} annotation.
         */
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
         * @param maxNumberOfInstances The maximum number of instances in the pool.
         * @param instanceLockTimeout The maximum amount of time to attempt to obtain a lock on an instance from the pool.
         * @param instanceLockTimeoutUnit The {@link TimeUnit} to use for the {@link #instanceLockTimeout()}
         * @param destroyOn The types of {@link Throwable Throwables} which must cause the destruction of a pooled bean instance.
         * @param dontDestroyOn The types of {@link Throwable Throwables} which must not cause the destruction of a pooled bean instance.

         * @return instance of the {@link Pooled} annotation
         */
        public static Literal of(
                        final int           maxNumberOfInstances,
                        final long          instanceLockTimeout,
                        final TimeUnit      instanceLockTimeoutUnit,
                        final Class<?>[]    destroyOn,
                        final Class<?>[]    dontDestroyOn


            ) {
            return new Literal(
                                            maxNumberOfInstances,
                                            instanceLockTimeout,
                                            instanceLockTimeoutUnit,
                                            destroyOn,
                                            dontDestroyOn
                );
        }

        private Literal(
                        final int           maxNumberOfInstances,
                        final long          instanceLockTimeout,
                        final TimeUnit      instanceLockTimeoutUnit,
                        final Class<?>[]    destroyOn,
                        final Class<?>[]    dontDestroyOn
                ) {

            this.maxNumberOfInstances =     maxNumberOfInstances;
            this.instanceLockTimeout =      instanceLockTimeout;
            this.instanceLockTimeoutUnit =  instanceLockTimeoutUnit;
            this.destroyOn =                destroyOn;
            this.dontDestroyOn =            dontDestroyOn;
        }


        /**
         * {@return the maximum number of instances in the pool.}
         */
        @Override
        public int maxNumberOfInstances() {
            return maxNumberOfInstances;
        }

        /**
         * {@return the maximum amount of time to attempt to obtain a lock on an instance from the pool.}
         */
        @Override
        public long instanceLockTimeout() {
            return instanceLockTimeout;
        }

        /**
         * {@return the {@link TimeUnit} to use for the {@link #instanceLockTimeout()}}
         */
        @Override
        public TimeUnit instanceLockTimeoutUnit() {
            return instanceLockTimeoutUnit;
        }

        /**
         * {@return the types of {@link Throwable Throwables} which must cause the destruction of a pooled bean instance.}
         */
        @Override
        public Class<?>[] destroyOn() {
            return destroyOn;
        }

        /**
         * {@return the types of {@link Throwable Throwables} which must not cause the destruction of a pooled bean instance.}
         */
        @Override
        public Class<?>[] dontDestroyOn() {
            return dontDestroyOn;
        }

    }
}
