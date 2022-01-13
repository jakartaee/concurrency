/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package ee.jakarta.tck.concurrent.framework;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.Properties;

/**
 * This class is intended to be used in conjunction with TestServlet.
 * TestServlets are deployed to the application server and has custom doGet/doPost methods
 * that will return a successful or failure message depending on the test outcome. 
 * 
 * The TestClient class has runTest methods that will create an HTTP connection to 
 * the TestServlet and provide the TestServlet with the method name it needs to test.
 * The TestClient class will then confirm that it recieved a successful outcome from the test.
 *
 */
public abstract class TestClient extends ArquillianTests {

	private static final TestLogger log = TestLogger.get(TestClient.class);

	public static final String SUCCESS = TestServlet.SUCCESS;
	public static final String FAILURE = TestServlet.FAILURE;
	public static final String TEST_METHOD = TestServlet.TEST_METHOD;
	
	public static final String nl = System.lineSeparator();

	//###### run test without response #####
	/**
	 * Runs test against servlet at baseURL, and assumes the testName.
	 */
	public void runTest(URL baseURL) {
		runTest(baseURL, testName);
	}
	
	/**
	 * Runs test against servlet at baseURL, and will run against a specified testName.
	 */
	public void runTest(URL baseURL, String testName) {
		try {
			assertSuccessfulURLResponse(URLBuilder.get().withBaseURL(baseURL).withPaths(getServletPath()).withTestName(testName).build(), null);	
		} catch (UnsupportedOperationException e) {
			throw new RuntimeException("Tried to call runTest method without overwritting getServletPath() method.", e);
		}
	}
	
	/**
	 * Runs test against servlet using a URLBuilder.  This is useful for complicated testing situations.
	 */
	public void runTest(URLBuilder builder) {
		assertSuccessfulURLResponse(builder.build(), null);
	}
	
	//###### run test with response	######
	
	/**
	 * Runs test against servlet at baseURL, and assumes the testName.
	 * Provide properties if you want them included in a POST request, otherwise pass in null.
	 */
	public String runTestWithResponse(URL baseURL, Properties props) {
		return runTestWithResponse(baseURL, testName, props);
	}
	
	/**
	 * Runs test against servlet at baseURL, and will run against a specified testName.
	 * Provide properties if you want them included in a POST request, otherwise pass in null.
	 */
	public String runTestWithResponse(URL baseURL, String testName, Properties props) {
		try {
			return assertSuccessfulURLResponse(URLBuilder.get().withBaseURL(baseURL).withPaths(getServletPath()).withTestName(testName).build(), props);	
		} catch (UnsupportedOperationException e) {
			throw new RuntimeException("Tried to call runTest method without overwritting getServletPath() method.", e);
		}
	}
	
	/**
	 * Runs test against servlet using a URLBuilder.  This is useful for complicated testing situations.
	 * Provide properties if you want them included in a POST request, otherwise pass in null.
	 */
	public String runTestWithResponse(URLBuilder builder, Properties props) {
		return assertSuccessfulURLResponse(builder.build(), props); 
	}

	//##### test runner ######
	private String assertSuccessfulURLResponse(URL url, Properties props) {
		log.enter("assertSuccessfulURLResponse", "Calling application with URL=" + url.toString());
		
		boolean withProps = props != null;
		boolean pass = false;

		HttpURLConnection con = null;
		try {
			con = (HttpURLConnection) url.openConnection();
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setUseCaches(false);
			con.setConnectTimeout((int) Duration.ofSeconds(30).toMillis());
			
			if(withProps) {
				con.setRequestMethod("POST");
				try( DataOutputStream wr = new DataOutputStream( con.getOutputStream())){
					wr.writeBytes( TestUtil.toEncodedString(props) );
				}
					   
			} else {
				con.setRequestMethod("GET");
			}

			final BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			final StringBuilder outputBuilder = new StringBuilder();
			String line;

			while ((line = br.readLine()) != null) {
				outputBuilder.append(line).append(nl);

				if (line.contains(SUCCESS)) {
					pass = true;
				}
			}
			
			log.exit("assertSuccessfulURLResponse", "Response code: " + con.getResponseCode() ,"Response body: " + outputBuilder.toString());

			assertTrue("Connection returned a response code that was greater than 400", con.getResponseCode() < 400);
			assertTrue("Output did not contain successful message: " + SUCCESS, pass);
		
			return outputBuilder.toString();
		} catch (IOException e) {
			throw new RuntimeException("Exception: " + e.getClass().getName() + " requesting URL=" + url.toString(), e);
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
	}
	
	/**
	 * Override this method to return the servlet path for the suite of tests.
	 * Used for the runTest() methods. 
	 */
	protected String getServletPath() {
		throw new UnsupportedOperationException("Subclass did not override the getServletPath method");
	}
	
	/**
	 * Asserts that the response from a runTestWithResponse method contains a specific string. 
	 * 
	 * @param message - message to display if test fails
	 * @param expected - the expected string to find in the response
	 * @param resp - the response you received from the servlet
	 */
	protected void assertStringInResponse(String message, String expected, String resp) {
		assertTrue(message, resp.toLowerCase().contains(expected.toLowerCase()));
	}
}
