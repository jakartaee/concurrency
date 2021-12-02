/*
 * Copyright (c) 2010, 2021 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The ContextService provides methods for creating dynamic proxy objects
 * (as defined by {@link java.lang.reflect.Proxy java.lang.reflect.Proxy}).
 * ContextService also creates proxy objects for functional interfaces
 * (such as {@link java.util.function.Function}) that can be used as
 * completion stage actions. Proxy objects run with
 * the addition of context typically associated with applications executing in a
 * Jakarta&trade; EE environment.
 * Examples of such context are classloading, namespace, security, etc.
 * <p>
 *
 * The proxy objects follow the same rules as defined for the
 * {@link java.lang.reflect.Proxy java.lang.reflect.Proxy} class with the following additions:
 * <ul>
 * <li>The proxy instance will retain the context of the creator's
 *     thread.
 * <li>The proxy instance will implement all of the interfaces specified on the
 *     {@code createContextualProxy} methods.
 * <li>The object to have a proxy instance created for should not be a
 *     component managed by the Jakarta EE Product Provider, such as a web
 *     component or a Jakarta Enterprise Bean.
 * <li>All interface method invocations on a proxy instance run in the
 *     creator's context with the exception of {@code hashCode},
 *     {@code equals}, {@code toString} and all other methods declared in
 *     {@link java.lang.Object}.
 * <li>The proxy instance must implement {@link java.io.Serializable}
 *     if the proxied object instance is serializable.
 * <li>The proxied object instance must implement
 *     {@link java.io.Serializable} if the proxy instance is serialized.
 * <li>Execution properties can be stored with the proxy instance. Custom
 *     property keys must not begin with "jakarta.enterprise.concurrent.".
 * <li>Execution properties are to be used for controlling how various contextual
 *     information is retrieved and applied to the thread. Although application
 *     components can store arbitrary property keys and values, it is not
 *     recommended. Jakarta EE product providers may impose limits to the
 *     size of the keys and values.
 * </ul>
 * <p>
 * For example, to contextualize a single completion stage action
 * such that it is able to access the namespace of the application component,
 * <pre>
 * contextSvc = InitialContext.doLookup("java:comp/DefaultContextService");
 * stage2 = stage1.thenApply(contextSvc.contextualFunction(i -&gt; {
 *     DataSource ds = InitialContext.doLookup("java:comp/env/dsRef");
 *     try (Connection con = ds.getConnection()) {
 *         PreparedStatement stmt = con.prepareStatement(sql);
 *         stmt.setInt(1, i);
 *         ResultSet result = stmt.executeQuery();
 *         return result.next() ? result.getInt(1) : 0;
 *     }
 * }));
 * </pre>
 * <p>
 *
 * @since 1.0
 */
public interface ContextService {
  /**
   * <p>Wraps a {@link java.util.concurrent.Callable} with context
   * that is captured from the thread that invokes
   * <code>contextualCallable</code>. Context is captured at the time <code>contextualCallable</code> is invoked.</p>
   *
   * <p>When <code>call</code> is invoked on the proxy instance,
   * context is first established on the thread that will run the <code>call</code> method,
   * then the <code>call</code> method of the provided <code>Callable</code> is invoked.
   * Finally, the previous context is restored on the thread, and the result of the
   * <code>Callable</code> is returned to the invoker.</p>
   *
   * @param <R> callable result type.
   * @param callable instance to contextualize.
   * @return contextualized proxy instance that wraps execution of the <code>call</code> method with context.
   * @throws IllegalArgumentException if an already-contextualized <code>Callable</code> is supplied to this method.
   * @since 3.0
   */
  public <R> Callable<R> contextualCallable(Callable<R> callable);

  /**
   * <p>Wraps a {@link java.util.function.BiConsumer} with context
   * that is captured from the thread that invokes
   * <code>contextualConsumer</code>. Context is captured at the time <code>contextualConsumer</code> is invoked.</p>
   *
   * <p>When <code>accept</code> is invoked on the proxy instance,
   * context is first established on the thread that will run the <code>accept</code> method,
   * then the <code>accept</code> method of the provided <code>BiConsumer</code> is invoked.
   * Finally, the previous context is restored on the thread, and control is returned to the invoker.</p>
   *
   * @param <T> type of first parameter to consumer.
   * @param <U> type of second parameter to consumer.
   * @param consumer instance to contextualize.
   * @return contextualized proxy instance that wraps execution of the <code>accept</code> method with context.
   * @throws IllegalArgumentException if an already-contextualized <code>BiConsumer</code> is supplied to this method.
   * @since 3.0
   */
  public <T, U> BiConsumer<T, U> contextualConsumer(BiConsumer<T, U> consumer);

