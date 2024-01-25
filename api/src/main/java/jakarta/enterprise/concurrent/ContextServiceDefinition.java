/*
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.Producer;
import jakarta.inject.Qualifier;

/**
 * <p>Defines a {@link ContextService}
 * to be injected into
 * {@link ContextService} injection points
 * including any required {@link Qualifier} annotations specified by {@link #qualifiers()}
 * and registered in JNDI by the container
 * under the JNDI name that is specified in the
 * {@link #name()} attribute.</p>
 *
 * <p>Application components can refer to this JNDI name in the
 * {@link jakarta.annotation.Resource#lookup() lookup} attribute of a
 * {@link jakarta.annotation.Resource} annotation,</p>
 *
 * <pre>{@literal @}ContextServiceDefinition(
 *     name = "java:app/concurrent/MyContext",
 *     qualifiers = MyQualifier.class,
 *     propagated = APPLICATION,
 *     unchanged = TRANSACTION,
 *     cleared = ALL_REMAINING)
 * public class MyServlet extends HttpServlet {
 *     {@literal @}Inject
 *     {@literal @}MyQualifier
 *     ConetxtService appContextSvc1;
 *
 *     {@literal @}Resource(lookup = "java:app/concurrent/MyContext",
 *               name = "java:app/concurrent/env/MyContextRef")
 *     ContextService appContextSvc2;
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
 *    &lt;resource-env-ref-name&gt;java:app/env/concurrent/MyContextRef&lt;/resource-env-ref-name&gt;
 *    &lt;resource-env-ref-type&gt;jakarta.enterprise.concurrent.ContextService&lt;/resource-env-ref-type&gt;
 *    &lt;lookup-name&gt;java:app/concurrent/MyContext&lt;/lookup-name&gt;
 * &lt;/resource-env-ref&gt;
 * </pre>
 *
 * <p>The {@link #cleared()}, {@link #propagated()}, and {@link #unchanged()}
 * attributes enable the application to configure how thread context
 * is applied to tasks and actions that are contextualized by the
 * <code>ContextService</code>.
 * Constants are provided on this class for context types that are
 * defined by the Jakarta EE Concurrency specification.
 * In addition to those constants, a Jakarta EE product provider
 * may choose to accept additional vendor-specific context types.
 * Usage of vendor-specific types will make applications non-portable.</p>
 *
 * <p>Overlap of the same context type across multiple lists is an error and
 * prevents the <code>ContextService</code> instance from being created.
 * If {@link #ALL_REMAINING} is not present in any of the lists, it is
 * implicitly appended to the {@link #cleared()} context types.</p>
 *
 * You can also define a {@code ContextService} with the
 * {@code <context-service>} deployment descriptor element.
 * For example,
 *
 * <pre>
 * &lt;context-service&gt;
 *    &lt;name&gt;java:app/concurrent/MyContext&lt;/name&gt;
 *    &lt;cleared&gt;Security&lt;/cleared&gt;
 *    &lt;cleared&gt;Transaction&lt;/cleared&gt;
 *    &lt;propagated&gt;Application&lt;/propagated&gt;
 *    &lt;unchanged&gt;Remaining&lt;/unchanged&gt;
 * &lt;/context-service&gt;
 * </pre>
 *
 * If a {@code context-service} and {@code ContextServiceDefinition}
 * have the same name, their attributes are merged to define a single
 * {@code ContextService} definition, with each attribute that is specified
 * in the {@code context-service} deployment descriptor entry taking
 * precedence over the corresponding attribute of the annotation.
 * If any qualifier elements are specified, the set of qualifier elements
 * replaces the qualifiers attribute of the annotation.
 *
 * @since 3.0
 */
@Repeatable(ContextServiceDefinition.List.class)
@Retention(RUNTIME)
@Target(TYPE)
public @interface ContextServiceDefinition {
    /**
     * <p>JNDI name of the {@link ContextService} instance being defined.
     * The JNDI name must be in a valid Jakarta EE namespace,
     * such as,</p>
     *
     * <ul>
     * <li>java:comp</li>
     * <li>java:module</li>
     * <li>java:app</li>
     * <li>java:global</li>
     * </ul>
     *
     * @return <code>ContextService</code> JNDI name.
     */
    String name();

    /**
     * <p>List of required {@link Qualifier qualifier annotations}.</p>
     *
     * <p>A {@link ContextService} injection point
     * with these qualifier annotations injects a bean that is
     * produced by this {@code ContextServiceDefinition}.</p>
     *
     * <p>The default value is an empty list, indicating that this
     * {@code ContextServiceDefinition} does not automatically produce
     * bean instances for any injection points.</p>
     *
     * <p>When the qualifiers list is non-empty, the container creates
     * a {@link ContextService} instance and registers
     * an {@link ApplicationScoped} bean for it with the specified
     * required qualifiers and required type of {@code ContextService}.
     * The life cycle of the bean aligns with the life cycle of the application
     * and the bean is not accessible from outside of the application.
     * Applications must not configure a {@code java:global} {@link #name() name}
     * if also configuring a non-empty list of qualifiers.</p>
     *
     * <p>Applications can define their own {@link Producer Producers}
     * for {@link ContextService} injection points as long as the
     * qualifier annotations on the producer do not conflict with the
     * non-empty {@link #qualifiers()} list of a
     * {@code ContextServiceDefinition}.</p>
     *
     * @return list of qualifiers.
     * @since 3.1
     */
    Class<?>[] qualifiers() default {};

