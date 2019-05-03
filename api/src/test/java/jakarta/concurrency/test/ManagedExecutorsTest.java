/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.concurrency.test;

import jakarta.concurrency.ManagedExecutorService;
import jakarta.concurrency.ManagedTask;
import jakarta.concurrency.ManagedExecutors;
import jakarta.concurrency.ManagedTaskListener;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import static org.junit.Assert.*;
import org.junit.Test;

public class ManagedExecutorsTest {
    
    /**
     * Basic test for ManagedExecutors.managedTask(Runnable, ManagedTaskListener)
     */
    @Test
    public void testManagedTask_Runnable_ManagedTaskListener() {
        RunnableImpl task = new RunnableImpl();
        ManagedTaskListenerImpl taskListener = new ManagedTaskListenerImpl();
        
        Runnable wrapped = ManagedExecutors.managedTask(task, taskListener);
        ManagedTask managedTask = (ManagedTask) wrapped;
        assertTrue(taskListener == managedTask.getManagedTaskListener());

        wrapped.run();
        assertTrue(task.ran);
}

    /**
     * Basic test for ManagedExecutors.managedTask(Runnable, Map, ManagedTaskListener)
     */
    @Test
    public void testManagedTask_Runnable_executionProperties_ManagedTaskListener() {
        RunnableImpl task = new RunnableImpl();
        ManagedTaskListenerImpl taskListener = new ManagedTaskListenerImpl();
        Map<String, String> executionProperties = new HashMap<String, String>();
        final String TASK_NAME = "task1";
        executionProperties.put(ManagedTask.IDENTITY_NAME, TASK_NAME);
        executionProperties.put(ManagedTask.LONGRUNNING_HINT, "true");
        
        Runnable wrapped = ManagedExecutors.managedTask(task, executionProperties, taskListener);
        ManagedTask managedTask = (ManagedTask) wrapped;
        assertTrue(taskListener == managedTask.getManagedTaskListener());
        assertEquals("true", managedTask.getExecutionProperties().get(ManagedTask.LONGRUNNING_HINT));
        assertEquals(TASK_NAME, managedTask.getExecutionProperties().get(ManagedTask.IDENTITY_NAME));
        
        wrapped.run();
        assertTrue(task.ran);
    }

    /**
     * Test for ManagedExecutors.managedTask(Runnable, Map, ManagedTaskListener)
     * but task already implements ManagedTask, and both executionProerties and
     * taskListeners were passed to managedTask().
     */
    @Test
    public void testManagedTask_Runnable_ManagedTask() {
        ManagedTaskListenerImpl TASK_LISTENER = new ManagedTaskListenerImpl();
        Map<String, String> EXEC_PROPERTIES = new HashMap<String, String>();
        EXEC_PROPERTIES.put("custom", "true");
        EXEC_PROPERTIES.put(ManagedTask.LONGRUNNING_HINT, "false");
        final String TASK_DESCRIPTION = "task1 description";
        ManagedTaskRunnableImpl task = new ManagedTaskRunnableImpl(TASK_DESCRIPTION, EXEC_PROPERTIES, TASK_LISTENER);

        ManagedTaskListenerImpl taskListener = new ManagedTaskListenerImpl();
        Map<String, String> executionProperties = new HashMap<String, String>();
        final String TASK_NAME = "task1";
        executionProperties.put(ManagedTask.IDENTITY_NAME, TASK_NAME);
        executionProperties.put(ManagedTask.LONGRUNNING_HINT, "true");
        
        Runnable wrapped = ManagedExecutors.managedTask(task, executionProperties, taskListener);
        ManagedTask managedTask = (ManagedTask) wrapped;
        assertTrue(taskListener == managedTask.getManagedTaskListener());
        assertEquals("true", managedTask.getExecutionProperties().get(ManagedTask.LONGRUNNING_HINT));
        assertEquals(TASK_NAME, managedTask.getExecutionProperties().get(ManagedTask.IDENTITY_NAME));
        assertEquals("true", managedTask.getExecutionProperties().get("custom"));
    }

