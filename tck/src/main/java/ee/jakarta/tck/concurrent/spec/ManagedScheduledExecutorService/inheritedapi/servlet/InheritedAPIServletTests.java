/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.concurrent.spec.ManagedScheduledExecutorService.inheritedapi.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.common.fixed.counter.CounterRunnableTask;
import ee.jakarta.tck.concurrent.common.fixed.counter.StaticCounter;
import ee.jakarta.tck.concurrent.common.tasks.CommonTasks;
import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common.PACKAGE;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;
import ee.jakarta.tck.concurrent.framework.junit.extensions.Assertions;
import ee.jakarta.tck.concurrent.framework.junit.extensions.Wait;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;

@Web
@Common({ PACKAGE.TASKS, PACKAGE.FIXED_COUNTER })
public class InheritedAPIServletTests {

    @Deployment(name = "InheritedAPIServletTests")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class);
    }

    @Resource(lookup = TestConstants.DefaultManagedScheduledExecutorService)
    public ManagedScheduledExecutorService scheduledExecutor;

    @BeforeEach
    public void before() {
        StaticCounter.reset();
    }

    /*
     * @testName: testApiSubmit
     * 
     * @assertion_ids: CONCURRENCY:SPEC:44.1
     * 
     * @test_Strategy:
     */
    @Test
    public void testApiSubmit() throws Exception {
        Future<?> result = scheduledExecutor.submit(new CommonTasks.SimpleCallable());
        Wait.waitTillFutureIsDone(result);
        assertEquals(result.get(), CommonTasks.SIMPLE_RETURN_STRING);

        result = scheduledExecutor.submit(new CommonTasks.SimpleRunnable());
        Wait.waitTillFutureIsDone(result);
        result.get();

        result = scheduledExecutor.submit(new CommonTasks.SimpleRunnable(), CommonTasks.SIMPLE_RETURN_STRING);
        Wait.waitTillFutureIsDone(result);
        assertEquals(result.get(), CommonTasks.SIMPLE_RETURN_STRING);
    }

    /*
     * @testName: testApiExecute
     * 
     * @assertion_ids: CONCURRENCY:SPEC:44.2
     * 
     * @test_Strategy:
     */
    @Test
    public void testApiExecute() {
        try {
            scheduledExecutor.execute(new CounterRunnableTask());
            StaticCounter.waitTill(1);
        } finally {
            StaticCounter.reset();
        }
    }

    /*
     * @testName: testApiInvokeAll
     * 
     * @assertion_ids: CONCURRENCY:SPEC:44.3
     * 
     * @test_Strategy:
     */
    @Test
    public void testApiInvokeAll() throws Exception {
        try {
            List<Callable<Integer>> taskList = new ArrayList<>();
            taskList.add(new CommonTasks.SimpleArgCallable(1));
            taskList.add(new CommonTasks.SimpleArgCallable(2));
            taskList.add(new CommonTasks.SimpleArgCallable(3));
            List<Future<Integer>> resultList = scheduledExecutor.invokeAll(taskList);
            for (Future<?> each : resultList) {
                Wait.waitTillFutureIsDone(each);
            }
            assertEquals(resultList.get(0).get(), 1);
            assertEquals(resultList.get(1).get(), 2);
            assertEquals(resultList.get(2).get(), 3);
            resultList = scheduledExecutor.invokeAll(taskList, TestConstants.WaitTimeout.getSeconds(),
                    TimeUnit.SECONDS);
            for (Future<?> each : resultList) {
                Wait.waitTillFutureIsDone(each);
            }
            assertEquals(resultList.get(0).get(), 1);
            assertEquals(resultList.get(1).get(), 2);
            assertEquals(resultList.get(2).get(), 3);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

        try {
            List<Callable<String>> taskList = new ArrayList<>();
            taskList.add(new CommonTasks.SimpleCallable(TestConstants.WaitTimeout));
            taskList.add(new CommonTasks.SimpleCallable(TestConstants.WaitTimeout));
            List<Future<String>> resultList = scheduledExecutor.invokeAll(taskList,
                    TestConstants.PollInterval.getSeconds(), TimeUnit.SECONDS);
            for (Future<?> each : resultList) {
                Wait.waitTillFutureThrowsException(each, CancellationException.class);
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /*
     * @testName: testApiInvokeAny
     * 
     * @assertion_ids: CONCURRENCY:SPEC:44.4
     * 
     * @test_Strategy:
     */
    @Test
    public void testApiInvokeAny() throws Exception {

        List<Callable<Integer>> taskList = new ArrayList<>();
        taskList.add(new CommonTasks.SimpleArgCallable(1));
        taskList.add(new CommonTasks.SimpleArgCallable(2));
        taskList.add(new CommonTasks.SimpleArgCallable(3));
        Integer result = scheduledExecutor.invokeAny(taskList);
        Assertions.assertBetween(result, 1, 3);

        result = scheduledExecutor.invokeAny(taskList, TestConstants.WaitTimeout.getSeconds(), TimeUnit.SECONDS);
        Assertions.assertBetween(result, 1, 3);

        assertThrows(TimeoutException.class, () -> {
            List<Callable<String>> taskList2 = new ArrayList<>();
            taskList2.add(new CommonTasks.SimpleCallable(TestConstants.WaitTimeout));
            taskList2.add(new CommonTasks.SimpleCallable(TestConstants.WaitTimeout));
            scheduledExecutor.invokeAny(taskList2, TestConstants.PollInterval.getSeconds(), TimeUnit.SECONDS);
        });
    }

    /*
     * @testName: testApiSchedule
     * 
     * @assertion_ids: CONCURRENCY:SPEC:44.5
     * 
     * @test_Strategy:
     */
    @Test
    public void testApiSchedule() throws Exception {
        Future<?> result = scheduledExecutor.schedule(new CommonTasks.SimpleCallable(),
                TestConstants.PollInterval.getSeconds(), TimeUnit.SECONDS);
        Wait.waitTillFutureIsDone(result);
        assertEquals(result.get(), CommonTasks.SIMPLE_RETURN_STRING);

        result = scheduledExecutor.schedule(new CommonTasks.SimpleRunnable(), TestConstants.PollInterval.getSeconds(),
                TimeUnit.SECONDS);
        Wait.waitTillFutureIsDone(result);
        assertEquals(result.get(), null);
    }

    /*
     * @testName: testApiScheduleAtFixedRate
     * 
     * @assertion_ids: CONCURRENCY:SPEC:44.6
     * 
     * @test_Strategy:
     */
    @Test
    public void testApiScheduleAtFixedRate() {
        ScheduledFuture<?> result = null;

        try {
            result = scheduledExecutor.scheduleAtFixedRate(new CounterRunnableTask(),
                    TestConstants.PollInterval.getSeconds(), TestConstants.PollInterval.getSeconds(), TimeUnit.SECONDS);
            Wait.sleep(TestConstants.WaitTimeout);
            Assertions.assertBetween(StaticCounter.getCount(), TestConstants.PollsPerTimeout - 2,
                    TestConstants.PollsPerTimeout + 2);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            if (result != null) {
                Wait.waitCancelFuture(result);
            }
        }
    }

    /*
     * @testName: testApiScheduleWithFixedDelay
     * 
     * @assertion_ids: CONCURRENCY:SPEC:44.7
     * 
     * @test_Strategy:
     */
    @Test
    public void testApiScheduleWithFixedDelay() {
        ScheduledFuture<?> result = null;
        try {
            result = scheduledExecutor.scheduleWithFixedDelay(new CounterRunnableTask(TestConstants.PollInterval),
                    TestConstants.PollInterval.getSeconds(), TestConstants.PollInterval.getSeconds(), TimeUnit.SECONDS);
            Wait.sleep(TestConstants.WaitTimeout);
            Assertions.assertBetween(StaticCounter.getCount(), (TestConstants.PollsPerTimeout / 2) - 2,
                    (TestConstants.PollsPerTimeout / 2) + 2);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            if (result != null) {
                Wait.waitCancelFuture(result);
            }
        }
    }

}
