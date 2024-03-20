/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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
package ee.jakarta.tck.concurrent.framework.junit.extensions;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.concurrent.Future;

import ee.jakarta.tck.concurrent.common.managed.task.listener.ListenerEvent;
import ee.jakarta.tck.concurrent.common.managed.task.listener.ManagedTaskListenerImpl;
import ee.jakarta.tck.concurrent.common.transaction.CancelledTransactedTask;
import ee.jakarta.tck.concurrent.framework.TestConstants;
import java.util.function.IntSupplier;

/**
 * Utility class for waiting for results.
 *
 * Prioritize polling for results, and discourages sleeping
 */
public final class Wait {

    private Wait() {
        // Utility class no constructor
    }

    /**
     * Waits for task to complete, but will timeout after
     * {@link TestConstants#waitTimeout}
     *
     * @param future to wait for
     * @return result
     */
    public static <T> T waitForTaskComplete(final Future<T> future) {
        return waitForTaskComplete(future, TestConstants.waitTimeout);
    }

    /**
     * Waits for task to complete, but will timeout after specified timeout
     *
     * @param future  - the future to wait for
     * @param timeout - the duration of timeout
     * @return result - result returned from future, or null if timeout was exceeded
     */
    public static <T> T waitForTaskComplete(final Future<T> future, final Duration timeout) {
        return assertTimeoutPreemptively(timeout, () -> future.get());
    }

    /**
     * Wait for listener to complete, but will timeout after
     * {@link TestConstants#waitTimeout}, and will be polled ever
     * {@link TestConstants#pollInterval}
     *
     * @param managedTaskListener - the listener to be polled
     */
    public static void waitForListenerComplete(final ManagedTaskListenerImpl managedTaskListener) {
        waitForListenerComplete(managedTaskListener, TestConstants.waitTimeout, TestConstants.pollInterval);
    }

    /**
     * Wait for listener to complete, but will timeout after a specified timeout,
     * and will be polled ever specified interval
     *
     * @param managedTaskListener - the listener to be polled
     * @param maxWaitTimeMillis   - timeout
     * @param pollIntervalMillis  - poll interval
     */
    public static void waitForListenerComplete(final ManagedTaskListenerImpl managedTaskListener, final Duration timeout,
            final Duration pollInterval) {
        assertTimeoutPreemptively(timeout, () -> {
            for (; !managedTaskListener.eventCalled(ListenerEvent.DONE); sleep(TestConstants.pollInterval)) {
                //empty
            }
        });
    }

    /**
     * Waits for future to throw an error, but will timeout after
     * {@link TestConstants#waitTimeout}, and will be polled every
     * {@link TestConstants#pollInterval}
     *
     * @param future - the future to wait for
     */
    public static <T extends Throwable> void waitTillFutureThrowsException(final Future<?> future,
            final Class<T> expected) {
        assertThrows(expected, () -> {
            assertTimeoutPreemptively(TestConstants.waitTimeout, () -> {
                for (;; sleep(TestConstants.pollInterval)) {
                    future.get();
                }
            });
        });
    }

    /**
     * Calls future.cancel(true), and then waits for future.done() to return true,
     * but will timeout after {@link TestConstants#waitTimeout}, and will be polled every
     * {@link TestConstants#pollInterval}
     *
     * @param future - the future to wait for
     */
    public static void waitCancelFuture(final Future<?> future) {
        assertTimeoutPreemptively(TestConstants.waitTimeout, () -> {
            for (future.cancel(true); !future.isDone(); sleep(TestConstants.pollInterval)) {
                //empty
            }
        });
    }

    /**
     * Waits until thread is finished, but will timeout after
     * {@link TestConstants#waitTimeout}, and will be polled every
     * {@link TestConstants#pollInterval}
     *
     * @param thread - the thread to wait for
     */
    public static void waitTillThreadFinish(final Thread thread) {
        assertTimeoutPreemptively(TestConstants.waitTimeout, () -> {
            for (; thread.isAlive(); sleep(TestConstants.pollInterval)) {
                //empty
            }
        });
    }

    /**
     * Waits for task to report the transaction has begun, but will timeout after
     * {@link TestConstants#waitTimeout}, and will be polled every
     * {@link TestConstants#pollInterval}
     *
     * @param task - the task to wait for
     */
    public static void waitForTransactionBegan(final CancelledTransactedTask task) {
        assertTimeoutPreemptively(TestConstants.waitTimeout, () -> {
            for (; !task.getBeginTransaction().get(); sleep(TestConstants.pollInterval)) {
                //empty
            }
        });
    }
    
    /**
     * Waits for a counter to report an expected value, but will timeout after
     * {@link TestConstants#waitTimeout}, and will be polled every
     * {@link TestConstants#pollInterval}
     *
     * @param counter
     * @param expected
     */
    public static void waitForCounter(final IntSupplier counter, final int expected) {
        assertTimeoutPreemptively(TestConstants.waitTimeout, () -> {
            for (; expected != counter.getAsInt(); Wait.sleep(TestConstants.pollInterval)) {
                //empty
            }
        });
    }

    public static void sleep(final Duration time) {
        try {
            Thread.sleep(time.toMillis());
        } catch (InterruptedException e) {
            throw new AssertionError("Interrupted while sleeping", e);
        }
    }
}
