package ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate;

import static jakarta.enterprise.concurrent.ContextServiceDefinition.SECURITY;

import ee.jakarta.tck.concurrent.framework.TestServlet;
import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorDefinition;
import jakarta.servlet.annotation.WebServlet;

@SuppressWarnings("serial")
@ContextServiceDefinition(name = "java:app/concurrent/securityClearedContextSvc",
						   cleared = SECURITY)
@ContextServiceDefinition(name = "java:app/concurrent/securityUnchangedContextSvc",
						   unchanged = SECURITY)
@ManagedExecutorDefinition(name = "java:app/concurrent/executor1")
@ManagedExecutorDefinition(name = "java:app/concurrent/executor2",
						   context = "java:app/concurrent/securityUnchangedContextSvc")
@WebServlet("/JSPSecurityServlet")
public class JSPSecurityServlet extends TestServlet{

}
