/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
package jakarta.enterprise.concurrent.spi;

import java.util.Map;

/**
 * Third party providers of thread context implement this interface to
 * participate in thread context capture and propagation.
 * <p>
 * Application code must never access the classes within this {@code spi}
 * package. Instead, application code uses the various interfaces that are defined
 * by the Jakarta Concurrency specification, such as
 * {@link jakarta.enterprise.concurrent.ManagedExecutorService ManagedExecutorService}
 * and {@link jakarta.enterprise.concurrent.ContextService ContextService}.
 * <p>
 * The {@code ThreadContextProvider} implementation and related classes are
 * packaged within the third party provider's JAR file. The implementation is made
 * discoverable via the {@link java.util.ServiceLoader ServiceLoader} mechanism. The JAR file
 * that packages it must include a file with the following name and location,
 * <p>
 * {@code META-INF/services/jakarta.enterprise.concurrent.spi.ThreadContextProvider}
 * <p>
 * The content of the aforementioned file must be one or more lines, each specifying
 * the fully qualified name of a {@code ThreadContextProvider} implementation
 * that is provided within the JAR file.
 * <p>
 * The Jakarta EE Product Provider must use the
 * {@code ServiceLoader} to identify all available implementations of
 * {@code ThreadContextProvider} that can participate in thread context capture
 * and propagation and must invoke them either to capture current thread context or establish
 * default thread context per the configuration of the
 * {@link jakarta.enterprise.concurrent.ContextServiceDefinition ContextServiceDefinition},
 * or vendor-specific configuration, and execution properties such as
 * {@link jakarta.enterprise.concurrent.ManagedTask#TRANSACTION ManagedTask.TRANSACTION}
 * that override context propagation configuration.
 *
 * @since 3.0
 */
public interface ThreadContextProvider {
    /**
     * Captures from the current thread a snapshot of the provided thread context type.
     *
     * @param props execution properties, which are optionally provided
     *        by some types of tasks and contextual proxies.
     *        Thread context providers that do not supply or use execution properties
     *        can ignore this parameter.
     * @return immutable snapshot of the provided type of context, captured from the
     *         current thread.
     */
    public ThreadContextSnapshot currentContext(Map<String, String> props);

    /**
     * Returns empty/cleared context of the provided type. This context is not
     * captured from the current thread, but instead represents the behavior that you
     * get for this context type when no particular context has been applied to the
     * thread.
     * <p>
     * This is used in cases where the provided type of thread context should not be
     * propagated from the requesting thread or inherited from the thread of execution,
     * in which case it is necessary to establish an empty/cleared context in its place,
     * so that an action does not unintentionally inherit context of the thread that
     * happens to run it.
     * <p>
     * For example, a security context provider's empty/cleared context ensures there
     * is no authenticated user on the thread. A transaction context provider's
     * empty/cleared context ensures that any active transaction is suspended.
     * And so forth.
     *
     * @param props execution properties, which are optionally provided
     *        by some types of tasks and contextual proxies.
     *        Thread context providers that do not supply or use execution properties
     *        can ignore this parameter.
     * @return immutable empty/default context of the provided type.
     */
    public ThreadContextSnapshot clearedContext(Map<String, String> props);

    /**
     * Returns a human readable identifier for the type of thread context that is
     * captured by this {@code ThreadContextProvider} implementation.
     * <p>
     * To ensure portability of applications, this is typically be a keyword that
     * is defined by the same specification that defines the thread context type.
     * <p>
     * {@link jakarta.enterprise.concurrent.ContextServiceDefinition ContextServiceDefinition}
     * defines identifiers for built-in thread context types, including
     * {@link jakarta.enterprise.concurrent.ContextServiceDefinition#APPLICATION Application},
     * {@link jakarta.enterprise.concurrent.ContextServiceDefinition#SECURITY Security}, and
     * {@link jakarta.enterprise.concurrent.ContextServiceDefinition#TRANSACTION Transaction},
     * as well as the
     * {@link jakarta.enterprise.concurrent.ContextServiceDefinition#ALL_REMAINING Remaining}
     * identifer which covers all remaining context.
     * These identifiers must not be returned from this method.
     * <p>
     * Applications use a combination of built-in identifiers and those that are
     * defined by other specifications and third-party context
     * types when configuring a {@code ContextServiceDefinition}
     * to capture and propagate only specific types of thread context.
     * <p>
     * For example:
     * <pre>
     * {@code @ContextServiceDefinition(}
     *    name = "java:module/concurrent/MyCustomContext",
     *    propagated = MyCustomContextProvider.CONTEXT_NAME,
     *    cleared = { ContextServiceDefinition.SECURITY, ContextServiceDefinition.TRANSACTION },
     *    unchanged = ContextServiceDefinition.ALL_REMAINING)
     * </pre>
     * <p>
     * It is an error for multiple thread context providers of an identical type to be
     * simultaneously available.
     *
     * @return identifier for the provided type of thread context.
     */
    public String getThreadContextType();
}
