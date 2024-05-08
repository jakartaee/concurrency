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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static jakarta.enterprise.concurrent.ContextServiceDefinition.APPLICATION;
import static jakarta.enterprise.concurrent.ContextServiceDefinition.TRANSACTION;

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
import ee.jakarta.tck.concurrent.common.qualifiers.CustomQualifier1;
import ee.jakarta.tck.concurrent.common.qualifiers.CustomQualifier2;
import ee.jakarta.tck.concurrent.common.qualifiers.InvalidQualifier3;
import ee.jakarta.tck.concurrent.common.qualifiers.OverwrittenQualifier4;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.enterprise.concurrent.ManagedThreadFactoryDefinition;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;

@ApplicationScoped
@ContextServiceDefinition(
        name = "java:app/concurrent/ContextE",
        qualifiers = {CustomQualifier1.class},
        cleared = {"IntContext"},
        propagated = {APPLICATION, "StringContext"},
        unchanged = {TRANSACTION})
@ManagedExecutorDefinition(
        name = "java:app/concurrent/ExecutorE",
        context = "java:app/concurrent/ContextE",
        qualifiers = {CustomQualifier2.class},
        maxAsync = 3)
@ManagedScheduledExecutorDefinition(
        name = "java:app/concurrent/ScheduledExecutorE",
        context = "java:app/concurrent/ContextE",
        qualifiers = {CustomQualifier1.class, CustomQualifier2.class},
        maxAsync = 2,
        hungTaskThreshold = 200000)
@ManagedThreadFactoryDefinition(
        name = "java:app/concurrent/ThreadFactoryE",
        context = "java:app/concurrent/ContextE",
        qualifiers = OverwrittenQualifier4.class, // overwritten via web.xml
        priority = 6
        )
@WebServlet("/AnnotationServlet")
public class AnnotationServlet extends TestServlet {
    
    private static final long serialVersionUID = 1L;
    private static final long MAX_WAIT_SECONDS = TimeUnit.MINUTES.toSeconds(2);
    
    private static final Runnable NOOP_RUNNABLE = () -> { };

    @EJB
    private AnnotationTestBeanInterface enterpriseBean;

    @Resource
    private UserTransaction tx;
    
    private Integer executeCallableWithContext(final ContextService svc, final int value) throws Exception {
        try {
            IntContext.set(value);
            Callable<Integer> result = svc.contextualCallable(() -> {
                return IntContext.get();
            });
            return result.call();
        } finally {
            IntContext.set(0);
        }
    }
    
    // Context Services
    @Inject
    private ContextService injectedDefContextSvc;

    @Inject
    @CustomQualifier1
    private ContextService injectedContextE;
    
    public void testAnnoDefinedContextServiceQualifiers() throws Throwable {
        assertAll("Context Service Tests",
                () -> assertNotNull(injectedDefContextSvc,
                        "Default contextService was not registered with default qualifier."),
                () -> assertNotNull(injectedContextE,
                        "Annotation defined contextService was not registered with required qualifier."),
                () -> assertTrue(CDI.current().select(ContextService.class, InvalidQualifier3.Literal.get()).isUnsatisfied(),
                        "A contextService was satisfied with a qualifier which was not defined in it's annotation")
                );
        
        //  Verify injected and looked up default context service are the same
        ContextService lookupDefContextSvc = InitialContext.doLookup("java:comp/DefaultContextService");
        
        Integer expected1 = executeCallableWithContext(lookupDefContextSvc, 95);
        Integer actual1 = executeCallableWithContext(injectedDefContextSvc, 95);
        
        assertEquals(expected1, actual1, "Default Context Service behavior differed between injection and lookup");
        
        //  Verify injected and looked up annotation defined context service are the same
        ContextService lookupContextE = InitialContext.doLookup("java:app/concurrent/ContextE");
        
        assertEquals(Integer.valueOf(0), executeCallableWithContext(lookupContextE, 65),
                "Annotation defined Context Service that was looked up did not clear the IntContext as configured.");
        assertEquals(Integer.valueOf(0), executeCallableWithContext(injectedContextE, 85),
                "Annotation defined Context Service that was injected based on a qualifier did not clear the IntContext as configured.");
    }
    