  /**
   * <p>Wraps a {@link java.util.function.Consumer} with context
   * that is captured from the thread that invokes
   * <code>contextualConsumer</code>. Context is captured at the time <code>contextualConsumer</code> is invoked.</p>
   *
   * <p>When <code>accept</code> is invoked on the proxy instance,
   * context is first established on the thread that will run the <code>accept</code> method,
   * then the <code>accept</code> method of the provided <code>Consumer</code> is invoked.
   * Finally, the previous context is restored on the thread, and control is returned to the invoker.</p>
   *
   * @param <T> type of parameter to consumer.
   * @param consumer instance to contextualize.
   * @return contextualized proxy instance that wraps execution of the <code>accept</code> method with context.
   * @throws IllegalArgumentException if an already-contextualized <code>Consumer</code> is supplied to this method.
   * @since 3.0
   */
  public <T> Consumer<T> contextualConsumer(Consumer<T> consumer);

  /**
   * <p>Wraps a {@link java.util.function.BiFunction} with context
   * that is captured from the thread that invokes
   * <code>contextualFunction</code>. Context is captured at the time <code>contextualFunction</code> is invoked.</p>
   *
   * <p>When <code>apply</code> is invoked on the proxy instance,
   * context is first established on the thread that will run the <code>apply</code> method,
   * then the <code>apply</code> method of the provided <code>BiFunction</code> is invoked.
   * Finally, the previous context is restored on the thread, and the result of the
   * <code>BiFunction</code> is returned to the invoker.</p>
   *
   * @param <T> type of first parameter to function.
   * @param <U> type of second parameter to function.
   * @param <R> function result type.
   * @param function instance to contextualize.
   * @return contextualized proxy instance that wraps execution of the <code>apply</code> method with context.
   * @throws IllegalArgumentException if an already-contextualized <code>BiFunction</code> is supplied to this method.
   * @since 3.0
   */
  public <T, U, R> BiFunction<T, U, R> contextualFunction(BiFunction<T, U, R> function);

  /**
   * <p>Wraps a {@link java.util.function.BiFunction} with context
   * that is captured from the thread that invokes
   * <code>contextualFunction</code>. Context is captured at the time <code>contextualFunction</code> is invoked.</p>
   *
   * <p>When <code>apply</code> is invoked on the proxy instance,
   * context is first established on the thread that will run the <code>apply</code> method,
   * then the <code>apply</code> method of the provided <code>Function</code> is invoked.
   * Finally, the previous context is restored on the thread, and the result of the
   * <code>Function</code> is returned to the invoker.</p>
   *
   * @param <T> type of parameter to function.
   * @param <R> function result type.
   * @param function instance to contextualize.
   * @return contextualized proxy instance that wraps execution of the <code>apply</code> method with context.
   * @throws IllegalArgumentException if an already-contextualized <code>Function</code> is supplied to this method.
   * @since 3.0
   */
  public <T, R> Function<T, R> contextualFunction(Function<T, R> function);

  /**
   * <p>Wraps a {@link java.lang.Runnable} with context
   * that is captured from the thread that invokes
   * <code>contextualRunnable</code>. Context is captured at the time <code>contextualRunnable</code> is invoked.</p>
   *
   * <p>When <code>run</code> is invoked on the proxy instance,
   * context is first established on the thread that will run the <code>run</code> method,
   * then the <code>run</code> method of the provided <code>Runnable</code> is invoked.
   * Finally, the previous context is restored on the thread, and control is returned to the invoker.</p>
   *
   * @param runnable instance to contextualize.
   * @return contextualized proxy instance that wraps execution of the <code>run</code> method with context.
   * @throws IllegalArgumentException if an already-contextualized <code>Runnable</code> is supplied to this method.
   * @since 3.0
   */
  public Runnable contextualRunnable(Runnable runnable);

