package jakarta.enterprise.concurrent.tck.framework;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Standard superclass for test servlets that accepts a `testMethod` parameter 
 * to the doGet / doPost methods that will attempt to run that method on the subclass. 
 * 
 * The doGet / doPost methods will append `SUCCESS` to the response if the test is successfully.
 * Otherwise, SUCCESS will not be appended to the response. 
 */
public class TestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final TestLogger log = TestLogger.get(TestServlet.class);
	
	private boolean runBeforeClass = true;
	
	public static final String SUCCESS = "SUCCESS";
	public static final String FAILURE = "FAILURE";
	public static final String TEST_METHOD = "testMethod";
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		doGet(request, response);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String method = request.getParameter(TEST_METHOD);
		
		log.enter(method, "Request URL: " + request.getRequestURL() + '?' + request.getQueryString());
		
		if(runBeforeClass) {
			try {
				beforeClass();
				runBeforeClass = false;
			} catch (Exception e) {
				throw new RuntimeException("Caught exception trying to run beforeClass method.", e);
			}
		}

		PrintWriter writer = response.getWriter();
		if (method != null && method.length() > 0) {
			try {
				before();

				// Use reflection to try invoking various test method signatures:
				// 1) method(HttpServletRequest request, HttpServletResponse response)
				// 2) method()
				// 3) use custom method invocation by calling invokeTest(method, request,
				// response)
				try {
					Method mthd = getClass().getMethod(method, HttpServletRequest.class, HttpServletResponse.class);
					mthd.invoke(this, request, response);
				} catch (NoSuchMethodException nsme) {
					try {
						Method mthd = getClass().getMethod(method, (Class<?>[]) null);
						mthd.invoke(this);
					} catch (NoSuchMethodException nsme1) {
						log.config("Delegating to invokeTest method");
						invokeTest(method, request, response);
					}
				} finally {
					after();
				}

				writer.println(SUCCESS);
			} catch (Throwable t) {
				if (t instanceof InvocationTargetException) {
					t = t.getCause();
				}
				
				log.warning("ERROR: Caught exception attempting to call test method " + method + " on servlet "
						+ getClass().getName(), t);

				writer.println("ERROR: Caught exception attempting to call test method " + method + " on servlet "
						+ getClass().getName());
				t.printStackTrace(writer);
			}
		} else {
			log.warning("ERROR: expected testMethod parameter");
			writer.println("ERROR: expected testMethod parameter");
		}

		writer.flush();
		writer.close();
		
		log.exit(method);
	}
	
	/**
	 * Override to mimic JUnit's {@code @BeforeClass} annotation.
	 */
	protected void beforeClass() throws Exception {
	}

	/**
	 * Override to mimic JUnit's {@code @Before} annotation.
	 */
	protected void before() throws Exception {
	}

	/**
	 * Override to mimic JUnit's {@code @After} annotation.
	 */
	protected void after() throws Exception {
	}

	/**
	 * Implement this method for custom test invocation, such as specific test
	 * method signatures
	 */
	protected void invokeTest(String method, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		throw new NoSuchMethodException("No such method '" + method + "' found on class " + getClass()
				+ " with any of the following signatures:   " + method + "(HttpServletRequest, HttpServletResponse)   "
				+ method + "()");
	}
}