    /**
     * Test for ManagedExecutors.managedTask(Runnable, Map, ManagedTaskListener)
     * but task already implements ManagedTask, and both executionProerties and
     * taskListeners passed to managedTask() were null.
     */
    @Test
    public void testManagedTask_Runnable_ManagedTask_null_args() {
        ManagedTaskListenerImpl TASK_LISTENER = new ManagedTaskListenerImpl();
        Map<String, String> EXEC_PROPERTIES = new HashMap<String, String>();
        EXEC_PROPERTIES.put("custom", "true");
        final String TASK_DESCRIPTION = "task1 description";
        ManagedTaskRunnableImpl task = new ManagedTaskRunnableImpl(TASK_DESCRIPTION, EXEC_PROPERTIES, TASK_LISTENER);
        
        Runnable wrapped = ManagedExecutors.managedTask(task, null, null);
        ManagedTask managedTask = (ManagedTask) wrapped;
        assertTrue(TASK_LISTENER == managedTask.getManagedTaskListener());
        assertEquals("true", managedTask.getExecutionProperties().get("custom"));
    }

    /**
     * Basic test for ManagedExecutors.managedTask(Callable, ManagedTaskListener)
     */
    @Test
    public void testManagedTask_Callable_ManagedTaskListener() throws Exception {
        final String RESULT = "result";
        CallableImpl<String> task = new CallableImpl<String>(RESULT);
        ManagedTaskListenerImpl taskListener = new ManagedTaskListenerImpl();
        
        Callable<String> wrapped = ManagedExecutors.managedTask(task, taskListener);
        ManagedTask managedTask = (ManagedTask) wrapped;
        assertTrue(taskListener == managedTask.getManagedTaskListener());

        assertEquals(RESULT, wrapped.call());
    }

    /**
     * Basic test for ManagedExecutors.managedTask(Callable, Map, ManagedTaskListener)
     */
    @Test
    public void testManagedTask_Callable_executionProperties_ManagedTaskListener() throws Exception {
        final String RESULT = "result";
        CallableImpl<String> task = new CallableImpl<String>(RESULT);
        ManagedTaskListenerImpl taskListener = new ManagedTaskListenerImpl();
        Map<String, String> executionProperties = new HashMap<String, String>();
        final String TASK_NAME = "task1";
        executionProperties.put(ManagedTask.IDENTITY_NAME, TASK_NAME);
        executionProperties.put(ManagedTask.LONGRUNNING_HINT, "true");
        
        Callable<String> wrapped = ManagedExecutors.managedTask(task, executionProperties, taskListener);
        ManagedTask managedTask = (ManagedTask) wrapped;
        assertTrue(taskListener == managedTask.getManagedTaskListener());
        assertEquals("true", managedTask.getExecutionProperties().get(ManagedTask.LONGRUNNING_HINT));
        assertEquals(TASK_NAME, managedTask.getExecutionProperties().get(ManagedTask.IDENTITY_NAME));
        
        assertEquals(RESULT, wrapped.call());
    }

    /**
     * Test for ManagedExecutors.managedTask(Callable, Map, ManagedTaskListener)
     * but task already implements ManagedTask, and both executionProerties and
     * taskListeners were passed to managedTask().
     */
    @Test
    public void testManagedTask_Callable_ManagedTask() {
        final String RESULT = "result";
        ManagedTaskListenerImpl TASK_LISTENER = new ManagedTaskListenerImpl();
        Map<String, String> EXEC_PROPERTIES = new HashMap<String, String>();
        EXEC_PROPERTIES.put("custom", "true");
        EXEC_PROPERTIES.put(ManagedTask.LONGRUNNING_HINT, "false");
        final String TASK_DESCRIPTION = "task1 description";
        ManagedTaskCallableImpl<String> task = new ManagedTaskCallableImpl(RESULT, TASK_DESCRIPTION, EXEC_PROPERTIES, TASK_LISTENER);

        ManagedTaskListenerImpl taskListener = new ManagedTaskListenerImpl();
        Map<String, String> executionProperties = new HashMap<String, String>();
        final String TASK_NAME = "task1";
        executionProperties.put(ManagedTask.IDENTITY_NAME, TASK_NAME);
        executionProperties.put(ManagedTask.LONGRUNNING_HINT, "true");
        
        Callable<String> wrapped = ManagedExecutors.managedTask(task, executionProperties, taskListener);
        ManagedTask managedTask = (ManagedTask) wrapped;
        assertTrue(taskListener == managedTask.getManagedTaskListener());
        assertEquals("true", managedTask.getExecutionProperties().get(ManagedTask.LONGRUNNING_HINT));
        assertEquals(TASK_NAME, managedTask.getExecutionProperties().get(ManagedTask.IDENTITY_NAME));
        assertEquals("true", managedTask.getExecutionProperties().get("custom"));
    }

