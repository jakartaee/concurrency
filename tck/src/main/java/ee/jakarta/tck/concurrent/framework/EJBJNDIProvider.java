package ee.jakarta.tck.concurrent.framework;

/**
 * A service provider to pass along EJB JNDI names from test class to servlet,
 * or tasks. This is a necessary provider since the same test packaged as an EAR
 * for Full profile, and a WAR for Web Profile will have different JNDI names
 * for their EJBs.
 */
public interface EJBJNDIProvider {
    /**
     * Provides the EJB JNDI name for the test.
     */
    public String getEJBJNDIName();
}
