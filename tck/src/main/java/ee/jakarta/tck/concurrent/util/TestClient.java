package jakarta.enterprise.concurrent.util;

import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;

/**
 * Test client superclass that should be extended by all test classes in this
 * TCK. This will allow for easier serviceability by abstracting out the testNG
 * specific work and allowing test classes to just focus on test logic.
 * 
 * Additionally, if in the future we want to replace testNG with another testing
 * framework, only this class will need to be updated.
 * 
 * Uses JUnit method param ordering for ease of portability
 *
 */
public abstract class TestClient {

	/**
	 * Name of the test being executed. Safe to reference inside of a test method.
	 * Otherwise, set to null.
	 */
	protected String testName;

	// Test properties
	protected TestProperties props = new TestProperties();

	// Server data
	protected String host;
	protected int port;
	private String URLContext;

	// Server data keys
	protected static final String hostNameKey = "webServerHost";
	protected static final String portNumberKey = "webServerPort";

	// Other common constants
	protected static final String HTTP = "http";

	// GETTERS AND SETTERS
	public String getURLContext() {
		return URLContext;
	}

	public void setURLContext(String URLContext) {
		this.URLContext = URLContext;
	}

	// UTILITY METHODS

	/**
	 * Gets webServerHost, and webServerPort properties from JVM properties. Best
	 * called from @BeforeClass
	 */
	protected void loadServerProperties() {
		// TODO ensure this method works

		// get props
		host = props.getProperty(hostNameKey);
		port = Integer.parseInt(props.getProperty(portNumberKey));

		// check props for errors
		assertNotNull("Expected '" + hostNameKey + "' to be set in JVM properties with a non-null value", host);
		assertTrue("Expected '" + portNumberKey + "' to be set in JVM properties with a non-zero, non-negative value",
				port >= 1);
	}

	protected void setupFailure(Throwable t) {
		org.testng.Assert.fail("Failed during setup due to an exception", t);
	}

	protected void cleanupFailure(Throwable t) {
		org.testng.Assert.fail("Failed during cleanup due to an exception", t);
	}

	// LIFECYCLE METHODS
	@BeforeClass
	public void testClientClassEntry(final ITestContext testContext) {
		TestUtil.logTrace("Run test class: " + this.getClass().getSuperclass().getSimpleName());
	}

	@BeforeTest
	public void testClientTestEntry(final ITestContext testContext) {
		testName = testContext.getName();
		TestUtil.logTraceEntry(testName);
	}

	@AfterTest
	public void testClientTestExit(final ITestContext testContext) {
		TestUtil.logTraceExit(testName);
		testName = null;
	}

	@AfterClass
	public void testClientClassExit() {
		TestUtil.logTrace("Exit test class: " + this.getClass().getSuperclass().getSimpleName());
	}

	// ASSERTION METHODS
	protected void assertTrue(boolean isTrue) {
		org.testng.Assert.assertTrue(isTrue, testName + " failed");
	}

	protected void assertTrue(String message, boolean isTrue) {
		org.testng.Assert.assertTrue(isTrue, message);
	}

	protected void assertFalse(boolean isFalse) {
		org.testng.Assert.assertFalse(isFalse, testName + " failed");
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
		org.testng.Assert.assertNull(obj,
				testName + " failed the task should return null result, actual result=" + obj);
	}

	protected void assertNull(String message, Object obj) {
		org.testng.Assert.assertNull(obj, message);
	}

	protected void assertNotNull(Object obj) {
		org.testng.Assert.assertNotNull(obj,
				testName + " failed the task should return not null result, actual result=" + obj);
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
		org.testng.Assert.fail(testName + " failed due to an exception", t);
	}
}
