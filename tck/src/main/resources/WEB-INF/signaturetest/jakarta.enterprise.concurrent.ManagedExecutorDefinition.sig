#public abstract @interface jakarta.enterprise.concurrent.ManagedExecutorDefinition extends [interface java.lang.annotation.Annotation]
@java.lang.annotation.Repeatable(value=jakarta.enterprise.concurrent.ManagedExecutorDefinition$List.class)
@java.lang.annotation.Retention(value=RUNTIME)
@java.lang.annotation.Target(value={TYPE})
public abstract int jakarta.enterprise.concurrent.ManagedExecutorDefinition.maxAsync() default -1
public abstract java.lang.String jakarta.enterprise.concurrent.ManagedExecutorDefinition.context() default java:comp/DefaultContextService
public abstract java.lang.String jakarta.enterprise.concurrent.ManagedExecutorDefinition.name()
public abstract long jakarta.enterprise.concurrent.ManagedExecutorDefinition.hungTaskThreshold() default -1
