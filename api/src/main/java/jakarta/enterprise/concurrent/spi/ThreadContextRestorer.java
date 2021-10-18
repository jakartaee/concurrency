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
 * Restores the prior context on a thread after a contextual task or action completes.
 */
@FunctionalInterface
public interface ThreadContextRestorer {
    /**
     * Invoked by the Jakarta EE Product Provider to remove the thread context that
     * the {@link ThreadContextSnapshot} began on this thread and restore the previous
     * context that was on the thread prior to that point. The Jakarta EE Product Provider
     * must invoke the {@link #endContext endContext} method exactly
     * once for each {@code ThreadContextRestorer} instance that it obtains
     * and on the same thread from which the Jakarta EE Product Provider obtained it
     * by invoking {@link ThreadContextSnapshot#begin ThreadContextSnapshot.begin}.
     * <p>
     * Typically, patterns such as the following will be observed:
     * <pre>
     * restorerA1 = contextA_snapshot1.begin();
     * restorerB1 = contextB_snapshot1.begin();
     * restorerC1 = contextC_snapshot1.begin();
     * ...
     * restorerC1.endContext();
     * restorerB1.endContext();
     * restorerA1.endContext();
     * </pre>
     * However, more advanced sequences such as the following are also valid:
     * <pre>
     * restorerA1 = contextA_snapshot1.begin();
     * restorerB1 = contextB_snapshot1.begin();
     * ...
     * restorerC1 = contextC_snapshot1.begin();
     * ...
     * restorerC1.endContext();
     * ...
     * restorerB2 = contextB_snapshot2.begin();
     * restorerC2 = contextC_snapshot2.begin();
     * ...
     * restorerC2.endContext();
     * restorerB2.endContext();
     * ...
     * restorerB1.endContext();
     * restorerA1.endContext();
     * </pre>
     *
     * @throws IllegalStateException if invoked more than once on the same instance.
     *
     * @since 3.0
     */
    public void endContext() throws IllegalStateException;
}
