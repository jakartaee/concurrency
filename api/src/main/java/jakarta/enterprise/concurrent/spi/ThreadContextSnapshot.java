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
package jakarta.enterprise.concurrent.spi;

/**
 * An immutable snapshot of a particular type of thread context.
 * <p>
 * The captured context represented by this snapshot can be applied to
 * any number of threads, including concurrently.
 * <p>
 * Any state that is associated with context applied to a thread should
 * be kept, not within the snapshot, but within the distinct
 * {@link ThreadContextRestorer} instance that this
 * {@code ThreadContextSnapshot} creates each time it is applied
 * to a thread.
 *
 * @since 3.0
 */
@FunctionalInterface
public interface ThreadContextSnapshot {
    /**
     * Applies the captured thread context snapshot to the current thread and
     * returns a distinct {@link ThreadContextRestorer} instance. The
     * {@code ThreadContextRestorer} instance tracks the context's life cycle,
     * including any state that is associated with it or that is necessary for
     * restoring the previous context.
     * <p>
     * For each invocation of this method, the Jakarta EE Product Provider
     * must invoke the corresponding
     * {@link ThreadContextRestorer#endContext endContext}
     * method exactly once, such that the previous context is restored
     * on the thread. If the Jakarta EE Product Provider sequentially begins
     * multiple {@code ThreadContextRestorer} instances on a thread,
     * it must invoke the corresponding {@code endContext} methods in reverse
     * order.
     *
     * @return restorer instance that reverts the state of this context type
     *         on the thread to what it was prior to applying this snapshot.
     */
    public ThreadContextRestorer begin();
}
