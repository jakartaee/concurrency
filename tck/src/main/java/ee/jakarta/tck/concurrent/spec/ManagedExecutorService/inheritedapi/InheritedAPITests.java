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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.concurrent.framework.TestUtil;
import ee.jakarta.tck.concurrent.framework.junit.anno.Web;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;

@Web
public class InheritedAPITests {

    @Deployment(name = "InheritedAPITests")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addClass(Task.class);
    }

    @Resource
    public ManagedExecutorService mes;

    /*
     * @testName: testBasicManagedExecutorService
     * 
     * @assertion_ids:
     * CONCURRENCY:SPEC:10.2;CONCURRENCY:SPEC:13;CONCURRENCY:SPEC:13.1;CONCURRENCY
     * :SPEC:13.2;
     * CONCURRENCY:SPEC:14;CONCURRENCY:SPEC:14.1;CONCURRENCY:SPEC:14.2;CONCURRENCY
     * :SPEC:14.3;
     * CONCURRENCY:SPEC:14.4;CONCURRENCY:SPEC:6.1;CONCURRENCY:SPEC:6.2;CONCURRENCY
     * :SPEC:8;
     * CONCURRENCY:SPEC:8.1;CONCURRENCY:SPEC:9;CONCURRENCY:SPEC:10;CONCURRENCY:
     * SPEC:10.2; CONCURRENCY:SPEC:12;CONCURRENCY:SPEC:19;CONCURRENCY:SPEC:27;
     * 
     * @test_Strategy: test basic function for ManagedExecutorService include
     * execute, submit, invokeAny, invokeAll, atMostOnce
     */

    @Test
    public void testExecute() {
        Task<?> commonTask = new Task.CommonTask(0);
        mes.execute(commonTask);
        // wait for a while.
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testSubmit() {
        Task<?> commonTask = new Task.CommonTask(0);
        Future<?> noRes = mes.submit((Runnable) commonTask);
        try {
            TestUtil.waitForTaskComplete(noRes);
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void testInvokeAny() {
        Task.CommonTask commonTask0 = new Task.CommonTask(0);
        Task.CommonTask commonTask1 = new Task.CommonTask(1);
        List<Task.CommonTask> tasks = new ArrayList<Task.CommonTask>();
        tasks.add(commonTask0);
        tasks.add(commonTask1);
        int res = -1;
        try {
            res = mes.invokeAny(tasks);
        } catch (InterruptedException e) {
            fail(e.toString());
        } catch (ExecutionException e) {
            fail(e.toString());
        }
        assertTrue(tasks.get(res).isRan());
    }

    @Test
    public void testInvokeAll() {
        Task.CommonTask commonTask0 = new Task.CommonTask(0);
        Task.CommonTask commonTask1 = new Task.CommonTask(1);
        List<Task.CommonTask> tasks = new ArrayList<Task.CommonTask>();
        tasks.add(commonTask0);
        tasks.add(commonTask1);
        List<Future<Integer>> res = null;
        try {
            res = mes.invokeAll(tasks);
            TestUtil.waitForTaskComplete(res.get(0));
            TestUtil.waitForTaskComplete(res.get(1));
        } catch (Exception e) {
            fail(e.toString());
        }
        assertTrue(commonTask0.isRan());
        assertTrue(commonTask1.isRan());
    }

    @Test
    public void testAtMostOnce() {
        Task.CommonTask commonTask = new Task.CommonTask(0);
        Future<?> future = mes.submit((Runnable) commonTask);
        try {
            TestUtil.waitForTaskComplete(future);
        } catch (Exception e) {
            fail(e.toString());
        }
        // check number.
        assertEquals(commonTask.runCount(), 1);
    }
}
