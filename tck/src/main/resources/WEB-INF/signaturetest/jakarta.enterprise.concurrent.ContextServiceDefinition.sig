#public abstract @interface jakarta.enterprise.concurrent.ContextServiceDefinition extends [interface java.lang.annotation.Annotation]
@java.lang.annotation.Repeatable(value=jakarta.enterprise.concurrent.ContextServiceDefinition$List.class)
@java.lang.annotation.Retention(value=RUNTIME)
@java.lang.annotation.Target(value={TYPE})
public abstract java.lang.String jakarta.enterprise.concurrent.ContextServiceDefinition.name()
public abstract java.lang.String[] jakarta.enterprise.concurrent.ContextServiceDefinition.cleared() default {Transaction}
public abstract java.lang.String[] jakarta.enterprise.concurrent.ContextServiceDefinition.propagated() default {Remaining}
public abstract java.lang.String[] jakarta.enterprise.concurrent.ContextServiceDefinition.unchanged() default {}
public static final java.lang.String jakarta.enterprise.concurrent.ContextServiceDefinition.ALL_REMAINING
public static final java.lang.String jakarta.enterprise.concurrent.ContextServiceDefinition.APPLICATION
public static final java.lang.String jakarta.enterprise.concurrent.ContextServiceDefinition.SECURITY
public static final java.lang.String jakarta.enterprise.concurrent.ContextServiceDefinition.TRANSACTION
