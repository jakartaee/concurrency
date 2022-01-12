#public abstract @interface jakarta.enterprise.concurrent.Asynchronous extends [interface java.lang.annotation.Annotation]
@jakarta.interceptor.InterceptorBinding()
@java.lang.annotation.Documented()
@java.lang.annotation.Inherited()
@java.lang.annotation.Retention(value=RUNTIME)
@java.lang.annotation.Target(value={METHOD, TYPE})
@jakarta.enterprise.util.Nonbinding() public abstract java.lang.String jakarta.enterprise.concurrent.Asynchronous.executor() default java:comp/DefaultManagedExecutorService
