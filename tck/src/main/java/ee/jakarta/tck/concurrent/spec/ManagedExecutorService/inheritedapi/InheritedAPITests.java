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

package ee.jakarta.tck.concurrent.spec.ManagedExecutorService.inheritedapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import ee.jakarta.tck.concurrent.common.fixed.counter.CounterRunnableTask;
import ee.jakarta.tck.concurrent.common.fixed.counter.StaticCounter;
import ee.jakarta.tck.concurrent.common.tasks.CommonTasks;
import ee.jakarta.tck.concurrent.framework.TestConstants;
import ee.jakarta.tck.concurrent.framework.junit.anno.Assertion;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common;
import ee.jakarta.tck.concurrent.framework.junit.anno.Common.PACKAGE;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;
import ee.jakarta.tck.concurrent.framework.junit.extensions.Assertions;
import ee.jakarta.tck.concurrent.framework.junit.extensions.Wait;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;

@Web
@Common({ PACKAGE.TASKS, PACKAGE.FIXED_COUNTER })
public class InheritedAPITests {

    @Deployment(name = "InheritedAPITests")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class);
    }

    @Resource(lookup = TestConstants.defaultManagedExecutorService)
    private ManagedExecutorService executor;

    @Assertion(id = "SPEC:10.2; SPEC:13; SPEC:13.1; SPEC:13.2",
            strategy = "Test basic function for ManagedExecutorService: execute")
    public void testExecute() {
        try {
            executor.execute(new CounterRunnableTask());
            StaticCounter.waitTill(1);
        } finally {
            StaticCounter.reset();
        }
    }

    @Assertion(id = "SPEC:14; SPEC:14.1; SPEC:14.2; SPEC:14.3",
            strategy = "Test basic function for ManagedExecutorService: submit")
    public void testSubmit() throws Exception {
        Future<?> result = executor.submit(new CommonTasks.SimpleCallable());
        assertEquals(Wait.waitForTaskComplete(result), TestConstants.simpleReturnValue);

        result = executor.submit(new CommonTasks.SimpleRunnable());
        Wait.waitForTaskComplete(result);

        result = executor.submit(new CommonTasks.SimpleRunnable(), TestConstants.simpleReturnValue);
        assertEquals(Wait.waitForTaskComplete(result), TestConstants.simpleReturnValue);
    }

    @Assertion(id = "SPEC:14.4; SPEC:6.1; SPEC:6.2; SPEC:8",
            strategy = "Test basic function for ManagedExecutorService: invokeAny")
    public void testInvokeAny() {
        try {
            List<Callable<Integer>> taskList = new ArrayList<>();
            taskList.add(new CommonTasks.SimpleArgCallable(1));
            taskList.add(new CommonTasks.SimpleArgCallable(2));
            taskList.add(new CommonTasks.SimpleArgCallable(3));
            Integer result = executor.invokeAny(taskList);
            Assertions.assertBetween(result, 1, 3);

            result = executor.invokeAny(taskList, TestConstants.waitTimeout.getSeconds(), TimeUnit.SECONDS);
            Assertions.assertBetween(result, 1, 3);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertThrows(TimeoutException.class, () -> {
            List<Callable<String>> taskList = new ArrayList<>();
            taskList.add(new CommonTasks.SimpleCallable(TestConstants.waitTimeout));
            taskList.add(new CommonTasks.SimpleCallable(TestConstants.waitTimeout));
            executor.invokeAny(taskList, TestConstants.pollInterval.getSeconds(), TimeUnit.SECONDS);
        });
    }

    @Assertion(id = "SPEC:8.1; SPEC:9; SPEC:10; SPEC:10.2; SPEC:12; SPEC:19; SPEC:27",
            strategy = "Test basic function for ManagedExecutorService: invokeAll")
    public void testInvokeAll() {
        try {
            List<Callable<Integer>> taskList = new ArrayList<>();
            taskList.add(new CommonTasks.SimpleArgCallable(1));
            taskList.add(new CommonTasks.SimpleArgCallable(2));
            taskList.add(new CommonTasks.SimpleArgCallable(3));
            List<Future<Integer>> resultList = executor.invokeAll(taskList);
            for (Future<?> each : resultList) {
                Wait.waitForTaskComplete(each);
            }
            assertEquals(resultList.get(0).get(), 1);
            assertEquals(resultList.get(1).get(), 2);
            assertEquals(resultList.get(2).get(), 3);

            resultList = executor.invokeAll(taskList, TestConstants.waitTimeout.getSeconds(), TimeUnit.SECONDS);
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
            List<Future<String>> resultList = executor.invokeAll(taskList, TestConstants.pollInterval.getSeconds(),
                    TimeUnit.SECONDS);
            for (Future<?> each : resultList) {
                Wait.waitTillFutureThrowsException(each, CancellationException.class);
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
}