  /**
   * <p>Wraps a {@link java.util.function.Supplier} with context captured from the thread that invokes
   * <code>contextualSupplier</code>. Context is captured at the time <code>contextualSupplier</code> is invoked.</p>
   *
   * <p>When <code>supply</code> is invoked on the proxy instance,
   * context is first established on the thread that will run the <code>supply</code> method,
   * then the <code>supply</code> method of the provided <code>Supplier</code> is invoked.
   * Finally, the previous context is restored on the thread, and the result of the
   * <code>Supplier</code> is returned to the invoker.</p>
   *
   * @param <R> supplier result type.
   * @param supplier instance to contextualize.
   * @return contextualized proxy instance that wraps execution of the <code>supply</code> method with context.
   * @throws IllegalArgumentException if an already-contextualized <code>Supplier</code> is supplied to this method.
   * @since 3.0
   */
  public <R> Supplier<R> contextualSupplier(Supplier<R> supplier);

  /**
   * Creates a new contextual object proxy for the input object instance.
   * <p>
   * Each method invocation will have the context of the application component
   * instance that created the contextual object proxy.
   * <p>
   * The contextual object is useful when developing or using Java&trade; SE
   * threading mechanisms propagating events to other component instances.
   * <p>
   * If the application component that created the proxy is not started or
   * deployed, all methods on reflected interfaces may throw an
   * {@link java.lang.IllegalStateException}.
   *  <p>
   * For example, to execute a Runnable which is contextualized with the
   * creator's context using a Java&trade; SE ExecutorService:
   *
   * <pre>
   *  public class MyRunnable implements Runnable {
   *      public void run() {
   *          System.out.println(&quot;MyRunnable.run with Jakarta EE Context available.&quot;);
   *      }
   *  }
   *
   *  InitialContext ctx = new InitialContext();
   *  ThreadFactory threadFactory = (ThreadFactory) ctx
   *           .lookup(&quot;java:comp/env/concurrent/ThreadFactory&quot;);
   *
   *  ContextService ctxService = (ContextService) ctx
   *           .lookup(&quot;java:comp/env/concurrent/ContextService&quot;);
   *
   *  MyRunnable myRunnableInstance = ...;
   *
   *  Runnable rProxy = ctxService.createContextualProxy(myRunnableInstance, Runnable.class);
   *
   *  ExecutorService exSvc = Executors.newThreadPool(10, threadFactory);
   *
   *  Future f = exSvc.submit(rProxy);
   * </pre>
   * <p>
   *
   * @param instance the instance of the object to proxy.
   * @param intf the interface that the proxy should implement.
   * @param <T> the type of the instance to proxy
   * @return a proxy for the input object that implements the specified interface.
   * @throws java.lang.IllegalArgumentException - if the {@code intf} argument
   * is null or the instance does not implement the specified
   * interface.
   * @throws java.lang.UnsupportedOperationException - if the {@code intf}
   * interface is {@link java.io.Serializable serializable}
   * but a thread context type does not support serialization.
   */
  public <T> T createContextualProxy(T instance, Class<T> intf);

  /**
   * Creates a new contextual object proxy for the input object instance.
   * <p>
   * This method is similar to {@code <T> T createContextualProxy(T instance, Class<T> intf)}
   * except that this method can be used if the proxy has to support multiple
   * interfaces.
   *  </p>
   * Example:
   *
   * <pre>
   *  public class MyRunnableWork implements Runnable, SomeWorkInterface {
   *      public void run() {
   *          System.out.println(&quot;MyRunnableWork.run with Jakarta EE Context available.&quot;);
   *      }
   *      public void someWorkInterfaceMethod() {
   *          ...
   *      }
   *  }
   *
   *  ThreadFactory threadFactory = ...;
   *
   *  ContextService ctxService = ...;
   *
   *  MyRunnableWork myRunnableWorkInstance = ...;
   *
   *  Object proxy = ctxService.createContextualProxy(myRunnableWorkInstance,
   *                                   Runnable.class, SomeWorkInterface.class);
   *
   *  // call SomeWorkInterface method on the proxy
   *  ((SomeWorkInterface) proxy).someWorkInterfaceMethod();
   *
   *  ExecutorService exSvc = Executors.newThreadPool(10, threadFactory);
   *
   *  // submit the proxy as a Runnable to the ExecutorService
   *  Future f = exSvc.submit( (Runnable)proxy);
   * </pre>
   *
   *
   * @param instance the instance of the object to proxy.
   * @param interfaces the interfaces that the proxy should implement.
   * @return a proxy for the input object that implements all of the specified
   *         interfaces.
   * @throws java.lang.IllegalArgumentException - if the {@code interfaces}
   * argument is null or the instance does not implement
   * all the specified interfaces.
   * @throws java.lang.UnsupportedOperationException - if any of the {@code interfaces}
   * are {@link java.io.Serializable serializable} but a thread context type
   * does not support serialization.
   */
  public Object createContextualProxy(Object instance, Class<?>... interfaces);

