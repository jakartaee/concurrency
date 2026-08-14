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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.TestLogger;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import jakarta.enterprise.concurrent.LockTimeoutException;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/LockServlet")
public class LockServlet extends TestServlet {
    private static final TestLogger log = TestLogger.get(LockServlet.class);
    private static final long serialVersionUID = 1L;
    private static final long TIMEOUT_S = TestConstants.waitTimeout.toSeconds();

    @Inject
    private LockBean lockBean;

    @Inject
    private LockMethodsBean lockMethodsBean;

    @Inject
    private ManagedThreadFactory threadFactory;

    /**
     * Perform 2 invocations of a bean method that is annotated both
     * Asynchronous and Lock. Verify that the invocations do not overlap,
     * but both run successfully.
     */
    public void testLockAsyncMethod() throws Exception {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch1 = new CountDownLatch(1);
        CountDownLatch finishLatch2 = new CountDownLatch(1);

        CompletableFuture<Integer> f1;
        CompletableFuture<Integer> f2;
        f1 = lockMethodsBean.asyncLockMethod(startLatch, finishLatch1);
        f2 = lockMethodsBean.asyncLockMethod(startLatch, finishLatch2);

        assertEquals(true,
                     startLatch.await(TIMEOUT_S, TimeUnit.SECONDS));

        // encourage possible overlap
        TimeUnit.SECONDS.sleep(2);

        finishLatch1.countDown();
        finishLatch2.countDown();

        // both must indicate that only 1 execution was running
        assertEquals(Integer.valueOf(1),
                     f1.get(TIMEOUT_S, TimeUnit.SECONDS));
        assertEquals(Integer.valueOf(1),
                     f2.get(TIMEOUT_S, TimeUnit.SECONDS));
    }

    /**
     * Obtain READ lock on a bean from another thread.
     * While the other thread is still running, the current thread must
     * be able to run another READ lock method of the same bean.
     */
    public void testMultipleThreadsShareReadLock() throws Exception {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(1);
        CompletableFuture<String> bgFuture = new CompletableFuture<>();

        Thread bgThread = threadFactory.newThread(() -> {
            try {
                bgFuture.complete(lockBean.readLockMethodHold(startLatch,
                                                              finishLatch));
            } catch (Throwable t) {
                bgFuture.completeExceptionally(t);
            }
        });
        bgThread.start();

        assertEquals(true,
                     startLatch.await(TIMEOUT_S, TimeUnit.SECONDS));

        String currentThreadResult = lockBean.readLockMethodShortTimeout();
        assertEquals("READ_SHORT_SUCCESS",
                     currentThreadResult);

        finishLatch.countDown();

        assertEquals("READ_HOLD_SUCCESS",
                     bgFuture.get(TIMEOUT_S, TimeUnit.SECONDS));
    }

    /**
     * Obtain WRITE lock on a bean from another thread.
     * While the other thread is still running, the current thread must
     * be able to run a Lock(type=WRITE) method of a different bean.
     */
    public void testMultipleThreadsWithWriteLocksOnDifferentBeans()
            throws Exception {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(1);
        CompletableFuture<String> bgFuture = new CompletableFuture<>();

        Thread bgThread = threadFactory.newThread(() -> {
            try {
                bgFuture.complete(lockBean.writeLockMethodHold(startLatch,
                                                               finishLatch));
            } catch (Throwable t) {
                bgFuture.completeExceptionally(t);
            }
        });
        bgThread.start();

        assertEquals(true,
                     startLatch.await(TIMEOUT_S, TimeUnit.SECONDS));

        // lockBean has a WRITE lock held, but lockMethodsBean is a separate
        // bean instance with its own lock, so this must succeed immediately
        assertEquals("WRITE_SUCCESS",
                     lockMethodsBean.writeLockMethodInstant());

        finishLatch.countDown();

        assertEquals("WRITE_HOLD_SUCCESS",
                     bgFuture.get(TIMEOUT_S, TimeUnit.SECONDS));
    }

    /**
     * Obtain WRITE lock on a bean from another thread.
     * While the other thread is still running, the current thread must
     * be able to run a method of the same bean that has no Lock annotation
     * given that the bean class also has no Lock annotation.
     */
    public void testNoLock() throws Exception {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(1);
        CompletableFuture<String> bgFuture = new CompletableFuture<>();

        Thread bgThread = threadFactory.newThread(() -> {
            try {
                bgFuture.complete(lockMethodsBean.writeLockMethodHold(startLatch,
                                                                      finishLatch));
            } catch (Throwable t) {
                bgFuture.completeExceptionally(t);
            }
        });
        bgThread.start();

        assertEquals(true,
                     startLatch.await(TIMEOUT_S, TimeUnit.SECONDS));

        assertEquals("NO_LOCK_SUCCESS",
                     lockMethodsBean.nonLockedMethod());

        finishLatch.countDown();

        assertEquals("WRITE_HOLD_SUCCESS",
                     bgFuture.get(TIMEOUT_S, TimeUnit.SECONDS));
    }

