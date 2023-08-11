package ee.jakarta.tck.concurrent.framework.junit.extensions;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.concurrent.Future;

import ee.jakarta.tck.concurrent.common.managed.task.listener.ListenerEvent;
import ee.jakarta.tck.concurrent.common.managed.task.listener.ManagedTaskListenerImpl;
import ee.jakarta.tck.concurrent.common.transaction.CancelledTransactedTask;
import ee.jakarta.tck.concurrent.framework.TestConstants;

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
     * Waits for future to complete, but will timeout after
     * {@link TestConstants#waitTimeout}, and will be polled every
     * {@link TestConstants#pollInterval}
     *
     * The difference between this method and waitForTaskComplete is that some
     * scheduled task will return values for multiple times, in this situation
     * waitForTaskComplete does not work.
     *
     * @param future - the future to wait for
     */
    public static void waitTillFutureIsDone(final Future<?> future) {
        assertTimeoutPreemptively(TestConstants.waitTimeout, () -> {
            for (; !future.isDone(); sleep(TestConstants.pollInterval)) {
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

    public static void waitForTransactionBegan(final CancelledTransactedTask task) {
        assertTimeoutPreemptively(TestConstants.waitTimeout, () -> {
            for (; !task.getBeginTransaction().get(); sleep(TestConstants.pollInterval)) {
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
