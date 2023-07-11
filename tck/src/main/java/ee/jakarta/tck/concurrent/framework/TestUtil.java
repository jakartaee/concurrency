/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Utility methods to be used on the client or server side. 
 * These utilities can be broken up into distinct categorities: 
 * - HTTP utilities
 * - Context lookup utilities
 * - Waiter utilities
 * - Assertions
 */
public final class TestUtil {
	public static final TestLogger log = TestLogger.get(TestUtil.class);

	public static final String nl = System.lineSeparator();
	
	// ########## HTTP ##########

	/**
	 * HTTP convenience method for servlets to get a response from another servlet. 
	 * Test clients should extend the {@link TestClient} class that has its own HTTP methods.
	 * 
	 * @param con - the URLConnection
	 * @return String - response body
	 * @throws IOException
	 */
	public static String getResponse(URLConnection con) throws IOException {	
		try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
			
			StringBuffer response = new StringBuffer();
			
			String line;
			while ((line = br.readLine()) != null) {
				response.append(line).append(nl);
			}

			return response.toString();
		}
	}

	/**
	 * HTTP convenience method for servlets to create a URLConnection and post properties 
	 * to that connection. 
	 * 
	 * Test clients should extend the {@link TestClient} class that has its own HTTP methods.
	 * 
	 * @param url - the URL to open a connection to
	 * @param props - the properties to put into the connection input stream
	 * 
	 * @return the connection for further testing
	 * @throws IOException
	 */
	public static URLConnection sendPostData(URL url, Properties props) throws IOException {
		log.info("Opening url connection to: " + url.toString());
		URLConnection urlConn = url.openConnection();
		// Begin POST of properties to SERVLET
		String argString = TestUtil.toEncodedString(props);
		urlConn.setDoOutput(true);
		urlConn.setDoInput(true);
		urlConn.setUseCaches(false);
		DataOutputStream out = new DataOutputStream(urlConn.getOutputStream());
		out.writeBytes(argString);
		out.flush();
		out.close();
		// End POST
		return urlConn;
	}
	
	static String toEncodedString(Properties args) throws UnsupportedEncodingException {
		StringBuffer buf = new StringBuffer();
		Enumeration<?> names = args.propertyNames();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			String value = args.getProperty(name);
			
			buf.append(URLEncoder.encode(name, StandardCharsets.UTF_8.name()))
				.append("=")
				.append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()));
			
			if (names.hasMoreElements())
				buf.append("&");
		}
		return buf.toString();
	}	
}
