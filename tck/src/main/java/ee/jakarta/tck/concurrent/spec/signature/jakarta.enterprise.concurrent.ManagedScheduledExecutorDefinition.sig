#public abstract @interface jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition extends [interface java.lang.annotation.Annotation]
@java.lang.annotation.Repeatable(value=jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition$List.class)
@java.lang.annotation.Retention(value=RUNTIME)
@java.lang.annotation.Target(value={TYPE})
public abstract int jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition.maxAsync() default -1
public abstract java.lang.String jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition.context() default java:comp/DefaultContextService
public abstract java.lang.String jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition.name()
public abstract long jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition.hungTaskThreshold() default -1
