package jakarta.enterprise.concurrent.tck.framework;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Standard superclass for test servlets that accepts a `testMethod` parameter 
 * to the doGet method that will attempt to run that method on the subclass. 
 * 
 * The doGet method will append `SUCCESS` to the response if the test is successfully.
 * Otherwise, SUCCESS will not be appended to the response. 
 *
 */
public class TestServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public static final String SUCCESS = "SUCCESS";
	public static final String TEST_METHOD = "testMethod";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String method = request.getParameter(TEST_METHOD);

		System.out.println(">>> BEGIN: " + method);
		System.out.println("Request URL: " + request.getRequestURL() + '?' + request.getQueryString());
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

				System.out.println("ERROR: " + t);
				StringWriter sw = new StringWriter();
				t.printStackTrace(new PrintWriter(sw));
				System.err.print(sw);

				writer.println("ERROR: Caught exception attempting to call test method " + method + " on servlet "
						+ getClass().getName());
				t.printStackTrace(writer);
			}
		} else {
			System.out.println("ERROR: expected testMethod parameter");
			writer.println("ERROR: expected testMethod parameter");
		}

		writer.flush();
		writer.close();

		System.out.println("<<< END:   " + method);
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

	// ASSERTION METHODS
	protected void setupFailure(Throwable t) {
		org.testng.Assert.fail("Failed during setup due to an exception", t);
	}

	protected void cleanupFailure(Throwable t) {
		org.testng.Assert.fail("Failed during cleanup due to an exception", t);
	}

	protected void assertTrue(boolean isTrue) {
		org.testng.Assert.assertTrue(isTrue, "failed");
	}

	protected void assertTrue(String message, boolean isTrue) {
		org.testng.Assert.assertTrue(isTrue, message);
	}

	protected void assertFalse(boolean isFalse) {
		org.testng.Assert.assertFalse(isFalse, "failed");
	}

	protected void assertFalse(String message, boolean isFalse) {
		org.testng.Assert.assertFalse(isFalse, message);
	}

	protected void assertEquals(String message, int expected, int actual) {
		org.testng.Assert.assertEquals(actual, expected, message);
	}

	protected void assertEquals(String message, String expected, String actual) {
		org.testng.Assert.assertEquals(actual, expected, message);
	}

	protected void assertNull(Object obj) {
		org.testng.Assert.assertNull(obj, "failed the task should return null result, actual result=" + obj);
	}

	protected void assertNull(String message, Object obj) {
		org.testng.Assert.assertNull(obj, message);
	}

	protected void assertNotNull(Object obj) {
		org.testng.Assert.assertNotNull(obj, "failed the task should return not null result, actual result=" + obj);
	}

	protected void assertNotNull(String message, Object obj) {
		org.testng.Assert.assertNotNull(obj, message);
	}

	protected void fail(String message) {
		org.testng.Assert.fail(message);
	}

	protected void fail(String message, Throwable t) {
		org.testng.Assert.fail(message, t);
	}

	protected void fail(Throwable t) {
		org.testng.Assert.fail("failed due to an exception", t);
	}

}
