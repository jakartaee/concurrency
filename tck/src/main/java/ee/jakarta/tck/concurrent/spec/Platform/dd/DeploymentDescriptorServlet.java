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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import ee.jakarta.tck.concurrent.common.context.IntContext;
import ee.jakarta.tck.concurrent.common.context.StringContext;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;

@WebServlet("/DeploymentDescriptorServlet")
public class DeploymentDescriptorServlet extends TestServlet {
    private static final long serialVersionUID = 1L;
    private static final long MAX_WAIT_SECONDS = TimeUnit.MINUTES.toSeconds(2);

    @EJB
    DeploymentDescriptorTestBeanInterface enterpriseBean;

    @Resource
    UserTransaction tx;    

    /**
     * Tests context-service defined in a deployment descriptor.
     */
    public void testDeploymentDescriptorDefinesContextService() throws Throwable {
        ContextService contextSvc = InitialContext.doLookup("java:global/concurrent/ContextD");
 
        Callable<Integer> checkContextAndGetTransactionStatus;

        tx.begin();
        try {
            IntContext.set(1001);
            StringContext.set("testDeploymentDescriptorDefinesContextService-1");

            checkContextAndGetTransactionStatus = contextSvc.contextualCallable(() -> {
                assertEquals(IntContext.get(), 0); // cleared
                assertEquals(StringContext.get(), "testDeploymentDescriptorDefinesContextService-1"); // propagated
                assertNotNull(InitialContext.doLookup("java:app/concurrent/ExecutorD")); // propagated
                return tx.getStatus(); // unchanged
            });

            StringContext.set("testDeploymentDescriptorDefinesContextService-2");

            int status = checkContextAndGetTransactionStatus.call();
            assertEquals(status, Status.STATUS_ACTIVE);

            assertEquals(IntContext.get(), 1001); // restored
            assertEquals(StringContext.get(), "testDeploymentDescriptorDefinesContextService-2"); // restored
        } finally {
            IntContext.set(0);
            StringContext.set(null);
            tx.rollback();
        }

        int status = checkContextAndGetTransactionStatus.call();
        assertEquals(status, Status.STATUS_NO_TRANSACTION);
    }

    /**
     * Tests managed-executor defined in a deployment descriptor.
     */
    public void testDeploymentDescriptorDefinesManagedExecutor() throws Throwable {
        LinkedBlockingQueue<Object> started = new LinkedBlockingQueue<Object>();
        CountDownLatch taskCanEnd = new CountDownLatch(1);

        Supplier<String> task = () -> {
            try {
                started.add(InitialContext.doLookup("java:app/concurrent/ExecutorD")); // requires Application context
                assertTrue(taskCanEnd.await(MAX_WAIT_SECONDS, TimeUnit.SECONDS));
            } catch (InterruptedException | NamingException x) {
                throw new CompletionException(x);
            }
            return StringContext.get();
        };

        ManagedExecutorService executor = InitialContext.doLookup("java:app/concurrent/ExecutorD");

        try {
            StringContext.set("testDeploymentDescriptorDefinesManagedExecutor-1");
            CompletableFuture<String> future1 = executor.supplyAsync(task);

            StringContext.set("testDeploymentDescriptorDefinesManagedExecutor-2");
            CompletableFuture<String> future2 = executor.supplyAsync(task);

            StringContext.set("testDeploymentDescriptorDefinesManagedExecutor-3");
            CompletableFuture<String> future3 = executor.supplyAsync(task);

            StringContext.set("testDeploymentDescriptorDefinesManagedExecutor-4");
            CompletableFuture<String> future4 = executor.supplyAsync(task);

            StringContext.set("testDeploymentDescriptorDefinesManagedExecutor-5");

            // 3 can start per max-async
            assertNotNull(started.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS));
            assertNotNull(started.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS));
            assertNotNull(started.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS));
            assertEquals(started.poll(1, TimeUnit.SECONDS), null);

            taskCanEnd.countDown();

            assertEquals(future1.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS), "testDeploymentDescriptorDefinesManagedExecutor-1");
            assertEquals(future2.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS), "testDeploymentDescriptorDefinesManagedExecutor-2");
            assertEquals(future3.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS), "testDeploymentDescriptorDefinesManagedExecutor-3");
            assertEquals(future4.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS), "testDeploymentDescriptorDefinesManagedExecutor-4");

            assertNotNull(started.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS));
        } finally {
            IntContext.set(0);
            StringContext.set(null);
        }
    }

    /**
     * Tests managed-scheduled-executor defined in a deployment descriptor.
     */
    public void testDeploymentDescriptorDefinesManagedScheduledExecutor() throws Throwable {
        enterpriseBean.testDeploymentDescriptorDefinesManagedScheduledExecutor();
    }

    /**
     * Tests managed-thread-factory defined in a deployment descriptor.
     */
    public void testDeploymentDescriptorDefinesManagedThreadFactory() throws Throwable {
        enterpriseBean.testDeploymentDescriptorDefinesManagedThreadFactory();
    }
}
