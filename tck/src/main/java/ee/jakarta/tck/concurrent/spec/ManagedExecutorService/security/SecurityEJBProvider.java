package ee.jakarta.tck.concurrent.spec.ManagedExecutorService.security;

import ee.jakarta.tck.concurrent.framework.EJBJNDIProvider;

/**
 * Need to provide different JNDI names depending application deployment
 */
public class SecurityEJBProvider {

    public static class FullProvider implements EJBJNDIProvider {
        public FullProvider() {
        }

        @Override
        public String getEJBJNDIName() {
            return "java:global/security/security_ejb/SecurityTestEjb";
        }
    }

    public static class WebProvider implements EJBJNDIProvider {
        public WebProvider() {
        }

        @Override
        public String getEJBJNDIName() {
            return "java:global/security_web/SecurityTestEjb";
        }
    }
}
