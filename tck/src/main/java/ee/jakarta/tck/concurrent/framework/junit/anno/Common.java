package ee.jakarta.tck.concurrent.framework.junit.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Common {
    
    public enum PACKAGE {
        CONTEXT,
        CONTEXT_PROVIDERS,
        COUNTER,
        FIXED_COUNTER,
        MANAGED_TASK_LISTENER,
        TASKS,
        TRANSACTION,
        SIGNATURE;
        
        private static final String prefix = "ee/jakarta/tck/concurrent/common/";
        
        public String getPackageName() {
            return prefix + this.name().toLowerCase().replace("_", "/");
        }
    }
    
    PACKAGE[] value();
}