    // Managed Executor Services
    @Inject
    private ManagedExecutorService injectedDefMES;

    @Inject
    @CustomQualifier2
    private ManagedExecutorService injectedMESE;
    
    public void testAnnoDefinedManagedExecutorSvcQualifiers() throws Throwable {
        assertAll("Managed Executor Service Tests",
            () -> assertNotNull(injectedDefMES,
                    "Default managedExecutorService was not registered with default qualifier."),
            () -> assertNotNull(injectedMESE,
                    "Annotation defined managedExecutorService was not registered with required qualifiers."),
            () -> assertTrue(CDI.current().select(ManagedExecutorService.class, CustomQualifier2.Literal.get(), InvalidQualifier3.Literal.get()).isUnsatisfied(),
                    "A managedExecutorService was satisfied with both a required and non-required qualifier.")
        );
        
        //  Verify injected and looked up default ManagedExecutorService behave the same
        ManagedExecutorService lookupDefMES = InitialContext.doLookup("java:comp/DefaultManagedExecutorService");
        
        ContextService lookupDefContextSvc = lookupDefMES.getContextService();
        ContextService extractedDefContextSvc = injectedDefMES.getContextService();
        
        Integer expected1 = executeCallableWithContext(lookupDefContextSvc, 95);
        Integer actual1 = executeCallableWithContext(extractedDefContextSvc, 95);
        
        assertEquals(expected1, actual1, "Default ManagedExecutorService behavior via context service differed between injection and lookup");
        
        //  Verify injected and looked up annotation defined ManagedExecutorService behave the same
        ManagedExecutorService lookupMESE = InitialContext.doLookup("java:app/concurrent/ExecutorE");
        
        ContextService lookupContextE = lookupMESE.getContextService();
        ContextService extractedContextE = injectedMESE.getContextService();
        
        assertEquals(Integer.valueOf(0), executeCallableWithContext(lookupContextE, 65),
                "Annotation defined and looked up ManagedExecutorService was configured with a context service that did not clear the IntContext as configured.");
        assertEquals(Integer.valueOf(0), executeCallableWithContext(extractedContextE, 85),
                "Annotation defined and injected ManagedExecutorService was configured with a context service that did not clear the IntContext as configured.");
    }
    
    // Managed Scheduled Executor Services
    @Inject
    private ManagedScheduledExecutorService injectedDefMSES;

    @Inject
    @CustomQualifier1
    @CustomQualifier2
    private ManagedScheduledExecutorService injectedMSESE;
    
    public void testAnnoDefinedManagedScheduledExecutorSvcQualifers() throws Throwable {
        assertAll("Managed Scheduled Executor Service Tests",
                () -> assertNotNull(injectedDefMSES,
                        "Default managedScheduledExecutorService was not registered with default qualifier."),
                () -> assertNotNull(injectedMSESE,
                        "Annotation defined managedScheduledExecutorService was not registered with required qualifiers."),
                () -> assertTrue(CDI.current().select(ManagedScheduledExecutorService.class, CustomQualifier1.Literal.get()).isResolvable(),
                        "A managedScheduledExecutorService was not satisfied with one of two configured qualifiers.")
        );
        
        //  Verify injected and looked up default ManagedScheduledExecutorService behave the same
        ManagedScheduledExecutorService lookupDefMSES = InitialContext.doLookup("java:comp/DefaultManagedScheduledExecutorService");

        ContextService lookupDefContextSvc = lookupDefMSES.getContextService();
        ContextService extractedDefContextSvc = injectedDefMSES.getContextService();
        
        Integer expected1 = executeCallableWithContext(lookupDefContextSvc, 95);
        Integer actual1 = executeCallableWithContext(extractedDefContextSvc, 95);
        
        assertEquals(expected1, actual1, "Default ManagedScheduledExecutorService behavior via context service differed between injection and lookup");
        
        //  Verify injected and looked up annotation defined ManagedExecutorService behave the same
        ManagedScheduledExecutorService lookupMSESE = InitialContext.doLookup("java:app/concurrent/ScheduledExecutorE");

        ContextService lookupContextE = lookupMSESE.getContextService();
        ContextService extractedContextE = injectedMSESE.getContextService();
        
        assertEquals(Integer.valueOf(0), executeCallableWithContext(lookupContextE, 65),
                "Annotation defined and looked up ManagedScheduledExecutorService was configured with a context service that did not clear the IntContext as configured.");
        assertEquals(Integer.valueOf(0), executeCallableWithContext(extractedContextE, 85),
                "Annotation defined and injected ManagedScheduledExecutorService was configured with a context service that did not clear the IntContext as configured.");
    }

