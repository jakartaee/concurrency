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

/**
 * This exception indicates that an attempt to concurrently access a {@link Pooled}
 * bean method resulted in a timeout.
 *
 * @since 3.2
 */
public class PoolLockTimeoutException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
     * <p>Constructor for PoolLockTimeoutException.</p>
     */
    public PoolLockTimeoutException() {
	}

    /**
     * <p>Constructor for PoolLockTimeoutException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
	public PoolLockTimeoutException(String message) {
		super(message);
	}
}