  /**
   * Creates a new contextual object proxy for the input object instance.
   * <p>
   * The contextual object is useful when developing or using Java&trade; SE
   * threading mechanisms propagating events to other component instances.
   * <p>
   * If the application component that created the proxy is not started or
   * deployed, all methods on reflected interfaces may throw an
   * {@link java.lang.IllegalStateException}.
   * <p>
   * This method accepts a {@code Map} object which allows the
   * contextual object creator to define what contexts or behaviors to capture
   * when creating the contextual object. The specified properties will remain
   * with the contextual object.
   * <p>
   *
   * For example, to call a Message Driven Bean (MDB) with the sender's
   * context, but within the MDB's transaction:
   *
   * <pre>
   * public class MyServlet ... {
   *     public void doPost() throws NamingException, JMSException {
   *        InitialContext ctx = new InitialContext();
   *
   *        // Get the ContextService that only propagates
   *        // security context.
   *        ContextService ctxSvc = (ContextService)
   *            ctx.lookup(&quot;java:comp/env/SecurityContext&quot;);
   *
   *        // Set any custom context data through execution properties
   *        Map&lt;String, String&gt; execProps = new HashMap&lt;&gt;();
   *        execProps.put(&quot;vendor_a.security.tokenexpiration&quot;, &quot;15000&quot;);
   *        // Specify that contextual object should run inside the current
   *        // transaction.  If we have a failure, we don't want to consume
   *        // the message.
   *        execProps.put(ManagedTask.TRANSACTION, &quot;USE_TRANSACTION_OF_EXECUTION_THREAD&quot;);
   *
   *        ProcessMessage msgProcessor =
   *            ctxSvc.createContextualProxy(new MessageProcessor(), execProps,
   *            ProcessMessage.class);
   *
   *        ConnectionFactory cf = (ConnectionFactory)
   *             ctx.lookup(&quot;java:comp/env/MyTopicConnectionFactory&quot;);
   *        Destination dest = (Destination) ctx.lookup(&quot;java:comp/env/MyTopic&quot;);
   *        Connection con = cf.createConnection();
   *
   *        Session session = con.createSession(true, Session.AUTO_ACKNOWLEDGE);
   *        MessageProducer producer = session.createProducer(dest);
   *
   *        Message msg = session.createObjectMessage((Serializable)msgProcessor);
   *        producer.send(dest, msg);
   *        ...
   *
   *    }
   *
   *  public class MyMDB ... {
   *    public void onMessage(Message msg) {
   *        // Get the ProcessMessage contextual object from the message.
   *        ObjectMessage omsg = (ObjectMessage)msg;
   *        ProcessMessage msgProcessor = (ProcessMessage)omsg.getObject();
   *
   *        // Process the message in the specified context.
   *        msgProcessor.processMessage(msg);
   *    }
   *  }
   *
   *  public interface  ProcessMessage {
   *      public void processMessage(Message msg);
   *  }
   *
   *  public class MessageProcessor implements ProcessMessage, Serializable {
   *      public void processMessage(Message msg) {
   *          // Process the message with the application container
   *          // context that sent the message.
   *
   *      }
   *  }
   *</pre>
   *
   *
   * @param instance the instance of the object to proxy.
   * @param executionProperties the properties to use when creating and running the context
   *                          object.
   * @param intf the interface that the proxy should implement.
   * @param <T> the type of the interface
   * @return a proxy for the input object that implements the specified interface.
   *
   * @throws java.lang.IllegalArgumentException - if the {@code intf} argument
   * null or the instance does not implement the specified interface.
   * @throws java.lang.UnsupportedOperationException - if the {@code intf}
   * interface is {@link java.io.Serializable serializable}
   * but a thread context type does not support serialization.
   */
  public <T> T createContextualProxy(T instance,
                                     Map<String, String> executionProperties,
                                     Class<T> intf);
  
  /**
   * Creates a new contextual object proxy for the input object instance.
   * <p>
   * This method is similar to
   * {@code <T> T createContextualProxy(T instance, Map<String, String> executionProperties, Class<T> intf)}
   * except that this method can be used if the proxy has to support multiple
   * interfaces.
   *
   * @param instance the instance of the object to proxy.
   * @param executionProperties the properties to use when creating and running the context
   *                          object.
   * @param interfaces the interfaces that the proxy should implement.
   * @return a proxy for the input object that implements all of the specified
   *         interfaces.
   *
   * @throws java.lang.IllegalArgumentException - if the {@code interfaces}
   * argument is null or the instance does not implement all the specified
   * interfaces.
   * @throws java.lang.UnsupportedOperationException - if any of the {@code interfaces}
   * are {@link java.io.Serializable serializable} but a thread context type
   * does not support serialization.
   */
  public Object createContextualProxy(Object instance,
                                      Map<String, String> executionProperties,
                                      Class<?>... interfaces);

