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

import java.util.Map;

/**
 * A task submitted to an {@link ManagedExecutorService} or 
 * {@link ManagedScheduledExecutorService} can optionally implement this 
 * interface to provide identifying information about the task, to provide
 * a {@link ManagedTaskListener} to get notification of lifecycle events of
 * the task, or to provide additional execution properties.
 * <p>
 * See also {@link ManagedExecutors#managedTask(java.util.concurrent.Callable, java.util.Map, javax.enterprise.concurrent.ManagedTaskListener) ManagedExecutors.managedTask()}.
 * <p>
 * 
 * @since 1.0
 */
public interface ManagedTask {

  /**
   * Execution property to be returned in {@link #getExecutionProperties()} or
   * {@link ContextService#createContextualProxy(java.lang.Object, java.util.Map, java.lang.Class) ContextService.createContextualProxy()}
   * to provide hint about whether the task could take a long time to complete.
   * Jakarta&trade; EE Product Providers may make use of this hint value to 
   * decide how to allocate thread resource for running this task.
   * Valid values are "true" or "false".
   */
  public static final String LONGRUNNING_HINT = "javax.enterprise.concurrent.LONGRUNNING_HINT";
  
  /**
   * Execution property to be returned in {@link #getExecutionProperties()} or
   * {@link ContextService#createContextualProxy(java.lang.Object, java.util.Map, java.lang.Class) ContextService.createContextualProxy()}
   * to inform the Jakarta EE Product Provider under which transaction 
   * should the task or proxy method of contextual proxy object be executed
   * in.
   * 
   * Valid values are:
   * <p>
   * "SUSPEND" (the default if unspecified) - Any transaction that is currently
   * active on the thread will be suspended and a 
   * {@link javax.transaction.UserTransaction} (accessible in the local 
   * JNDI namespace as "java:comp/UserTransaction") will be available. The 
   * original transaction, if any was active on the thread, will be resumed
   * when the task or contextual proxy object method returns.
   * 
   * <p>
   * "USE_TRANSACTION_OF_EXECUTION_THREAD" - The contextual proxy object method
   * will run within the transaction (if any) of the execution thread. A
   * {@link javax.transaction.UserTransaction} will only be available if it is 
   * also available in the execution thread (for example, when the proxy method
   * is invoked from a Servlet or Bean Managed Transaction). When there is
   * no existing transaction on the execution thread, such as when running tasks
   * that are submitted to a {@link ManagedExecutorService} or a
   * {@link ManagedScheduledExecutorService}, a 
   * {@link javax.transaction.UserTransaction} will be available.
   * <P>
   */
  public static final String TRANSACTION = "javax.enterprise.concurrent.TRANSACTION";

  /**
   * Constant for the "SUSPEND" value of the TRANSACTION execution property.
   * See {@link ManagedTask#TRANSACTION}.
   */
  public static final String SUSPEND = "SUSPEND";
  
  /**
   * Constant for the "USE_TRANSACTION_OF_EXECUTION_THREAD" value of the 
   * TRANSACTION execution property.
   * See {@link ManagedTask#TRANSACTION}.
   */
  public static final String USE_TRANSACTION_OF_EXECUTION_THREAD = "USE_TRANSACTION_OF_EXECUTION_THREAD";
  
  /**
   * Execution property to be returned in {@link #getExecutionProperties()} or
   * {@link ContextService#createContextualProxy(java.lang.Object, java.util.Map, java.lang.Class) ContextService.createContextualProxy()}
   * to provide a String that identifies the task. It may be the name or ID that
   * allow management facilities to inspect the task to determine the intent 
   * of the task and its state. Implementations should not depend upon 
   * any thread execution context and should typically return only 
   * readily-available instance data to identify the task.
   */
  public static final String IDENTITY_NAME = "javax.enterprise.concurrent.IDENTITY_NAME";
  
  /**
   * The {@link ManagedTaskListener} to receive notification of lifecycle
   * events of this task.
   * 
   * @return The {@link ManagedTaskListener} to receive notification of 
   * lifecycle events of this task, or null if it is not necessary to get
   * notified of such events.
   */
  public ManagedTaskListener getManagedTaskListener();
  
  /**
   * Provides additional information to the {@link ManagedExecutorService} or
   * {@link ManagedScheduledExecutorService} when executing this task.<p>
   * 
   * Some standard property keys are defined in this class. 
   * Custom property keys may be defined but must not begin with 
   * "javax.enterprise.concurrent.".
   * 
   * @return A Map&lt;String, String&gt; containing additional execution properties, or
   * null if no additional information is provided for this task.
   */
  public Map<String, String> getExecutionProperties();
}
