<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page extends="ee.jakarta.tck.concurrent.framework.TestServlet" %>
<%@ page import="ee.jakarta.tck.concurrent.framework.TestLogger" %>

<%@ page import="java.util.concurrent.CompletableFuture" %>
<%@ page import="java.util.function.Supplier" %>

<%@ page import="javax.naming.InitialContext" %>

<%@ page import="jakarta.enterprise.concurrent.ContextService" %>
<%@ page import="jakarta.enterprise.concurrent.ManagedExecutorService" %>
<%@ page import="jakarta.servlet.ServletException" %>

<%@ page import="org.testng.Assert" %>


<%!
final TestLogger log = TestLogger.get("ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.jspTests");

public void testSecurityClearedContext(HttpServletRequest request, HttpServletResponse response) throws Exception {
	request.login("javajoe", "javajoe");
	
	String result;
	try {
		ContextService contextSvc =  InitialContext.doLookup("java:app/concurrent/securityClearedContextSvc");
		Supplier<String> contextualSupplier = contextSvc.contextualSupplier(() -> {
	        // Security Context should be cleared for securityClearedContextSvc
	        return request.getUserPrincipal() == null ? "null" : request.getUserPrincipal().getName();
	    });
		result = contextualSupplier.get();
		Assert.assertNotNull(result, "Security context result should have been set to a string value");
		Assert.assertEquals(result, "null", "Security context should have been cleared.");
	} catch (Exception e) { //Return any exceptions thrown by the test as a string for easier debugging
		log.warning("Exception thrown: " + e.getMessage());
	   	throw new ServletException(e);
	}
}

public void testSecurityUnchangedContext(HttpServletRequest request, HttpServletResponse response) throws Exception {
	request.login("javajoe", "javajoe");
	
	String result;
	try {
		ContextService contextSvc =  InitialContext.doLookup("java:app/concurrent/securityUnchangedContextSvc");
		Supplier<String> contextualSupplier = contextSvc.contextualSupplier(() -> {
	        // Security Context should be availible for calls on the same thread
	        return request.getUserPrincipal() == null ? "null" : request.getUserPrincipal().getName();
	    });
		result = contextualSupplier.get();
	    Assert.assertNotNull(result, "Security context result should have been set to a string value");
	    Assert.assertEquals(result, "javajoe", "Security Context should have been propagated.");
	    
	    ManagedExecutorService executor = InitialContext.doLookup("java:app/concurrent/executor2");
	    CompletableFuture<String> future = executor.supplyAsync(() -> {
	        // Security Context should not be available for calls on a new thread
	        return request.getUserPrincipal() == null ? "null" : request.getUserPrincipal().getName();
	    });
	    result = future.join();
	    Assert.assertNotNull(result, "Security context result should have been set to a string value");
	    Assert.assertEquals(result, "null", "Security context should not have been propogated.");
	} catch (Exception e) { //Return any exceptions thrown by the test as a string for easier debugging
		log.warning("Exception thrown: " + e.getMessage());
	   	throw new ServletException(e);
	}
}

public void testSecurityPropagatedContext(HttpServletRequest request, HttpServletResponse response) throws Exception {
	request.login("javajoe", "javajoe");
	
	String result;
	try {
	    ManagedExecutorService executor = InitialContext.doLookup("java:app/concurrent/executor1");
	    CompletableFuture<String> future = executor.supplyAsync(() -> {
	        // Security Context should be propogated for the default ContextService
	        return request.getUserPrincipal() == null ? "null" : request.getUserPrincipal().getName();
	    });
	    result = future.join();
	    Assert.assertNotNull(result, "Security context result should have been set to a string value");
	    Assert.assertEquals(result, "javajoe", "Security Context should have been propagated");
	} catch (Exception e) { //Return any exceptions thrown by the test as a string for easier debugging
		log.warning("Exception thrown: " + e.getMessage());
	   	throw new ServletException(e);
	}
}
%>