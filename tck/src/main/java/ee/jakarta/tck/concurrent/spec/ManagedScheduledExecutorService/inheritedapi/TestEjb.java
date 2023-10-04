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

package ee.jakarta.tck.concurrent.spec.ManagedScheduledExecutorService.inheritedapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ee.jakarta.tck.concurrent.common.counter.CounterInterface;
import ee.jakarta.tck.concurrent.common.counter.CounterRunnableTask;
import ee.jakarta.tck.concurrent.common.fixed.counter.StaticCounter;
import ee.jakarta.tck.concurrent.common.tasks.CommonTasks;
import ee.jakarta.tck.concurrent.framework.EJBJNDIProvider;
import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.junit.extensions.Assertions;
import ee.jakarta.tck.concurrent.framework.junit.extensions.Wait;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;

@Stateless
public class TestEjb implements TestEjbInterface {

    @EJB
    private CounterInterface counter;

    @Resource(lookup = TestConstants.defaultManagedScheduledExecutorService)
    private ManagedScheduledExecutorService scheduledExecutor;

    public void testApiSubmit() {
        try {
            Future<?> result = scheduledExecutor.submit(new CommonTasks.SimpleCallable());
            assertEquals(Wait.waitForTaskComplete(result), TestConstants.simpleReturnValue);

            result = scheduledExecutor.submit(new CommonTasks.SimpleRunnable());
            Wait.waitForTaskComplete(result);

            result = scheduledExecutor.submit(new CommonTasks.SimpleRunnable(), TestConstants.simpleReturnValue);
            assertEquals(Wait.waitForTaskComplete(result), TestConstants.simpleReturnValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void testApiExecute() {
        try {
            EJBJNDIProvider nameProvider = ServiceLoader.load(EJBJNDIProvider.class).findFirst().orElseThrow();
            scheduledExecutor.execute(new CounterRunnableTask(nameProvider.getEJBJNDIName()));
            StaticCounter.waitTillSurpassed(1);
        } finally {
            counter.reset();
        }
    }

    public void testApiInvokeAll() {
        try {
            List<Callable<Integer>> taskList = new ArrayList<>();
            taskList.add(new CommonTasks.SimpleArgCallable(1));
            taskList.add(new CommonTasks.SimpleArgCallable(2));
            taskList.add(new CommonTasks.SimpleArgCallable(3));
            List<Future<Integer>> resultList = scheduledExecutor.invokeAll(taskList);
            for (Future<?> each : resultList) {
                Wait.waitForTaskComplete(each);
            }
            assertEquals(resultList.get(0).get(), 1);
            assertEquals(resultList.get(1).get(), 2);
            assertEquals(resultList.get(2).get(), 3);

            resultList = scheduledExecutor.invokeAll(taskList, TestConstants.waitTimeout.getSeconds(),
                    TimeUnit.SECONDS);
            for (Future<?> each : resultList) {
                Wait.waitForTaskComplete(each);
            }
            assertEquals(resultList.get(0).get(), 1);
            assertEquals(resultList.get(1).get(), 2);
            assertEquals(resultList.get(2).get(), 3);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            List<Callable<String>> taskList = new ArrayList<>();
            taskList.add(new CommonTasks.SimpleCallable(TestConstants.waitTimeout));
            taskList.add(new CommonTasks.SimpleCallable(TestConstants.waitTimeout));
            List<Future<String>> resultList = scheduledExecutor.invokeAll(taskList,
                    TestConstants.pollInterval.getSeconds(), TimeUnit.SECONDS);
            for (Future<?> each : resultList) {
                Wait.waitTillFutureThrowsException(each, CancellationException.class);
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    public void testApiInvokeAny() {
        try {
            List<Callable<Integer>> taskList = new ArrayList<>();
            taskList.add(new CommonTasks.SimpleArgCallable(1));
            taskList.add(new CommonTasks.SimpleArgCallable(2));
            taskList.add(new CommonTasks.SimpleArgCallable(3));
            Integer result = scheduledExecutor.invokeAny(taskList);
            Assertions.assertBetween(result, 1, 3);

            result = scheduledExecutor.invokeAny(taskList, TestConstants.waitTimeout.getSeconds(), TimeUnit.SECONDS);
            Assertions.assertBetween(result, 1, 3);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            List<Callable<String>> taskList = new ArrayList<>();
            taskList.add(new CommonTasks.SimpleCallable(TestConstants.waitTimeout));
            taskList.add(new CommonTasks.SimpleCallable(TestConstants.waitTimeout));
            scheduledExecutor.invokeAny(taskList, TestConstants.pollInterval.getSeconds(), TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            return; // expected
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
        fail("Task should be cancelled because of timeout");
    }

    public void testApiSchedule() {
        try {
            Future<?> result = scheduledExecutor.schedule(new CommonTasks.SimpleCallable(),
                    TestConstants.pollInterval.getSeconds(), TimeUnit.SECONDS);
            assertEquals(TestConstants.simpleReturnValue, Wait.waitForTaskComplete(result));

            result = scheduledExecutor.schedule(new CommonTasks.SimpleRunnable(),
                    TestConstants.pollInterval.getSeconds(), TimeUnit.SECONDS);
            assertEquals(null, Wait.waitForTaskComplete(result));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testApiScheduleAtFixedRate() {
        ScheduledFuture<?> result = null;
        try {
            EJBJNDIProvider nameProvider = ServiceLoader.load(EJBJNDIProvider.class).findFirst().orElseThrow();
            result = scheduledExecutor.scheduleAtFixedRate(new CounterRunnableTask(nameProvider.getEJBJNDIName()),
                    TestConstants.pollInterval.getSeconds(), TestConstants.pollInterval.getSeconds(), TimeUnit.SECONDS);
            Wait.sleep(TestConstants.waitTimeout);
            Assertions.assertBetween(counter.getCount(), TestConstants.pollsPerTimeout - 2,
                    TestConstants.pollsPerTimeout + 2);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            if (result != null) {
                Wait.waitCancelFuture(result);
            }
            counter.reset();
        }
    }

    public void testApiScheduleWithFixedDelay() {
        ScheduledFuture<?> result = null;
        try {
            EJBJNDIProvider nameProvider = ServiceLoader.load(EJBJNDIProvider.class).findFirst().orElseThrow();
            result = scheduledExecutor.scheduleWithFixedDelay(
                    new CounterRunnableTask(nameProvider.getEJBJNDIName(), TestConstants.pollInterval), // task
                    TestConstants.pollInterval.getSeconds(), // initial delay
                    TestConstants.pollInterval.getSeconds(), // delay
                    TimeUnit.SECONDS); // Time units
            Wait.sleep(TestConstants.waitTimeout);
            Assertions.assertBetween(counter.getCount(), (TestConstants.pollsPerTimeout / 2) - 2,
                    (TestConstants.pollsPerTimeout / 2) + 2);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            if (result != null) {
                Wait.waitCancelFuture(result);
            }
            counter.reset();
        }
    }

}
