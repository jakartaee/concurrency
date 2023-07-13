/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
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
package ee.jakarta.tck.concurrent.spec.ManagedExecutorService.resourcedef;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ee.jakarta.tck.concurrent.common.context.IntContext;
import ee.jakarta.tck.concurrent.common.context.StringContext;
import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AppBean {
    private static final long MAX_WAIT_SECONDS = TimeUnit.MINUTES.toSeconds(2);

    @Asynchronous(executor = "java:module/concurrent/ExecutorB")
    public CompletionStage<String> addStringContextAndWait(final BlockingQueue<String> queue, final CountDownLatch blocker) {
        String s = StringContext.get();
        try {
            queue.add(s);
            blocker.await(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            return Asynchronous.Result.complete(s);
        } catch (Exception x) {
            throw new CompletionException(x);
        }
    }

    @Asynchronous
    public void exchange(final Exchanger<String> exchanger, final String value) {
        try {
            exchanger.exchange(value, MAX_WAIT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException x) {
            throw new CompletionException(x);
        }
    }

    @Asynchronous(executor = "java:app/concurrent/ExecutorA")
    public CompletableFuture<Integer> waitAndGetIntContext(final Semaphore started, final CountDownLatch blocker) {
        started.release(1);
        CompletableFuture<Integer> future = Asynchronous.Result.getFuture();
        try {
            while (!future.isDone() && !blocker.await(300, TimeUnit.MILLISECONDS)) {
                System.out.println(
                        Thread.currentThread().getName() + ": waitAndGetIntContext awaiting signal from caller");
            }
            future.complete(IntContext.get());
        } catch (Exception x) {
            future.completeExceptionally(x);
        }
        return future;
    }
}
