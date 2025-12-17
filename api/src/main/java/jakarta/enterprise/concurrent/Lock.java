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
 * @since 3.2.0
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
         * For read-only operations. Allows simultaneous access to methods designated as <code>READ</code>, as long as no
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
     * Specifies what type of timeout to use for grabbing a lock.
     */
    enum TimeoutType {
        /**
         * A timeout value in the units specified by the <code>unit</code> element.
         */
        TIMEOUT,

        /**
         * Concurrent access is not permitted; so no wait therefore no timeout is allowed.
         * Either the lock is available and grabbed immediately, or an exception is thrown.
         */
        NOT_PERMITTED,

        /**
         * The client request will block indefinitely until it can proceed; it waits for as long
         * as it needs to, hence the timeout is unlimited.
         */
        INDEFINITTE
    }

    /**
     *
     * @return The Lock.Type to use
     */
    @Nonbinding Type type() default Type.WRITE;

    /**
     *
     * @return the way to deal with time out waiting for a lock
     */
    @Nonbinding TimeoutType timeoutType() default TimeoutType.TIMEOUT;

    /**
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
        public static final Literal INSTANCE = of(Type.WRITE, TimeoutType.TIMEOUT, 60, SECONDS);

        private final Type type;
        private final TimeoutType timeoutType;
        private final long accessTimeout;
        private final TimeUnit unit;

        public static Literal of(final Type type, final TimeoutType timeoutType, final long accessTimeout, final TimeUnit unit) {
            return new Literal(type, timeoutType, accessTimeout, unit);
        }

        private Literal(final Type type, final TimeoutType timeoutType, final long accessTimeout, final TimeUnit unit) {
            this.type = type;
            this.timeoutType = timeoutType;
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
         * {@return the way to deal with time out waiting for a lock.}
         */
        @Override
        public TimeoutType timeoutType() {
            return timeoutType;
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
