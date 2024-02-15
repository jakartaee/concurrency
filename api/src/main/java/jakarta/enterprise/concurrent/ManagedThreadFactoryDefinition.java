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
 * <p>Defines {@link ManagedThreadFactory} instances
 * to be injected into
 * {@link ManagedThreadFactory} injection points
 * including any required {@link Qualifier} annotations specified by {@link #qualifiers()}
 * and registered in JNDI by the container
 * under the JNDI name that is specified in the
 * {@link #name()} attribute.</p>
 *
 * <p>Application components can refer to this JNDI name in the
 * {@link jakarta.annotation.Resource#lookup() lookup} attribute of a
 * {@link jakarta.annotation.Resource} annotation,</p>
 *
 * <pre>
 * {@literal @}ManagedThreadFactoryDefinition(
 *     name = "java:global/concurrent/MyThreadFactory",
 *     qualifiers = MyQualifier.class,
 *     context = "java:global/concurrent/MyThreadFactoryContext",
 *     priority = 4)
 * {@literal @}ContextServiceDefinition(
 *     name = "java:global/concurrent/MyThreadFactoryContext",
 *     propagated = APPLICATION)
 * public class MyServlet extends HttpServlet {
 *     {@literal @}Inject
 *     {@literal @}MyQualifier
 *     ManagedThreadFactory myThreadFactory1;
 *
 *     {@literal @}Resource(lookup = "java:global/concurrent/MyThreadFactory",
 *               name = "java:module/concurrent/env/MyThreadFactoryRef")
 *     ManagedThreadFactory myThreadFactory2;
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
 *    &lt;resource-env-ref-name&gt;java:module/env/concurrent/MyThreadFactoryRef&lt;/resource-env-ref-name&gt;
 *    &lt;resource-env-ref-type&gt;jakarta.enterprise.concurrent.ManagedThreadFactory&lt;/resource-env-ref-type&gt;
 *    &lt;lookup-name&gt;java:global/concurrent/MyThreadFactory&lt;/lookup-name&gt;
 * &lt;/resource-env-ref&gt;
 * </pre>
 *
 * You can also define a {@code ManagedThreadFactory} with the
 * {@code <managed-thread-factory>} deployment descriptor element.
 * For example,
 *
 * <pre>
 * &lt;managed-thread-factory&gt;
 *    &lt;name&gt;java:global/concurrent/MyThreadFactory&lt;/name&gt;
 *    &lt;context-service-ref&gt;java:global/concurrent/MyThreadFactoryContext&lt;/context-service-ref&gt;
 *    &lt;priority&gt;4&lt;/priority&gt;
 * &lt;/managed-thread-factory&gt;
 * </pre>
 *
 * If a {@code managed-thread-factory} and {@code ManagedThreadFactoryDefinition}
 * have the same name, their attributes are merged to define a single
 * {@code ManagedThreadFactory} definition, with each attribute that is specified
 * in the {@code managed-thread-factory} deployment descriptor entry taking
 * precedence over the corresponding attribute of the annotation.
 * If any qualifier elements are specified, the set of qualifier elements
 * replaces the qualifiers attribute of the annotation.
 *
 * @since 3.0
 */
@Repeatable(ManagedThreadFactoryDefinition.List.class)
@Retention(RUNTIME)
@Target(TYPE)
public @interface ManagedThreadFactoryDefinition {
    /**
     * JNDI name of the {@link ManagedThreadFactory} instance.
     * The JNDI name must be in a valid Jakarta EE namespace,
     * such as,
     * <ul>
     * <li>java:comp</li>
     * <li>java:module</li>
     * <li>java:app</li>
     * <li>java:global</li>
     * </ul>
     *
     * @return <code>ManagedThreadFactory</code> JNDI name.
     */
    String name();

    /**
     * <p>List of required {@link Qualifier qualifier annotations}.</p>
     *
     * <p>A {@link ManagedThreadFactory} injection point
     * with these qualifier annotations injects a bean that is
     * produced by this {@code ManagedThreadFactoryDefinition}.</p>
     *
     * <p>The default value is an empty list, indicating that this
     * {@code ManagedThreadFactoryDefinition} does not automatically produce
     * bean instances for any injection points.</p>
     *
     * <p>When the qualifiers list is non-empty, the container creates
     * a {@link ManagedThreadFactory} instance and registers
     * an {@link ApplicationScoped} bean for it with the specified
     * required qualifiers and required type of {@code ManagedThreadFactory}.
     * The life cycle of the bean aligns with the life cycle of the application
     * and the bean is not accessible from outside of the application.
     * Applications must not configure a {@code java:global} {@link #name() name}
     * if also configuring a non-empty list of qualifiers.</p>
     *
     * <p>Applications can define their own {@link Producer Producers}
     * for {@link ManagedThreadFactory} injection points as long as the
     * qualifier annotations on the producer do not conflict with the
     * non-empty {@link #qualifiers()} list of a
     * {@code ManagedThreadFactoryDefinition}.</p>
     *
     * @return list of qualifiers.
     * @since 3.1
     */
    Class<?>[] qualifiers() default {};

    /**
     * Determines how context is applied to threads from this
     * thread factory.
     * <p>
     * The name can be the name of a {@link ContextServiceDefinition} or
     * the name of a {@code context-service} deployment descriptor element
     * or the JNDI name of the Jakarta EE default {@code ContextService}
     * instance, {@code java:comp/DefaultContextService}.
     * <p>
     * The name of the {@code ContextService} must be no more granular
     * than the name of this {@code ManagedThreadFactoryDefinition}. For example,
     * if this {@code ManagedThreadFactoryDefinition} has a name in {@code java:app},
     * the {@code ContextService} can be in {@code java:app} or {@code java:global},
     * but not in {@code java:module} which would be ambiguous as to which
     * module's {@code ContextService} definition should be used.
     * <p>
     * The default value, {@code java:comp/DefaultContextService}, is the
     * JNDI name of the Jakarta EE default {@code ContextService}.
     *
     * @return instructions for capturing and propagating or clearing context.
     */
    String context() default "java:comp/DefaultContextService";

    /**
     * <p>Priority for platform threads created by this thread factory.
     * Virtual threads always have the priority {@link java.lang.Thread#NORM_PRIORITY}
     * regardless of this setting.</p>
     *
     * <p>The default is {@link java.lang.Thread#NORM_PRIORITY}.</p>
     *
     * @return the priority for new threads.
     */
    int priority() default Thread.NORM_PRIORITY;

    // TODO switch the link below back to
    //      {@link Thread#isVirtual() virtual} threads
    //      instead of
    //      virtual {@link Thread threads}
    //      once this project compiles against Java SE 21 again.
    /**
     * <p>Indicates whether this thread factory is requested to
     * create virtual {@link Thread threads}.
     * Virtual threads are discussed in the {@link Thread} JavaDoc
     * under the section that is titled <i><b>Virtual threads</b></i>.</p>
     *
     * <p>When {@code true}, the thread factory can create
     * virtual threads if it is capable of doing so
     * and if the request is not overridden by vendor-specific
     * configuration that restricts the use of virtual threads.</p>
     *
     * <p>The default is {@code false}, indicating that the
     * thread factory must not create virtual threads.
     * When {@code false}, the thread factory always creates
     * platform threads.</p>
     *
     * <p>When running on Java SE 17, the {@code true} value
     * behaves the same as the {@code false} value and results in
     * platform threads being created rather than virtual threads.
     * </p>
     *
     * @return {@code true} if the thread factory is requested to
     *         create virtual threads, otherwise {@code false}.
     * @since 3.1
     */
    boolean virtual() default false;

    /**
     * Enables multiple <code>ManagedThreadFactoryDefinition</code>
     * annotations on the same type.
     */
    @Retention(RUNTIME)
    @Target(TYPE)
    public @interface List {
        ManagedThreadFactoryDefinition[] value();
    }
}