    /**
     * Test for ManagedExecutors.managedTask(Callable, Map, ManagedTaskListener)
     * but task already implements ManagedTask, and both executionProerties and
     * taskListeners passed to managedTask() were null.
     */
    @Test
    public void testManagedTask_Callable_ManagedTask_null_args() {
        final String RESULT = "result";
        ManagedTaskListenerImpl TASK_LISTENER = new ManagedTaskListenerImpl();
        Map<String, String> EXEC_PROPERTIES = new HashMap<String, String>();
        EXEC_PROPERTIES.put("custom", "true");
        final String TASK_DESCRIPTION = "task1 description";
        ManagedTaskCallableImpl<String> task = new ManagedTaskCallableImpl(RESULT, TASK_DESCRIPTION, EXEC_PROPERTIES, TASK_LISTENER);
        
        Callable wrapped = ManagedExecutors.managedTask(task, null, null);
        ManagedTask managedTask = (ManagedTask) wrapped;
        assertTrue(TASK_LISTENER == managedTask.getManagedTaskListener());
        assertEquals("true", managedTask.getExecutionProperties().get("custom"));
    }

    @Test (expected = IllegalArgumentException.class)
    public void testManagedTask_null_Runnable_task() {
        Runnable task = null;
        ManagedExecutors.managedTask(task, new ManagedTaskListenerImpl());
    }

    @Test (expected = IllegalArgumentException.class)
    public void testManagedTask_null_Runnable_task_2() {
        Runnable task = null;
        ManagedExecutors.managedTask(task, new HashMap<String, String>(), new ManagedTaskListenerImpl());
    }

    @Test (expected = IllegalArgumentException.class)
    public void testManagedTask_null_Callable_task() {
        Callable<?> task = null;
        ManagedExecutors.managedTask(task, new ManagedTaskListenerImpl());
    }

    @Test (expected = IllegalArgumentException.class)
    public void testManagedTask_null_Callable_task_2() {
        Callable<?> task = null;
        ManagedExecutors.managedTask(task, new HashMap<String, String>(), new ManagedTaskListenerImpl());
    }

    static class RunnableImpl implements Runnable {

        boolean ran = false;
        
        @Override
        public void run() {
            ran = true;
        }
        
    }
    
    static class ManagedTaskRunnableImpl extends RunnableImpl implements ManagedTask {

        final String description;
        final ManagedTaskListener taskListener;
        final Map<String, String> executionProperties;

        public ManagedTaskRunnableImpl(String description, Map<String, String> executionProperties, ManagedTaskListener taskListener) {
            this.description = description;
            this.taskListener = taskListener;
            this.executionProperties = executionProperties;
        }
        
        public String getIdentityDescription(Locale locale) {
            return description;
        }

        @Override
        public ManagedTaskListener getManagedTaskListener() {
            return taskListener;
        }

        @Override
        public Map<String, String> getExecutionProperties() {
            return executionProperties;
        }
        
    }

    static class CallableImpl<V> implements Callable<V> {

        V result;

        public CallableImpl(V result) {
            this.result = result;
        }
        
        @Override
        public V call() throws Exception {
            return result;
        }
        
    }
    
    static class ManagedTaskCallableImpl<V> extends CallableImpl<V> implements ManagedTask {

        final String description;
        final ManagedTaskListener taskListener;
        final Map<String, String> executionProperties;

        public ManagedTaskCallableImpl(V result, String description, Map<String, String> executionProperties, ManagedTaskListener taskListener) {
            super(result);
            this.description = description;
            this.taskListener = taskListener;
            this.executionProperties = executionProperties;
        }
        
        public String getIdentityDescription(Locale locale) {
            return description;
        }

        @Override
        public ManagedTaskListener getManagedTaskListener() {
            return taskListener;
        }

        @Override
        public Map<String, String> getExecutionProperties() {
            return executionProperties;
        }
        
    }

    static class ManagedTaskListenerImpl implements ManagedTaskListener {

        @Override
        public void taskSubmitted(Future<?> future, ManagedExecutorService executor, Object task) {
        }

        @Override
        public void taskAborted(Future<?> future, ManagedExecutorService executor, Object task, Throwable exception) {
        }

        @Override
        public void taskDone(Future<?> future, ManagedExecutorService executor, Object task, Throwable exception) {
        }

        @Override
        public void taskStarting(Future<?> future, ManagedExecutorService executor, Object task) {
        }
        
    }
}
