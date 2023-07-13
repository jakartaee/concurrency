/*
 * Copyright (c) 2013, 2022 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.concurrent.common.managed.task.listener;

import java.time.Duration;

import ee.jakarta.tck.concurrent.framework.junit.extensions.Wait;

public class RunnableTaskWithStatus implements Runnable {

    private final ManagedTaskListenerImpl listener;

    private final Duration blockTime;

    public RunnableTaskWithStatus(final ManagedTaskListenerImpl listener) {
        this.listener = listener;
        blockTime = Duration.ZERO;
    }

    public RunnableTaskWithStatus(final ManagedTaskListenerImpl listener, final Duration blockTime) {
        this.listener = listener;
        this.blockTime = blockTime;
    }

    public void run() {
        listener.update(ListenerEvent.TASK_RUN);
        if (!blockTime.isZero()) {
            Wait.sleep(blockTime);
        }
    }
    
    public ManagedTaskListenerImpl getListener() {
        return listener;
    }

}