    // Managed Thread Factory
    @Inject
    private ManagedThreadFactory injectedDefMTF;

    @Resource(lookup = "java:app/concurrent/ThreadFactoryE")
    private ManagedThreadFactory resourceMTFE;
    
    // Qualifier on @ManagedThreadFactoryDefinition is only overridden in web mode
    public void testAnnoDefinedManagedThreadFactoryQualifersFull() throws Throwable {
        
        ManagedThreadFactory lookupDefMTF = InitialContext.doLookup("java:comp/DefaultManagedThreadFactory");
        ManagedThreadFactory lookupMTFE = InitialContext.doLookup("java:app/concurrent/ThreadFactoryE");

        assertAll("Thread Factory Tests",
            () -> assertNotNull(injectedDefMTF,
                    "Default managedThreadFactory was not registered with default qualifier."),
            () -> assertEquals(lookupDefMTF.newThread(NOOP_RUNNABLE).getPriority(), injectedDefMTF.newThread(NOOP_RUNNABLE).getPriority(),
                    "Default managedThreadFactory from injection and lookup did not have the same priority."),
            () -> assertNotNull(resourceMTFE,
                    "Annotation defined managedThreadFactory with qualifiers could not be found via @Resource."),
            () -> assertEquals(lookupMTFE.newThread(NOOP_RUNNABLE).getPriority(), resourceMTFE.newThread(NOOP_RUNNABLE).getPriority(),
                    "The managedThreadFactory from resource injection and lookup did not have the same priority."),
            () -> assertTrue(CDI.current().select(ManagedThreadFactory.class, OverwrittenQualifier4.Literal.get()).isResolvable(),
                    "A managedThreadFactory was not satisfied with a required qualifier that was not overwritten by a deployment descriptor.")
        );
    }

    // Qualifier on @ManagedThreadFactoryDefinition is overridden in web mode
    public void testAnnoDefinedManagedThreadFactoryQualifersWeb() throws Throwable {

        ManagedThreadFactory lookupDefMTF = InitialContext.doLookup("java:comp/DefaultManagedThreadFactory");
        ManagedThreadFactory lookupMTFE = InitialContext.doLookup("java:app/concurrent/ThreadFactoryE");

        assertAll("Thread Factory Tests",
            () -> assertNotNull(injectedDefMTF,
                    "Default managedThreadFactory was not registered with default qualifier."),
            () -> assertEquals(lookupDefMTF.newThread(NOOP_RUNNABLE).getPriority(), injectedDefMTF.newThread(NOOP_RUNNABLE).getPriority(),
                    "Default managedThreadFactory from injection and lookup did not have the same priority."),
            () -> assertNotNull(resourceMTFE,
                    "Annotation defined managedThreadFactory with no qualifiers could not be found via @Resource."),
            () -> assertEquals(lookupMTFE.newThread(NOOP_RUNNABLE).getPriority(), resourceMTFE.newThread(NOOP_RUNNABLE).getPriority(),
                    "The managedThreadFactory from resource injection and lookup did not have the same priority."),
            () -> assertTrue(CDI.current().select(ManagedThreadFactory.class, OverwrittenQualifier4.Literal.get()).isUnsatisfied(),
                    "A managedThreadFactory was satisfied with a required qualifier that should have been overwritten by the deployment descriptor.")
        );
    }

