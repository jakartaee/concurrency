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

package jakarta.enterprise.concurrent;

import jakarta.enterprise.concurrent.spi.ThreadContextRestorer;
import jakarta.enterprise.concurrent.spi.ThreadContextSnapshot;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ThreadContextSnapshot and ThreadContexRestorer example from the specification.
 */
public class ThreadPrioritySnapshot implements ThreadContextSnapshot, Serializable {
    final int priority;

    ThreadPrioritySnapshot(int priority) {
        this.priority = priority;
    }

    public ThreadContextRestorer begin() {
        Thread thread = Thread.currentThread();
        int priorityToRestore = thread.getPriority();
        AtomicBoolean restored = new AtomicBoolean();

        ThreadContextRestorer contextRestorer = () -> {
            if (restored.compareAndSet(false, true))
                thread.setPriority(priorityToRestore);
            else
                throw new IllegalStateException();
        };

        thread.setPriority(priority);

        return contextRestorer;
    }
}