    /**
     * <p>Types of context to clear whenever a thread runs the
     * contextual task or action. The thread's previous context
     * is restored afterward.
     *
     * <p>Constants are provided on this class for the context types
     * that are defined by the Jakarta EE Concurrency specification.</p>
     *
     * @return context types to clear.
     */
    String[] cleared() default { TRANSACTION };

    /**
     * <p>Types of context to capture from the requesting thread
     * and propagate to a thread that runs the contextual task
     * or action.
     * The captured context is re-established when threads
     * run the contextual task or action, with the respective
     * thread's previous context being restored afterward.
     *
     * <p>Constants are provided on this class for the context types
     * that are defined by the Jakarta EE Concurrency specification.</p>
     *
     * @return context types to capture and propagate.
     */
    String[] propagated() default { ALL_REMAINING };

    /**
     * <p>Types of context that are left alone when a thread
     * runs the contextual task or action.
     * <p>For example, with <code> unchanged = TRANSACTION</code>
     * if a transaction is started after a function is
     * contextualized, but before the function is run on the same thread,
     * the transaction will be active in the contextual function:</p>
     *
     * <pre>Consumer&lt;String, Integer&gt; updateDB = contextService.contextualConsumer(fn);
     *
     *&#47;/ later, on another thread
     *tx.begin();
     *updateDB.accept("java:comp/env/jdbc/ds1");
     *&#47;/...additional transactional work
     *tx.commit();</pre>
     *
     * <p>Constants are provided on this class for the context types
     * that are defined by the Jakarta EE Concurrency specification.</p>
     *
     * @return context types to leave unchanged.
     */
    String[] unchanged() default {};

    /**
     * <p>All available thread context types that are not specified
     * elsewhere. This includes thread context types from custom
     * {@link jakarta.enterprise.concurrent.spi.ThreadContextProvider ThreadContextProviders}
     * that are not specified elsewhere.</p>
     *
     * <p>For example, to define a <code>ContextService</code> that
     * propagates {@link #SECURITY} context,
     * leaves {@link #TRANSACTION} context alone,
     * and clears every other context type:</p>
     *
     * <pre>{@literal @}ContextServiceDefinition(
     *     name = "java:module/concurrent/SecurityContext",
     *     propagated = SECURITY,
     *     unchanged = TRANSACTION,
     *     cleared = ALL_REMAINING)
     * public class MyServlet extends HttpServlet ...
     * </pre>
     */
    static final String ALL_REMAINING = "Remaining";

    /**
     * <p>Context pertaining to the application component or module,
     * including its Jakarta EE namespace (such as
     * <code>java:comp/env/</code>) and thread context class loader.</p>
     *
     * <p>A cleared application context means that the thread is
     * not associated with any application component and lacks
     * access to the Jakarta EE namespace and thread context class
     * loader of the application.</p>
     */
    static final String APPLICATION = "Application";

    // TODO CDI context is the topic of
    // https://github.com/jakartaee/concurrency/issues/105

    /**
     * <p>Context that controls the credentials that are associated
     * with the thread, including the caller subject and
     * invocation/RunAs subject.</p>
     *
     * <p>A cleared security context gives the thread unauthenticated
     * subjects.</p>
     */
    static final String SECURITY = "Security";

    /**
     * <p>Context that controls the transaction that is associated
     * with the thread.</p>
     *
     * <p>When cleared transaction context is applied to a thread,
     * any global transaction that was previously present there is
     * first suspended such that the contextual task or action can
     * begin and manage, as permitted by the container, its own new
     * {@code jakarta.transaction.UserTransaction}.
     * After the contextual task or action completes, the prior
     * transaction is resumed on the thread. This is equivalent to
     * the execution property, {@link ManagedTask#TRANSACTION} with
     * a value of {@link ManagedTask#SUSPEND}.</p>
     *
     * <p>The execution property, {@link ManagedTask#TRANSACTION},
     * if specified, takes precedence over the behavior for
     * transaction context that is specified on the resource
     * definition annotations.</p>
     *
     * <p>Jakarta EE providers need not support the propagation
     * of transactions to other threads and can reject resource
     * definition annotations that include transaction as a
     * propagated context.</p>
     */
    // TODO the last item above is the topic of
    // https://github.com/jakartaee/concurrency/issues/102
    // and can be updated accordingly when that capability is added.
    static final String TRANSACTION = "Transaction";

    /**
     * Enables multiple <code>ContextServiceDefinition</code>
     * annotations on the same type.
     */
    @Retention(RUNTIME)
    @Target(TYPE)
    public @interface List {
        ContextServiceDefinition[] value();
    }
}
