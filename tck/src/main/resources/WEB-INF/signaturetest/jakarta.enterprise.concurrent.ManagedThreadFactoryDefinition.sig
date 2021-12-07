#public abstract @interface jakarta.enterprise.concurrent.ManagedThreadFactoryDefinition extends [interface java.lang.annotation.Annotation]
@java.lang.annotation.Repeatable(value=jakarta.enterprise.concurrent.ManagedThreadFactoryDefinition$List.class)
@java.lang.annotation.Retention(value=RUNTIME)
@java.lang.annotation.Target(value={TYPE})
public abstract int jakarta.enterprise.concurrent.ManagedThreadFactoryDefinition.priority() default 5
public abstract java.lang.String jakarta.enterprise.concurrent.ManagedThreadFactoryDefinition.context() default java:comp/DefaultContextService
public abstract java.lang.String jakarta.enterprise.concurrent.ManagedThreadFactoryDefinition.name()
