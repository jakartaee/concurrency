package ee.jakarta.tck.concurrent.framework.junit.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Common {
    
    public enum PACKAGE {
        CONTEXT("ee.jakarta.tck.concurrent.common.context"),
        CONTEXT_PROVIDER("ee.jakarta.tck.concurrent.common.context.provider"),
        COUNTER("ee.jakarta.tck.concurrent.common.counter"),
        FIXED_COUNTER("ee.jakarta.tck.concurrent.common.fixed.counter"),
        MANAGED_TASK_LISTENER("ee.jakarta.tck.concurrent.common.managedTaskListener"),
        TASKS("ee.jakarta.tck.concurrent.common.tasks"),
        SIGNATURE("ee.jakarta.tck.concurrent.framework.signaturetest");
        
        private Package pkg;
        
        PACKAGE(String packageName) {
            this.pkg = ClassLoader.getSystemClassLoader().getDefinedPackage(packageName);
        }
        
        public Package getPackage() {
            return pkg;
        }
    }
    
    PACKAGE[] value();
}
