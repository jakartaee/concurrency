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

package javax.enterprise.concurrent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Utility methods for classes defined in this package.
 *
 * @since 1.0
 */
public class ManagedExecutors {
   
    /**
     * Not suppose to create instances of this class
     */
    private ManagedExecutors() {};
    
    final static String NULL_TASK_ERROR_MSG = "Task cannot be null";
    
    /**
     * Utility method for checking the {@code isShutdown()} value of the current 
     * thread if it is a {@link ManageableThread} created from  
     * {@link ManagedThreadFactory#newThread(java.lang.Runnable) ManagedThreadFactory.newThread() }.
     * 
     * @return Returns the {@code isShutdown()} value if the current thread is a
     *     {@code ManageableThread} created by {@code ManagedThreadFactory}, or
     *     false if the current thread is not a {@code ManageableThread}.
     * 
     */
    public static boolean isCurrentThreadShutdown() {
        Thread currThread = Thread.currentThread();
        if (currThread instanceof ManageableThread) {
            return ((ManageableThread) currThread).isShutdown();
        }
        return false;
    }
    
    /**
     * Returns a {@link Runnable} object that also implements {@link ManagedTask}
     * interface so it can receive notification of lifecycle events with the
     * provided {@link ManagedTaskListener} when the task is submitted 
     * to a {@link ManagedExecutorService} or a {@link ManagedScheduledExecutorService}.
     * <p>
     * Example:
     * <pre>
     * Runnable task = ...;
     * ManagedTaskListener myTaskListener = ...;
     * ManagedExecutorService executor = ...;
     * 
     * Runnable taskWithListener = ManagedExecutors.managedTask(task, myTaskListener);
     * executor.submit(taskWithListener);
     * </pre>
     * @param task the task to have the given ManagedTaskListener associated with
     * @param taskListener (optional) the {@code ManagedTaskListener} to receive  
     * lifecycle events notification when the task is submitted. If {@code task} 
     * implements {@code ManagedTask}, and {@code taskListener} is not 
     * {@code null}, the {@code ManagedTaskListener} interface methods of the 
     * task will not be called.
     * @return a Runnable object
     * @throws IllegalArgumentException if {@code task} is {@code null}
     */
    public static Runnable managedTask(Runnable task, ManagedTaskListener taskListener)
        throws IllegalArgumentException {
        return managedTask(task, null, taskListener);
    }
    
    /**
     * Returns a {@link Runnable} object that also implements {@link ManagedTask}
     * interface so it can receive notification of lifecycle events with the
     * provided {@link ManagedTaskListener} and to provide additional execution 
     * properties when the task is submitted to a {@link ManagedExecutorService} or a 
     * {@link ManagedScheduledExecutorService}.
     * 
     * @param task the task to have the given ManagedTaskListener associated with
     * @param taskListener (optional) the {@code ManagedTaskListener} to receive  
     * lifecycle events notification when the task is submitted. If {@code task} 
     * implements {@code ManagedTask}, and {@code taskListener} is not 
     * {@code null}, the {@code ManagedTaskListener} interface methods of the 
     * task will not be called.
     * @param executionProperties (optional) execution properties to provide additional hints
     * to {@link ManagedExecutorService} or {@link ManagedScheduledExecutorService}
     * when the task is submitted. 
     * If {@code task} implements {@code ManagedTask} with non-empty 
     * execution properties, the {@code Runnable} returned will contain the union
     * of the execution properties specified in the {@code task} and the {@code executionProperties} 
     * argument, with the latter taking precedence if the same property key is 
     * specified in both.
     * After the method is called, further changes to the {@code Map} 
     * object will not be reflected in the {@code Runnable} returned by this method.
     * @return a Runnable object
     * @throws IllegalArgumentException if {@code task} is {@code null}
     */
    public static Runnable managedTask(Runnable task, Map<String, String> executionProperties, ManagedTaskListener taskListener)
        throws IllegalArgumentException {
        if (task == null) {
          throw new IllegalArgumentException(NULL_TASK_ERROR_MSG);
        }
        return new RunnableAdapter(task, executionProperties, taskListener);
    }
    
    /**
     * Returns a {@link Callable} object that also implements {@link ManagedTask}
     * interface so it can receive notification of lifecycle events with the
     * provided {@link ManagedTaskListener} when the task is submitted 
     * to a {@link ManagedExecutorService} or a {@link ManagedScheduledExecutorService}.
     * 
     * @param task the task to have the given ManagedTaskListener associated with
     * @param taskListener (optional) the {@code ManagedTaskListener} to receive  
     * lifecycle events notification when the task is submitted. If {@code task} 
     * implements {@code ManagedTask}, and {@code taskListener} is not 
     * {@code null}, the {@code ManagedTaskListener} interface methods of the 
     * task will not be called.
     * @return a Callable object
     * @throws IllegalArgumentException if {@code task} is {@code null}
     */
    public static <V> Callable<V> managedTask(Callable<V> task, ManagedTaskListener taskListener) 
        throws IllegalArgumentException {
        return managedTask(task, null, taskListener);
    }
    