    /**
     * Obtain READ lock on a bean and from within the same bean method
     * attempt a Lock(type=WRITE) method of the same bean.
     * IllegalStateException must be raised to the caller.
     */
    public void testReadLockDoesNotUpgradeToWriteLock() throws Exception {
        Class<?> exceptionClass = null;
        try {
            lockMethodsBean.readLockMethodWithWriteCall();
        } catch (IllegalStateException e) {
            exceptionClass = IllegalStateException.class;
        } catch (Throwable t) {
            if (t.getCause() instanceof IllegalStateException) {
                exceptionClass = IllegalStateException.class;
            } else {
                exceptionClass = t.getClass();
            }
        }
        assertEquals(IllegalStateException.class,
                     exceptionClass);
    }

    /**
     * Obtain READ lock on a bean and from within the same bean method
     * invoke another Lock(type=READ) method of the same bean.
     */
    public void testReadLockReentryFromSameThread() throws Exception {
        assertEquals("READ_SUCCESS",
                     lockMethodsBean.readLockMethodReentrant());
    }

    /**
     * Obtain WRITE lock on a bean from another thread.
     * While the other thread is still running, the current thread
     * attempts a READ lock on the bean and must time out.
     */
    public void testWriteLockBlocksOtherThreadFromRead() throws Exception {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(1);
        CompletableFuture<String> bgFuture = new CompletableFuture<>();

        Thread bgThread = threadFactory.newThread(() -> {
            try {
                bgFuture.complete(lockBean.writeLockMethodHold(startLatch,
                                                               finishLatch));
            } catch (Throwable t) {
                bgFuture.completeExceptionally(t);
            }
        });
        bgThread.start();

        assertEquals(true,
                     startLatch.await(TIMEOUT_S, TimeUnit.SECONDS));

        try {
            lockBean.readLockMethodShortTimeout();
            fail("Should not be able to Lock the bean in READ mode"
               + " while another has a WRITE mode Lock on the bean");
        } catch (LockTimeoutException e) {
            // pass
        }

        finishLatch.countDown();

        assertEquals("WRITE_HOLD_SUCCESS",
                     bgFuture.get(TIMEOUT_S, TimeUnit.SECONDS));
    }

    /**
     * Obtain a WRITE lock on a bean from another thread.
     * While the other thread is still running, the current thread
     * attempts a WRITE lock on the bean and must time out.
     */
    public void testWriteLockBlocksOtherThreadFromWrite() throws Exception {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(1);
        CompletableFuture<String> bgFuture = new CompletableFuture<>();

        Thread bgThread = threadFactory.newThread(() -> {
            try {
                bgFuture.complete(lockBean.writeLockMethodHold(startLatch,
                                                               finishLatch));
            } catch (Throwable t) {
                bgFuture.completeExceptionally(t);
            }
        });
        bgThread.start();

        assertEquals(true,
                     startLatch.await(TIMEOUT_S, TimeUnit.SECONDS));

        try {
            lockBean.writeLockMethodShortTimeout();
            fail("Should not be able to Lock the bean in WRITE mode"
               + " while another has a WRITE mode Lock on the bean");
        } catch (LockTimeoutException e) {
            // pass
        }

        finishLatch.countDown();

        assertEquals("WRITE_HOLD_SUCCESS",
                     bgFuture.get(TIMEOUT_S, TimeUnit.SECONDS));
    }

    /**
     * Obtain WRITE lock on a bean and from within the same bean method
     * invoke another Lock(type=WRITE) method of the same bean.
     */
    public void testWriteLockReentryFromSameThread() throws Exception {
        assertEquals("WRITE_SUCCESS",
                     lockMethodsBean.writeLockMethodReentrant());
    }

    /**
     * Obtain WRITE lock on a bean and from within the same bean method
     * invoke another Lock(type=READ) method of the same bean.
     */
    public void testWriteLockToReadLockReentryFromSameThread() throws Exception {
        assertEquals("READ_SUCCESS",
                     lockMethodsBean.writeLockToReadMethodReentrant());
    }
}
