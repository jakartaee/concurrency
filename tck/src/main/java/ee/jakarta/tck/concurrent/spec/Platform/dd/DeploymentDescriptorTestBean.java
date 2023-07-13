/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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
package ee.jakarta.tck.concurrent.spec.Platform.dd;

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

@Local(DeploymentDescriptorTestBeanInterface.class)
@Stateless
public class DeploymentDescriptorTestBean implements DeploymentDescriptorTestBeanInterface {
    private static final long MAX_WAIT_SECONDS = TimeUnit.MINUTES.toSeconds(2);

    @Override
    public void testDeploymentDescriptorDefinesManagedScheduledExecutor() {
        try {
            LinkedBlockingQueue<Integer> started = new LinkedBlockingQueue<Integer>();
            CountDownLatch taskCanEnd = new CountDownLatch(1);

            Callable<String> task = () -> {
                started.add(IntContext.get());
                assertTrue(taskCanEnd.await(MAX_WAIT_SECONDS, TimeUnit.SECONDS));
                return StringContext.get();
            };

            ManagedScheduledExecutorService executor = InitialContext
                    .doLookup("java:global/concurrent/ScheduledExecutorD");

            IntContext.set(3000);

            StringContext.set("testDeploymentDescriptorDefinesManagedScheduledExecutor-1");
            Future<String> future1 = executor.submit(task);

            StringContext.set("testDeploymentDescriptorDefinesManagedScheduledExecutor-2");
            Future<String> future2 = executor.submit(task);

            StringContext.set("testDeploymentDescriptorDefinesManagedScheduledExecutor-3");
            Future<String> future3 = executor.submit(task);

            StringContext.set("testDeploymentDescriptorDefinesManagedScheduledExecutor-4");

            // 2 can start per max-async
            assertEquals(started.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS), Integer.valueOf(0));
            assertEquals(started.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS), Integer.valueOf(0));
            assertEquals(started.poll(1, TimeUnit.SECONDS), null);

            taskCanEnd.countDown();

            assertEquals(future1.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                    "testDeploymentDescriptorDefinesManagedScheduledExecutor-1");
            assertEquals(future2.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                    "testDeploymentDescriptorDefinesManagedScheduledExecutor-2");
            assertEquals(future3.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS),
                    "testDeploymentDescriptorDefinesManagedScheduledExecutor-3");

            assertEquals(started.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS), Integer.valueOf(0));
        } catch (ExecutionException | InterruptedException | NamingException | TimeoutException x) {
            throw new EJBException(x);
        } finally {
            IntContext.set(0);
            StringContext.set(null);
        }
    }

    @Override
    public void testDeploymentDescriptorDefinesManagedThreadFactory() {
        try {
            IntContext.set(4000);
            StringContext.set("testDeploymentDescriptorDefinesManagedThreadFactory-1");

            ManagedThreadFactory threadFactory = (ManagedThreadFactory) InitialContext
                    .doLookup("java:app/concurrent/ThreadFactoryD");

            StringContext.set("testDeploymentDescriptorDefinesManagedThreadFactory-2");

            LinkedBlockingQueue<Object> results = new LinkedBlockingQueue<>();

            Thread thread = threadFactory.newThread(() -> {
                results.add(Thread.currentThread().getPriority());
                results.add(IntContext.get());
                results.add(StringContext.get());
                try {
                    results.add(InitialContext.doLookup("java:app/concurrent/ThreadFactoryD"));
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
                    "testDeploymentDescriptorDefinesManagedThreadFactory-1"); // propagated
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