  /**
   * <p>Captures thread context as an {@link java.util.concurrent.Executor}
   * that runs tasks on the same thread from which
   * <code>execute</code>is invoked but with context that is captured from the thread
   * that invokes <code>currentContextExecutor</code>.</p>
   *
   * <p>Example usage:</p>
   * <pre>
   * <code>Executor contextSnapshot = contextSvc.currentContextExecutor();
   * ...
   * // from another thread, or after thread context has changed,
   * contextSnapshot.execute(() -&gt; obj.doSomethingThatNeedsContext());
   * contextSnapshot.execute(() -&gt; doSomethingElseThatNeedsContext(x, y));
   * </code></pre>
   *
   * <p>The returned <code>Executor</code> must raise <code>IllegalArgumentException</code>
   * if an already-contextualized <code>Runnable</code> is supplied to its
   * <code>execute</code> method.</p>
   *
   * @return an executor that wraps the <code>execute</code> method with context.
   * @since 3.0
   */
  public Executor currentContextExecutor();

  /**
   * Gets the current execution properties on the context proxy instance.
   *
   * @param contextualProxy the contextual proxy instance to retrieve the execution properties.
   * @return A copy of the current contextual object execution properties, or null if
   *         the contextualProxy is created without specifying any execution properties.
   *
   * @throws java.lang.IllegalArgumentException thrown if the input contextualProxy is not a valid
   *                                            contextual object proxy created with the
   *                                            {@code createContextualProxy} method.
   */
  public Map<String, String> getExecutionProperties(Object contextualProxy);

  /**
   * <p>Returns a new {@link java.util.concurrent.CompletableFuture} that is completed by the completion of the
   * specified stage.</p>
   *
   * <p>The new completable future gets its default asynchronous execution facility
   * from this <code>ContextService</code>,
   * using the same {@link ManagedExecutorService} if this <code>ContextService</code>
   * was obtained by {@link ManagedExecutorService#getContextService()}.</p>
   *
   * <p>When dependent stages are created from the new completable future,
   * and from the dependent stages of those stages, and so on, thread context is captured
   * and/or cleared by the <code>ContextService</code>. This guarantees that the action
   * performed by each stage always runs under the thread context of the code that creates the stage,
   * unless the user explicitly overrides by supplying a pre-contextualized action.</p>
   *
   * <p>Invocation of this method does not impact thread context propagation for the originally supplied
   * completable future or any other dependent stages directly created from it (not using this method).</p>
   *
   * @param <T> completable future result type.
   * @param stage a completable future whose completion triggers completion of the new completable
   *        future that is created by this method.
   * @return the new completable future.
   * @since 3.0
   */
  public <T> CompletableFuture<T> withContextCapture(CompletableFuture<T> stage);

  /**
   * <p>Returns a new {@link java.util.concurrent.CompletionStage} that is completed by the completion of the
   * specified stage.</p>
   *
   * <p>The new completion stage gets its default asynchronous execution facility from this <code>ContextService</code>,
   * using the same {@link ManagedExecutorService} if this <code>ContextService</code>
   * was obtained by {@link ManagedExecutorService#getContextService()},
   * otherwise using the DefaultManagedExecutorService.</p>
   *
   * <p>When dependent stages are created from the new completion stage,
   * and from the dependent stages of those stages, and so on, thread context is captured
   * and/or cleared by the <code>ContextService</code>. This guarantees that the action
   * performed by each stage always runs under the thread context of the code that creates the stage,
   * unless the user explicitly overrides by supplying a pre-contextualized action.</p>
   *
   * <p>Invocation of this method does not impact thread context propagation for the originally supplied
   * stage or any other dependent stages directly created from it (not using this method).</p>
   *
   * @param <T> completion stage result type.
   * @param stage a completion stage whose completion triggers completion of the new stage
   *        that is created by this method.
   * @return the new completion stage.
   * @since 3.0
   */
  public <T> CompletionStage<T> withContextCapture(CompletionStage<T> stage);
}
