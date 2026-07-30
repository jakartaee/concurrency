/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

/**
 * Indicates that a {@link Lock} could not be obtained within the allotted
 * {@linkplain Lock#accessTimeout access timeout}.
 *
 * @since 3.2
 */
public class LockTimeoutException extends RuntimeException {
    private static final long serialVersionUID = -9021472677315027139L;

    /**
     * Constructs a {@code LockTimeoutException} without providing a detail
     * message or cause.
     */
    public LockTimeoutException() {
        super();
    }

    /**
     * Constructs a {@code LockTimeoutException} with the given detail message.
     *
     * @param message the {@linkplain Throwable#getMessage() detail message}
     */
    public LockTimeoutException(final String message) {
        super(message);
    }

    /**
     * Constructs a {@code LockTimeoutException} with the given detail message
     * and cause.
     *
     * @param message the {@linkplain Throwable#getMessage() detail message}
     * @param cause   the {@linkplain Throwable#getCause() cause}. A
     *                {@code null} value indicates an unknown or nonexistent
     *                cause
     */
    public LockTimeoutException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a {@code LockTimeoutException} with the given cause. The
     * detail message is obtained from {@code cause.toString()} or, if
     * the cause is {@null}, remains {@null}.
     *
     * @param cause the {@linkplain Throwable#getCause() cause}. A {@code null}
     *              value indicates an unknown or nonexistent cause
     */
    public LockTimeoutException(final Throwable cause) {
        super(cause);
    }
}
