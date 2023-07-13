package ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate;

import ee.jakarta.tck.concurrent.framework.EJBJNDIProvider;

/**
 * Need to provide different JNDI names depending application deployment
 */
public class ContextEJBProvider {

    public static class FullProvider implements EJBJNDIProvider {
        public FullProvider() {
        }

        @Override
        public String getEJBJNDIName() {
            return "java:app/ContextPropagationTests_ejb/LimitedBean";
        }
    }

    public static class WebProvider implements EJBJNDIProvider {
        public WebProvider() {
        }

        @Override
        public String getEJBJNDIName() {
            return "java:app/ContextPropagationTests_web/LimitedBean";
        }
    }
}
