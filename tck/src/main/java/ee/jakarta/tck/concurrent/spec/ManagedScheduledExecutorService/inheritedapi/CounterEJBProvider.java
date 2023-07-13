package ee.jakarta.tck.concurrent.spec.ManagedScheduledExecutorService.inheritedapi;

import ee.jakarta.tck.concurrent.framework.EJBJNDIProvider;

/**
 * Need to provide different JNDI names depending application deployment
 */
public class CounterEJBProvider {

    public static class FullProvider implements EJBJNDIProvider {
        public FullProvider() {
        }

        @Override
        public String getEJBJNDIName() {
            return "java:global/inheritedapi/inheritedapi/CounterSingleton";
        }
    }

    public static class WebProvider implements EJBJNDIProvider {
        public WebProvider() {
        }

        @Override
        public String getEJBJNDIName() {
            return "java:global/inheritedapi/CounterSingleton";
        }
    }
}
