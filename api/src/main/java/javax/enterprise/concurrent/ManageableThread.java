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

/**
 *
 * Interface to be implemented by the Jakarta&trade; EE product providers on threads
 * that are created by calling 
 * {@link ManagedThreadFactory#newThread(java.lang.Runnable) }.
 * 
 * @since 1.0
 */
public interface ManageableThread {

    /**
     * This method is used by the application component provider to check 
     * whether a thread created by the {@code newThread} method of 
     * {@link ManagedThreadFactory} has been marked for shut down. 
     * If the value is true, the application component provider should finish
     * any work on this thread as soon as possible.
     * 
     * @return true if this thread has been marked for shutdown.
     */
    public boolean isShutdown();
}