    /**
     * Returns a {@link Callable} object that also implements {@link ManagedTask}
     * interface so it can receive notification of lifecycle events with the
     * provided {@link ManagedTaskListener} and to provide additional execution 
     * properties when the task is submitted to a {@link ManagedExecutorService} or a 
     * {@link ManagedScheduledExecutorService}.
     * 
     * @param task the task to have the given ManagedTaskListener associated with
     * @param taskListener (optional) the {@code ManagedTaskListener} to receive  
     * lifecycle events notification when the task is submitted. If {@code task} 
     * implements {@code ManagedTask}, and {@code taskListener} is not 
     * {@code null}, the {@code ManagedTaskListener} interface methods of the 
     * task will not be called.
     * @param executionProperties (optional) execution properties to provide additional hints
     * to {@link ManagedExecutorService} or {@link ManagedScheduledExecutorService}
     * when the task is submitted. 
     * If {@code task} implements {@code ManagedTask} with non-empty 
     * execution properties, the {@code Runnable} returned will contain the union
     * of the execution properties specified in the {@code task} and the {@code executionProperties} 
     * argument, with the latter taking precedence if the same property key is 
     * specified in both.
     * After the method is called, further changes to the {@code Map} 
     * object will not be reflected in the {@code Callable} returned by this method.
     * @return a Callable object
     * @throws IllegalArgumentException if {@code task} is {@code null}
     */
    public static <V> Callable<V> managedTask(Callable<V> task, Map<String, String> executionProperties, ManagedTaskListener taskListener) 
        throws IllegalArgumentException {
        if (task == null) {
          throw new IllegalArgumentException(NULL_TASK_ERROR_MSG);
        }
        return new CallableAdapter(task, executionProperties, taskListener);
    }
    
    /**
     * Adapter for Runnable to include ManagedTask interface methods
     */
    static final class RunnableAdapter extends Adapter implements Runnable {

        final Runnable task;

        public RunnableAdapter(Runnable task, Map<String, String> executionProperties, ManagedTaskListener taskListener) {
            super(taskListener, executionProperties, 
                    task instanceof ManagedTask? (ManagedTask)task: null);
            this.task = task;
        }
        
        @Override
        public void run() {
            task.run();
        }

    }

    /**
     * Adapter for Callable to include ManagedTask interface methods
     */
    static final class CallableAdapter<V> extends Adapter implements Callable<V> {

        final Callable<V> task;

        public CallableAdapter(Callable<V> task, Map<String, String> executionProperties, ManagedTaskListener taskListener) {
            super(taskListener, executionProperties, 
                    task instanceof ManagedTask? (ManagedTask)task: null);
            this.task = task;
        }
        
        @Override
        public V call() throws Exception {
            return task.call();
        }

    }
    
    static class Adapter implements ManagedTask {

        protected final ManagedTaskListener taskListener;
        protected final Map<String, String> executionProperties;
        // If the Runnable or Callable to be wrapped also implements the
        // ManagedTask interface, save it here to provide pieces of information
        // that are not supplied by the provided ManagedTaskListener or
        // executionProperties.
        protected final ManagedTask managedTask;

        public Adapter(ManagedTaskListener taskListener, Map<String, String> executionProperties, ManagedTask managedTask) {
            this.taskListener = taskListener;
            this.managedTask = managedTask;
            this.executionProperties = 
               initExecutionProperties(managedTask == null? null: managedTask.getExecutionProperties(), 
                                       executionProperties);
        }
        
        @Override
        public ManagedTaskListener getManagedTaskListener() {
            if (taskListener != null) {
                return taskListener;
            }
            if (managedTask != null) {
                return managedTask.getManagedTaskListener();
            }
            return null;
        }

        @Override
        public Map<String, String> getExecutionProperties() {
            if (executionProperties != null) {
                return executionProperties;
            }
            return null;
        }
        
        private Map<String, String> initExecutionProperties(Map<String, String> base, Map<String, String> override) {
            if (base == null && override == null) {
                return null;
            }
            Map<String, String> props = new HashMap<String, String>();
            if (base != null) {
                props.putAll(base);
            }
            if (override != null) {
                props.putAll(override);
            }
            return props;
        }
        
    }
}
