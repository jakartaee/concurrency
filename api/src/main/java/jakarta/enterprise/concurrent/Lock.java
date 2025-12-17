/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * <p>A lock that governs concurrent access to a CDI managed bean.</p>
 *
 * <p>When the {@code Lock} annotation is applied to a bean class or method,
 * a single lock controls access to an instance of the bean. The lock is obtained,
 * either in {@linkplain Type#WRITE exclusive} or {@linkplain Type#READ shared}
 * mode, by invoking bean methods. If the bean implementation internally accesses
 * one of its own methods or fields, that access is not subject to the lock.</p>
 *
 * <p>A bean method that does not have a {@code Lock} annotation inherits
 * the {@code Lock} annotation, if present, of the class that declares the method.
 * A bean class that has a method annotated {@code Lock}, but does not have a
 * {@code Lock} annotation of its own at the class level, operates according to
 * the default values of the {@code Lock} annotation when bean methods that are
 * not annotated {@code Lock} are invoked.</p>
 *
 * @since 3.2
 */
@InterceptorBinding
@Target({ METHOD, TYPE })
@Retention(RUNTIME)
@Inherited
public @interface Lock {

    /**
     * Designates the type of lock to be READ or WRITE.
     */
    enum Type {
        /**
         * For read-only operations. Allows simultaneous access, as long as no
         * <code>WRITE</code> lock is held.
         */
        READ,

        /**
         * For exclusive access to the bean instance. A <code>WRITE</code> lock can only be acquired when no other method with
         * either a <code>READ</code> or <code>WRITE</code> lock is currently held.
         */
        WRITE
    }

    /**
     * Value for {@link #accessTimeout} that indicates that the lock that
     * permits running a bean method must be obtained immediately.
     * Otherwise, a {@link java.util.concurrent.CompletionException} that
     * chains a {@link java.util.concurrent.TimeoutException} is thrown.
     */
    long IMMEDIATE = 0;

    /**
     * Value for {@link #accessTimeout} that indicates to wait as long as necessary
     * to obtain the lock that permits running a bean method.
     */
    long UNLIMITED = -1;

    /**
     * <p>Indicates whether to obtain the lock on the bean instance in
     * {@linkplain Type#WRITE exclusive} or {@linkplain Type#READ shared} mode.
     * </p>
     * @return The Lock.Type to use
     */
    @Nonbinding Type type() default Type.WRITE;

    /**
     * <p>The maximum amount of time to wait to obtain the lock.
     * If the lock that allows running the method cannot be obtained within
     * the specified amount of time, a {@link java.util.concurrent.CompletionException}
     * that chains a {@link java.util.concurrent.TimeoutException} is thrown
     * from the method invocation attempt.</p>
     *
     * <p>Use the {@link #IMMEDIATE} constant to avoid waiting.</p>
     *
     * <p>Use the {@link #UNLIMITED} constant to avoid timing out.</p>
     *
     * <p>The supplied timeout value must be positive or one of the constants
     * described above.</p>
     *
     * @return the time to wait to obtain a lock
     */
    @Nonbinding long accessTimeout() default 60;

    /**
     *
     * @return units used for the specified accessTimeout value.
     */
    @Nonbinding TimeUnit unit() default SECONDS;


    /**
     * Supports inline instantiation of the {@link Lock} annotation.
     *
     */
    public static final class Literal extends AnnotationLiteral<Lock> implements Lock {

        private static final long serialVersionUID = 1L;

        /**
         * Instance of the Literal annotation.
         */
        public static final Literal INSTANCE = of(Type.WRITE, 60, SECONDS);

        private final Type type;
        private final long accessTimeout;
        private final TimeUnit unit;

        public static Literal of(final Type type, final long accessTimeout, final TimeUnit unit) {
            return new Literal(type, accessTimeout, unit);
        }

        private Literal(final Type type, final long accessTimeout, final TimeUnit unit) {
            this.type = type;
            this.accessTimeout = accessTimeout;
            this.unit = unit;
        }

        /**
         * {@return the Lock.Type to use.}
         */
        @Override
        public Type type() {
            return type;
        }

        /**
         * {@return the time to wait to obtain a lock.}
         */
        @Override
        public long accessTimeout() {
            return accessTimeout;
        }

        /**
         * {@return units used for the specified accessTimeout value.}
         */
        @Override
        public TimeUnit unit() {
            return unit;
        }
    }

}
