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
package ee.jakarta.tck.concurrent.api.Lock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ee.jakarta.tck.concurrent.framework.TestConstants;
import jakarta.enterprise.concurrent.Lock;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * A CDI managed bean annotated Lock applies the Lock annotations to all
 * methods that are not themselves annotated Lock.
 */
@ApplicationScoped
@Lock(type = Lock.Type.READ, accessTimeout = 500, unit = TimeUnit.MILLISECONDS)
public class LockBean {
    private static final long TIMEOUT_S = TestConstants.waitTimeout.toSeconds();

    public String readLockMethodHold(final CountDownLatch startLatch,
                                     final CountDownLatch finishLatch)
            throws InterruptedException {
        startLatch.countDown();
        if (finishLatch.await(TIMEOUT_S, TimeUnit.SECONDS)) {
            return "READ_HOLD_SUCCESS";
        } else {
            return "READ_HOLD_TIMED_OUT";
        }
    }

    public String readLockMethodShortTimeout() {
        return "READ_SHORT_SUCCESS";
    }

    // Overrides the Lock annotation on the class
    @Lock(type = Lock.Type.WRITE)
    public String writeLockMethodHold(final CountDownLatch startLatch,
                                      final CountDownLatch finishLatch)
            throws InterruptedException {
        startLatch.countDown();
        if (finishLatch.await(TIMEOUT_S, TimeUnit.SECONDS)) {
            return "WRITE_HOLD_SUCCESS";
        } else {
            return "WRITE_HOLD_TIMED_OUT";
        }
    }

    // Overrides the Lock annotation on the class
    @Lock(type = Lock.Type.WRITE, accessTimeout = 1, unit = TimeUnit.SECONDS)
    public String writeLockMethodShortTimeout() {
        return "WRITE_SHORT_SUCCESS";
    }
}
