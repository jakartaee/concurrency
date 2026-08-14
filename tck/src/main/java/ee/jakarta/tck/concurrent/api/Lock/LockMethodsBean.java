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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ee.jakarta.tck.concurrent.framework.TestConstants;
import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.concurrent.Lock;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class LockMethodsBean {
    private static final long TIMEOUT_S = TestConstants.waitTimeout.toSeconds();

    private final AtomicInteger asyncLockMethodActiveCount = new AtomicInteger(0);

    @Inject
    private LockMethodsBean self;

    @Asynchronous
    @Lock(type = Lock.Type.WRITE, accessTimeout = 10, unit = TimeUnit.SECONDS)
    public CompletableFuture<Integer> asyncLockMethod(
            final CountDownLatch startLatch,
            final CountDownLatch finishLatch) throws InterruptedException {
        asyncLockMethodActiveCount.incrementAndGet();
        startLatch.countDown();
        int numActiveAtFinish;
        try {
            finishLatch.await(TIMEOUT_S, TimeUnit.SECONDS);
        } finally {
            numActiveAtFinish = asyncLockMethodActiveCount.getAndDecrement();
        }
        return Asynchronous.Result.complete(numActiveAtFinish);
    }

    public String nonLockedMethod() {
        return "NO_LOCK_SUCCESS";
    }

    @Lock(type = Lock.Type.READ, accessTimeout = Lock.IMMEDIATE)
    public String readLockMethodInstant() {
        return "READ_SUCCESS";
    }

    @Lock(type = Lock.Type.READ, accessTimeout = 99, unit = TimeUnit.SECONDS)
    public String readLockMethodReentrant() {
        return self.readLockMethodInstant();
    }

    @Lock(type = Lock.Type.READ, accessTimeout = Lock.UNLIMITED)
    public String readLockMethodWithWriteCall() {
        return self.writeLockMethodInstant();
    }

    @Lock(type = Lock.Type.WRITE, accessTimeout = 200) // unit defaults to SECONDS
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

    @Lock(type = Lock.Type.WRITE, accessTimeout = Lock.IMMEDIATE)
    public String writeLockMethodInstant() {
        return "WRITE_SUCCESS";
    }

    @Lock // defaults to WRITE lock with 60 SECOND access timeout
    public String writeLockMethodReentrant() {
        return self.writeLockMethodInstant();
    }

    @Lock(type = Lock.Type.WRITE, accessTimeout = 3, unit = TimeUnit.MINUTES)
    public String writeLockToReadMethodReentrant() {
        return self.readLockMethodInstant();
    }
}
