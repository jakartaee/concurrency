/*
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
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
package ee.jakarta.tck.concurrent.spec.ManagedScheduledExecutorService.asynchronous;

import ee.jakarta.tck.concurrent.spec.context.IntContext;
import ee.jakarta.tck.concurrent.spec.context.StringContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.context.RequestScoped;

import javax.naming.InitialContext;
import javax.naming.NamingException;

@RequestScoped
public class ReqBean {
    private static final long MAX_WAIT_SECONDS = TimeUnit.MINUTES.toSeconds(2);

    @Asynchronous(executor = "java:app/concurrent/ScheduledExecutorA")
    public CompletableFuture<String> awaitAndGetThirdPartyContext(Semaphore invocationsStarted,
                                                                  CountDownLatch blocker) {
        invocationsStarted.release(1);
        CompletableFuture<String> future = Asynchronous.Result.getFuture();
        try {
            blocker.await(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            future.complete(IntContext.get() + StringContext.get());
        } catch (Exception x) {
            future.completeExceptionally(x);
        }
        return future;
    }

    @Asynchronous(executor = "java:comp/concurrent/ScheduledExecutorC")
    public CompletionStage<ContextService> lookUpAContextService() {
        try {
            return CompletableFuture.completedFuture(InitialContext.doLookup("java:comp/concurrent/ContextC"));
        } catch (NamingException x) {
            throw new CompletionException(x);
        }
    }

    public CompletableFuture<String> notAsynchronous() {
        return CompletableFuture.completedFuture(Thread.currentThread().getName());
    }
}
