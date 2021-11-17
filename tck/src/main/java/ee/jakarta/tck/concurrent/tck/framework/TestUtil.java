/*
 * Copyright (c) 2007, 2021 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id$
 */

package jakarta.enterprise.concurrent.tck.framework;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.Enumeration;
import java.util.Properties;

/**
 * TestUtil is a final utility class responsible for implementing logging across
 * multiple VMs. It also contains many convenience methods for logging property
 * object contents, stacktraces, and header lines.
 *
 */
public final class TestUtil {
	public static final TestLogger log = TestLogger.get(TestUtil.class);

	public static final String nl = System.lineSeparator();

	/**
	 * Convience method to handle sucking in the data from a connection.
	 */
	public static String getResponse(URLConnection connection) throws IOException {
		StringBuffer content;
		BufferedReader in;
		// set up the streams / readers
		InputStream instream = connection.getInputStream();
		InputStreamReader inreader = new InputStreamReader(instream);
		in = new BufferedReader(inreader);
		// data structures
		content = new StringBuffer(1024);
		char[] chars = new char[1024];
		int length = 0;
		// pull the data into the content buffer
		while (length != -1) {
			content.append(chars, 0, length);
			length = in.read(chars, 0, chars.length);
		}
		// return
		instream.close(); // john feb 16
		inreader.close(); // john feb 16
		in.close(); // john feb 16
		return content.toString();
	}

	/**
	 * Loads any properties that might be in a given String.
	 */
	private static Properties getResponseProperties(String string) throws IOException {
		Properties props;
		ByteArrayInputStream in;
		byte[] bytes;
		props = new Properties();
		bytes = string.getBytes();
		in = new ByteArrayInputStream(bytes);
		props.load(in);
		in.close();
		return props;
	}

	@SuppressWarnings("deprecation")
	public static String toEncodedString(Properties args) {
		StringBuffer buf = new StringBuffer();
		Enumeration<?> names = args.propertyNames();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			String value = args.getProperty(name);
			buf.append(URLEncoder.encode(name)).append("=").append(URLEncoder.encode(value));
			if (names.hasMoreElements())
				buf.append("&");
		}
		return buf.toString();
	}

	public static URLConnection sendPostData(Properties p, URL url) throws IOException {
		log.info("Openning url connection to: " + url.toString());
		URLConnection urlConn = url.openConnection();
		// Begin POST of properties to SERVLET
		String argString = TestUtil.toEncodedString(p);
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

	/**
	 * pauses the calling thread for the specified number of seconds
	 *
	 * @param s number of seconds
	 */
	public static void sleepSec(int s) {
		log.config("Sleeping " + s + " seconds");
		try {
			Thread.sleep(Duration.ofSeconds(s).toMillis());
		} catch (InterruptedException e) {
			log.severe("Exception: " + e);
		}
	}

	/**
	 * pauses the calling thread for the specified number of milliseconds
	 *
	 * @param s number of milliseconds
	 */
	public static void sleep(int s) {
		sleepMsec(s);
	}

	/**
	 * pauses the calling thread for the specified number of milliseconds
	 *
	 * @param s number of milliseconds
	 */
	public static void sleepMsec(int s) {
		log.config("Sleeping " + s + " milliseconds");
		try {
			Thread.sleep(s);
		} catch (InterruptedException e) {
			log.severe("Exception: " + e);
		}
	}

}