    public void testAnnotationDefinesContextService() throws Throwable {
        ContextService contextSvc = InitialContext.doLookup("java:app/concurrent/ContextE");

        Callable<Integer> checkContextAndGetTransactionStatus;

        tx.begin();
        try {
            IntContext.set(1001);
            StringContext.set("testAnnotationDefinesContextService-1");

            checkContextAndGetTransactionStatus = contextSvc.contextualCallable(() -> {
                assertEquals(0, IntContext.get()); // cleared
                assertEquals("testAnnotationDefinesContextService-1", StringContext.get()); // propagated
                assertNotNull(InitialContext.doLookup("java:app/concurrent/ExecutorE")); // propagated
                return tx.getStatus(); // unchanged
            });

            StringContext.set("testAnnotationDefinesContextService-2");

            int status = checkContextAndGetTransactionStatus.call();
            assertEquals(Status.STATUS_ACTIVE, status);

            assertEquals(1001, IntContext.get()); // restored
            assertEquals("testAnnotationDefinesContextService-2", StringContext.get()); // restored
        } finally {
            IntContext.set(0);
            StringContext.set(null);
            tx.rollback();
        }

        int status = checkContextAndGetTransactionStatus.call();
        assertEquals(Status.STATUS_NO_TRANSACTION, status);
    }

    public void testAnnotationDefinesManagedExecutor() throws Throwable {
        LinkedBlockingQueue<Object> started = new LinkedBlockingQueue<Object>();
        CountDownLatch taskCanEnd = new CountDownLatch(1);

        Supplier<String> task = () -> {
            try {
                started.add(InitialContext.doLookup("java:app/concurrent/ExecutorE")); // requires Application context
                assertTrue(taskCanEnd.await(MAX_WAIT_SECONDS, TimeUnit.SECONDS));
            } catch (InterruptedException | NamingException x) {
                throw new CompletionException(x);
            }
            return StringContext.get();
        };

        ManagedExecutorService executor = InitialContext.doLookup("java:app/concurrent/ExecutorE");

        try {
            StringContext.set("testAnnotationDefinesManagedExecutor-1");
            CompletableFuture<String> future1 = executor.supplyAsync(task);

            StringContext.set("testAnnotationDefinesManagedExecutor-2");
            CompletableFuture<String> future2 = executor.supplyAsync(task);

            StringContext.set("testAnnotationDefinesManagedExecutor-3");
            CompletableFuture<String> future3 = executor.supplyAsync(task);

            StringContext.set("testAnnotationDefinesManagedExecutor-4");
            CompletableFuture<String> future4 = executor.supplyAsync(task);

            StringContext.set("testAnnotationDefinesManagedExecutor-5");

            // 3 can start per max-async
            assertNotNull(started.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS));
            assertNotNull(started.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS));
            assertNotNull(started.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS));
            assertEquals(null, started.poll(1, TimeUnit.SECONDS));

            taskCanEnd.countDown();

            assertEquals("testAnnotationDefinesManagedExecutor-1",
                    future1.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS));
            assertEquals("testAnnotationDefinesManagedExecutor-2",
                    future2.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS));
            assertEquals("testAnnotationDefinesManagedExecutor-3",
                    future3.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS));
            assertEquals("testAnnotationDefinesManagedExecutor-4",
                    future4.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS));

            assertNotNull(started.poll(MAX_WAIT_SECONDS, TimeUnit.SECONDS));
        } finally {
            IntContext.set(0);
            StringContext.set(null);
        }
    }

    public void testAnnotationDefinesManagedScheduledExecutor() throws Throwable {
        enterpriseBean.testAnnotationDefinesManagedScheduledExecutor();
    }

    public void testAnnotationDefinesManagedThreadFactory() throws Throwable {
        enterpriseBean.testAnnotationDefinesManagedThreadFactory();
    }

}
