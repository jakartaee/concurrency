#public abstract @interface jakarta.enterprise.concurrent.Asynchronous extends [interface java.lang.annotation.Annotation]
@jakarta.interceptor.InterceptorBinding()
@java.lang.annotation.Documented()
@java.lang.annotation.Inherited()
@java.lang.annotation.Retention(value=RUNTIME)
@java.lang.annotation.Target(value={METHOD, TYPE})
public abstract java.lang.String jakarta.enterprise.concurrent.Asynchronous.executor() [@jakarta.enterprise.util.Nonbinding()] default java:comp/DefaultManagedExecutorService
