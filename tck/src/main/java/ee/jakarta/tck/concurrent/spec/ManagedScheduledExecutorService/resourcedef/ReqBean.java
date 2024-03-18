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
package ee.jakarta.tck.concurrent.spec.ManagedScheduledExecutorService.resourcedef;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import ee.jakarta.tck.concurrent.common.context.IntContext;
import ee.jakarta.tck.concurrent.common.context.StringContext;
import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.Schedule;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class ReqBean {

    private static final Logger log = Logger.getLogger(ReqBean.class.getCanonicalName());

    private static final long MAX_WAIT_SECONDS = TimeUnit.MINUTES.toSeconds(2);

    // Asynchronous Tests
    
    @Asynchronous(executor = "java:app/concurrent/ScheduledExecutorA")
    public CompletableFuture<String> awaitAndGetThirdPartyContext(final Semaphore invocationsStarted,
            final CountDownLatch blocker) {
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
    
    // Scheduled Asynchronous Tests
    public static enum RETURN {
            NULL, // Never completes
            COMPLETE_EXCEPTIONALLY, // Completes with exception
            COMPLETE_RESULT, // Completes with result
            INCOMPLETE, // Returns future that is not complete
            THROW_EXCEPTION; // Method throws exception
            
            private String message = "";
            
            public RETURN withMessage(final String m) {
                this.message = m;
                return this;
            }
            
            public String getMessage() {
                return message;
            }
    }
    
    /**
     * A scheduled async method that runs every 5 seconds
     *
     * @param runs - how many times to run before returning
     * @param type - how this method should return (successfully / incomplete / exceptionally)
     * @param counter - The counter provided from the caller to compare against
     *
     * @return A result or exception
     */
    @Asynchronous(executor = "java:comp/DefaultManagedScheduledExecutorService", runAt = @Schedule(cron = "*/5 * * * * *"))
    public CompletableFuture<Integer> scheduledEvery5seconds(final int runs, final RETURN type, final AtomicInteger counter) {
        int count = counter.incrementAndGet();
        
        log.info("Executing scheduledEvery5seconds method " + count + "/" + runs + " (Returning: " + type.toString() + ")");
        log.info("  Thread: " + Thread.currentThread().toString());
        
        if (runs != count) {
            return null; // Continue onto next scheduled execution
        }
        
        CompletableFuture<Integer> future = Asynchronous.Result.getFuture();
        
        switch (type) {
        case NULL: //Never stop executions
            return null;
        case COMPLETE_EXCEPTIONALLY:
            future.completeExceptionally(new Exception(type.getMessage()));
            break;
        case COMPLETE_RESULT:
            future.complete(count);
            break;
        case INCOMPLETE:
            break; //never complete future
        case THROW_EXCEPTION:
            throw new RuntimeException(type.getMessage());
        default:
            break;
        }
        
        return future;
    }
    
    @Asynchronous(executor = "java:comp/DefaultManagedScheduledExecutorService", runAt = @Schedule(cron = "*/3 * * * * *"))
    public void scheduledEvery3SecondsVoidReturn(final int runs, final RETURN type, final AtomicInteger counter) {
        int count = counter.incrementAndGet();
        
        log.info("Executing scheduledEvery3SecondsVoidReturn method " + count + "/" + runs + " (Returning: " + type.toString() + ")");
        log.info("  Thread: " + Thread.currentThread().toString());
        
        if (runs != count) {
            return; // Continue onto next scheduled execution
        }
        
        CompletableFuture<Void> future = Asynchronous.Result.getFuture();
        
        switch (type) {
        case COMPLETE_EXCEPTIONALLY:
            future.completeExceptionally(new Exception(type.getMessage()));
            break;
        case COMPLETE_RESULT:
            future.complete(null);
            break;
        case THROW_EXCEPTION:
            throw new RuntimeException(type.getMessage());
        default:
            break;
        }
    }
    
    /**
     * A scheduled async method that runs every 3 seconds, but takes 5 seconds to complete
     *
     * @param runs - how many times to run before returning
     * @param counter - The counter provided from the caller to compare against
     *
     * @return The number of runs completed
     */
    @Asynchronous(executor = "java:comp/DefaultManagedScheduledExecutorService", runAt = @Schedule(cron = "*/3 * * * * *"))
    public CompletableFuture<Integer> scheduledEvery3SecondsTakes5Seconds(final int runs, final AtomicInteger counter) {
        int count = counter.incrementAndGet();
        
        log.info("Executing scheduledEvery3SecondsTakes5Seconds method " + count + "/" + runs);
        log.info("  Thread: " + Thread.currentThread().toString());
        
        if (runs != count) {
            
            try {
                Thread.sleep(Duration.ofSeconds(5).toMillis());
            } catch (InterruptedException e) {
                throw new RuntimeException("Thread was interrupted while waiting", e);
            }
            
            return null; // Continue onto next scheduled execution
        }
        
        CompletableFuture<Integer> future = Asynchronous.Result.getFuture();
        future.complete(count);
        
        return future;
    }
    
    /**
     * A scheduled async method that runs every 3 seconds
     * Uses executor = "java:module/concurrent/ScheduledExecutorB" with max-async = 4
     *
     * @param runs - how many times to run before returning
     * @param counter - The counter provided from the caller to compare against
     *
     * @return completed future of IntContext
     */
    @Asynchronous(executor = "java:module/concurrent/ScheduledExecutorB", runAt = @Schedule(cron = "*/3 * * * * *"))
    public CompletableFuture<Integer> scheduledEvery3Seconds(final int runs, final AtomicInteger counter) {
        int count = counter.incrementAndGet();
        
        log.info("Executing scheduledEvery3Seconds method " + count + "/" + runs);
        log.info("  Thread: " + Thread.currentThread().toString());
        
        if (runs != count) {
            return null; // Continue onto next scheduled execution
        }
        
        CompletableFuture<Integer> future = Asynchronous.Result.getFuture();
        future.complete(IntContext.get());
        
        return future;
    }
    
    /**
     * A scheduled async method that runs every 3 seconds and every minute
     * Uses executor = "java:module/concurrent/ScheduledExecutorB" with max-async = 4
     *
     * @param runs - how many times to run before returning
     * @param counter - The counter provided from the caller to compare against
     *
     * @return completed future of StringContext
     */
    @Asynchronous(executor = "java:module/concurrent/ScheduledExecutorB", runAt = {
            @Schedule(cron = "*/3 * * * * *"),
            @Schedule(cron = "0 * * * * *")
    })
    public CompletableFuture<String> scheduledEvery3SecondsAnd1Minute(final int runs, final AtomicInteger counter) {
        int count = counter.incrementAndGet();
        
        log.info("Executing scheduledEvery3SecondsAnd1Minute method " + count + "/" + runs);
        log.info("  Thread: " + Thread.currentThread().toString());
        
        if (runs != count) {
            return null; // Continue onto next scheduled execution
        }
        
        CompletableFuture<String> future = Asynchronous.Result.getFuture();
        future.complete(StringContext.get());
        
        return future;
    }
    
    /**
     * A scheduled async method that should not run due to invalid configuration
     */
    @Asynchronous(executor = "java:app/concurrent/INVALID", runAt = @Schedule(cron = "*/3 * * * * *"))
    public CompletableFuture<String> scheduledInvalidExecutor() {
        throw new UnsupportedOperationException("Should not be able to execute with invalid executor");
    }
}
