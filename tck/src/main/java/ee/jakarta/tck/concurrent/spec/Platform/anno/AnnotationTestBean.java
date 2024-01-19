/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package ee.jakarta.tck.concurrent.spec.Platform.anno;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import ee.jakarta.tck.concurrent.common.context.IntContext;
import ee.jakarta.tck.concurrent.common.context.StringContext;
import jakarta.ejb.EJBException;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.ManagedThreadFactory;

@Local(AnnotationTestBeanInterface.class)
@Stateless
public class AnnotationTestBean implements AnnotationTestBeanInterface {
    private static final long MAX_WAIT_SECONDS = TimeUnit.MINUTES.toSeconds(2);

    @Override
    public void testAnnotationDefinesManagedScheduledExecutor() {
        try {
            LinkedBlockingQueue<Integer> started = new LinkedBlockingQueue<Integer>();
            CountDownLatch taskCanEnd = new CountDownLatch(1);

            Callable<String> task = () -> {
                started.add(IntContext.get());
                assertTrue(taskCanEnd.await(MAX_WAIT_SECONDS, TimeUnit.SECONDS));
                return StringContext.get();
            };

            ManagedScheduledExecutorService executor = InitialContext
                    .doLookup("java:global/concurrent/ScheduledExecutorE");

            IntContext.set(3000);

            StringContext.set("testAnnotationDefinesManagedScheduledExecutor-1");
            Future<String> future1 = executor.submit(task);

            StringContext.set("testAnnotationDefinesManagedScheduledExecutor-2");
            Future<String> future2 = executor.submit(task);

            StringContext.set("testAnnotationDefinesManagedScheduledExecutor-3");
            Future<String> future3 = executor.submit(task);

            StringContext.set("testAnnotationDefinesManagedScheduledExecutor-4");

            // 2 can start per max-async
            assertEquals(started.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS), Integer.valueOf(0));
            assertEquals(started.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS), Integer.valueOf(0));
            assertEquals(started.poll(1, TimeUnit.SECONDS), null);

            taskCanEnd.countDown();

            assertEquals(future1.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                    "testAnnotationDefinesManagedScheduledExecutor-1");
            assertEquals(future2.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                    "testAnnotationDefinesManagedScheduledExecutor-2");
            assertEquals(future3.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                    "testAnnotationDefinesManagedScheduledExecutor-3");

            assertEquals(started.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS), Integer.valueOf(0));
        } catch (ExecutionException | InterruptedException | NamingException | TimeoutException x) {
            throw new EJBException(x);
        } finally {
            IntContext.set(0);
            StringContext.set(null);
        }
    }

    @Override
    public void testAnnotationDefinesManagedThreadFactory() {
        try {
            IntContext.set(4000);
            StringContext.set("testAnnotationDefinesManagedThreadFactory-1");

            ManagedThreadFactory threadFactory = (ManagedThreadFactory) InitialContext
                    .doLookup("java:app/concurrent/ThreadFactoryE");

            StringContext.set("testAnnotationDefinesManagedThreadFactory-2");

            LinkedBlockingQueue<Object> results = new LinkedBlockingQueue<>();

            Thread thread = threadFactory.newThread(() -> {
                results.add(Thread.currentThread().getPriority());
                results.add(IntContext.get());
                results.add(StringContext.get());
                try {
                    results.add(InitialContext.doLookup("java:app/concurrent/ThreadFactoryE"));
                } catch (Exception x) {
                    results.add(x);
                }
            });

            assertEquals(thread.getPriority(), 6); // configured value on managed-thread-factory

            thread.start();

            assertEquals(results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS), Integer.valueOf(6)); // priority from
                                                                                                // managed-thread-factory
            assertEquals(results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS), Integer.valueOf(0)); // IntContext cleared
            assertEquals(results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                    "testAnnotationDefinesManagedThreadFactory-1"); // propagated
            Object lookupResult = results.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            if (lookupResult instanceof Exception)
                throw new EJBException((Exception) lookupResult);
            assertNotNull(lookupResult);

        } catch (InterruptedException | NamingException x) {
            throw new EJBException(x);
        } finally {
            IntContext.set(0);
            StringContext.set(null);
        }
    }
}
