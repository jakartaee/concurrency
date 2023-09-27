/*
 * Copyright (c) 2021,2023 Contributors to the Eclipse Foundation
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

package jakarta.enterprise.concurrent;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>Defines a {@link ManagedScheduledExecutorService}
 * to be injected into
 * {@link ManagedScheduledExecutorService} injection points
 * with the specified {@link #qualifiers()}
 * and registered in JNDI by the container
 * under the JNDI name that is specified in the
 * {@link #name()} attribute.</p>
 *
 * <p>Application components can refer to this JNDI name in the
 * {@link jakarta.annotation.Resource#lookup() lookup} attribute of a
 * {@link jakarta.annotation.Resource} annotation,</p>
 *
 * <pre>
 * {@literal @}ManagedScheduledExecutorDefinition(
 *     name = "java:comp/concurrent/MyScheduledExecutor",
 *     context = "java:comp/concurrent/MyScheduledExecutorContext",
 *     hungTaskThreshold = 30000,
 *     maxAsync = 3)
 * {@literal @}ContextServiceDefinition(
 *     name = "java:comp/concurrent/MyScheduledExecutorContext",
 *     propagated = APPLICATION)
 * public class MyServlet extends HttpServlet {
 *     {@literal @}Inject
 *     {@literal @}MyQualifier
 *     ManagedScheduledExecutorService myScheduledExecutor1;
 *
 *     {@literal @}Resource(lookup = "java:comp/concurrent/MyScheduledExecutor",
 *               name = "java:comp/concurrent/env/MyScheduledExecutorRef")
 *     ManagedScheduledExecutorService myScheduledExecutor2;
 *     ...
 *
 * {@literal @}Qualifier
 * {@literal @}Retention(RetentionPolicy.RUNTIME)
 * {@literal @}Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE })
 * public {@literal @}interface MyQualifier {}
 * </pre>
 *
 * <p>Resource environment references in a deployment descriptor
 * can similarly specify the <code>lookup-name</code>,</p>
 *
 * <pre>
 * &lt;resource-env-ref&gt;
 *    &lt;resource-env-ref-name&gt;java:comp/env/concurrent/MyScheduledExecutorRef&lt;/resource-env-ref-name&gt;
 *    &lt;resource-env-ref-type&gt;jakarta.enterprise.concurrent.ManagedScheduledExecutorService&lt;/resource-env-ref-type&gt;
 *    &lt;lookup-name&gt;java:comp/concurrent/MyScheduledExecutor&lt;/lookup-name&gt;
 * &lt;/resource-env-ref&gt;
 * </pre>
 *
 * You can also define a {@code ManagedScheduledExecutorService} with the
 * {@code <managed-scheduled-executor>} deployment descriptor element.
 * For example,
 *
 * <pre>
 * &lt;managed-scheduled-executor&gt;
 *    &lt;name&gt;java:module/concurrent/MyExecutor&lt;/name&gt;
 *    &lt;context-service-ref&gt;java:module/concurrent/MyExecutorContext&lt;/context-service-ref&gt;
 *    &lt;hung-task-threshold&gt;120000&lt;/hung-task-threshold&gt;
 *    &lt;max-async&gt;5&lt;/max-async&gt;
 * &lt;/managed-scheduled-executor&gt;
 * </pre>
 *
 * If a {@code managed-scheduled-executor} and {@code ManagedScheduledExecutorDefinition}
 * have the same name, their attributes are merged to define a single
 * {@code ManagedScheduledExecutorService} definition, with each attribute that is specified
 * in the {@code managed-scheduled-executor} deployment descriptor entry taking
 * precedence over the corresponding attribute of the annotation.
 *
 * @since 3.0
 */
@Repeatable(ManagedScheduledExecutorDefinition.List.class)
@Retention(RUNTIME)
@Target(TYPE)
public @interface ManagedScheduledExecutorDefinition {
    /**
     * JNDI name of the {@link ManagedScheduledExecutorService} instance.
     * The JNDI name must be in a valid Jakarta EE namespace,
     * such as,
     * <ul>
     * <li>java:comp</li>
     * <li>java:module</li>
     * <li>java:app</li>
     * <li>java:global</li>
     * </ul>
     *
     * @return <code>ManagedScheduledExecutorService</code> JNDI name.
     */
    String name();

    /**
     * <p>List of {@link Qualifier qualifier annotations}.</p>
     *
     * <p>A {@link ManagedScheduledExecutorService} injection point
     * with these qualifier annotations injects a bean that is
     * produced by this {@code ManagedScheduledExecutorDefinition}.</p>
     *
     * <p>The default value is an empty list, indicating that this
     * {@code ManagedScheduledExecutorDefinition} does not automatically produce
     * bean instances for any injection points.</p>
     *
     * <p>Applications can define their own {@link Producer Producers}
     * for {@link ManagedScheduledExecutorService} injection points as long as the
     * qualifier annotations on the producer do not conflict with the
     * non-empty {@link #qualifiers()} list of a
     * {@code ManagedScheduledExecutorDefinition}.</p>
     *
     * @return list of qualifiers.
     * @since 3.1
     */
    Class<?>[] qualifiers() default {};

    /**
     * The name of a {@link ContextService} instance which
     * determines how context is applied to tasks and actions that
     * run on this executor.
     * <p>
     * The name can be the name of a {@link ContextServiceDefinition} or
     * the name of a {@code context-service} deployment descriptor element
     * or the JNDI name of the Jakarta EE default {@code ContextService}
     * instance, {@code java:comp/DefaultContextService}.
     * <p>
     * The name of the {@code ContextService} must be no more granular
     * than the name of this {@code ManagedScheduledExecutorDefinition}. For example,
     * if this {@code ManagedScheduledExecutorDefinition} has a name in {@code java:app},
     * the {@code ContextService} can be in {@code java:app} or {@code java:global},
     * but not in {@code java:module} which would be ambiguous as to which
     * module's {@code ContextService} definition should be used.
     * <p>
     * The default value, {@code java:comp/DefaultContextService}, is the
     * JNDI name of the Jakarta EE default {@code ContextService}.
     *
     * @return name of the {@code ContextService} for
     *         capturing and propagating or clearing context.
     */
    String context() default "java:comp/DefaultContextService";

    /**
     * <p>The amount of time in milliseconds that a task or action
     * can execute before it is considered hung.</p>
     *
     * <p>The default value of <code>-1</code> indicates unlimited.</p>
     *
     * @return number of milliseconds after which a task or action
     *         is considered hung.
     */
    long hungTaskThreshold() default -1;

    /**
     * <p>Upper bound on contextual tasks and actions that this executor
     * will simultaneously execute asynchronously. This constraint does
     * not apply to tasks and actions that the executor runs inline,
     * such as when a thread requests
     * {@link java.util.concurrent.CompletableFuture#join()} and the
     * action runs inline if it has not yet started.
     * This constraint also does not apply to tasks that are scheduled
     * via the <code>schedule*</code> methods or
     * {@link Asynchronous#runAt()}.</p>
     *
     * <p>The default value of <code>-1</code> indicates unbounded,
     * although still subject to resource constraints of the system.</p>
     *
     * @return upper limit on asynchronous execution.
     */
    int maxAsync() default -1;

    /**
     * Enables multiple <code>ManagedScheduledExecutorDefinition</code>
     * annotations on the same type.
     */
    @Retention(RUNTIME)
    @Target(TYPE)
    public @interface List {
        ManagedScheduledExecutorDefinition[] value();
    }
}
