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

import static org.testng.Assert.fail;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import ee.jakarta.tck.concurrent.common.managedTaskListener.ListenerEvent;
import ee.jakarta.tck.concurrent.common.managedTaskListener.ManagedTaskListenerImpl;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.ManagedThreadFactory;

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
	
	//########## Lookups ##########	
	public static <T> T lookup(String jndiName) {
		Context ctx = null;
		T targetObject = null;
		try {
			ctx = new InitialContext();
			targetObject = (T) ctx.lookup(jndiName);
		} catch (NamingException e) {
			throw new RuntimeException("failed to lookup resource.", e);
		} finally {
			try {
				ctx.close();
			} catch (Exception ignore) {
			}
		}
		return targetObject;
	}
	
	public static ContextService getContextService() {
		return lookup(TestConstants.DefaultContextService);
	}
	
	public static ManagedExecutorService getManagedExecutorService() {
		return lookup(TestConstants.DefaultManagedExecutorService);
	}
	
	public static ManagedScheduledExecutorService getManagedScheduledExecutorService() {
		return lookup(TestConstants.DefaultManagedScheduledExecutorService);
	}
	
	public static ManagedThreadFactory getManagedThreadFactory() {
		return lookup(TestConstants.DefaultManagedThreadFactory);
	}
	
	//########## Waiters ##########
	
	/**
	 * USE WITH CAUTION!! 
	 * When possible tests should use waitFor methods to wait for specific condition to be meet.
	 * Pausing the thread for a specific duration will directly impact test performance but in some cases is required. 
	 * 
	 * Pauses the calling thread for the specified duration
	 * 
	 * @param duration - duration to sleep
	 * @throws InterruptedException 
	 */
	public static void sleep(Duration duration) throws InterruptedException {
		log.config("Sleeping " + duration.toMillis() + " milliseconds");
		Thread.sleep(duration.toMillis());
	}
	
	/**
	 * Waits for task to complete, but will timeout after {@link TestConstants#WaitTimeout}
	 * @param future to wait for
	 * @return result
	 */
	public static <T> T waitForTaskComplete(final Future<T> future) {
		return waitForTaskComplete(future, TestConstants.WaitTimeout.toMillis());
	}
	
	/**
	 * Waits for task to complete, but will timeout after specified timeout in millis
	 * @param future - the future to wait for
	 * @param maxWaitTimeMillis - the timeout
	 * @return result
	 */
	public static <T> T waitForTaskComplete(final Future<T> future, final long maxWaitTimeMillis) {
		T result = null;
		try {
			result = future.get(maxWaitTimeMillis, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException("waitForTaskComplete", e);
		} catch (ExecutionException e) {
			throw new RuntimeException("waitForTaskComplete", e);
		} catch (TimeoutException e) {
			throw new RuntimeException("failed to finish task in " + maxWaitTimeMillis + " milliseconds. ", e);
		} catch (Exception e) { // may caught the exception thrown from the task
								// submitted.
			throw new RuntimeException("waitForTaskComplete failed to finish task", e);
		}
		return result;
	}
	
	/**
	 * Wait for listener to complete, but will timeout after {@link TestConstants#WaitTimeout}, 
	 * and will be polled ever {@link TestConstants#PollInterval}
	 * 
	 * @param managedTaskListener - the listener to be polled
	 */
	public static void waitForListenerComplete(ManagedTaskListenerImpl managedTaskListener) {
		waitForListenerComplete(managedTaskListener, TestConstants.WaitTimeout.toMillis(), TestConstants.PollInterval.toMillis());
	}

	/**
	 * Wait for listener to complete, but will timeout after a specified timeout, 
	 * and will be polled ever specified interval
	 * 
	 * @param managedTaskListener - the listener to be polled
	 * @param maxWaitTimeMillis - timeout
	 * @param pollIntervalMillis - poll interval
	 */
	public static void waitForListenerComplete(ManagedTaskListenerImpl managedTaskListener, long maxWaitTimeMillis,
			long pollIntervalMillis) {
		final long stopTime = System.currentTimeMillis() + maxWaitTimeMillis;
		while (!managedTaskListener.eventCalled(ListenerEvent.DONE) && System.currentTimeMillis() < stopTime) {
			try {
				Thread.sleep(pollIntervalMillis);
			} catch (InterruptedException e) {
				throw new RuntimeException("Thread was inerrupted while sleeping", e);
			}
		}
	}

	/**
	 * Waits for future to complete, but will timeout after {@link TestConstants#WaitTimeout},
	 * and will be polled every {@link TestConstants#PollInterval}
	 * 
	 * The difference between this method and waitForTaskComplete is that some
	 * scheduled task will return values for multiple times, in this situation
	 * waitForTaskComplete does not work.
	 * 
	 * @param future - the future to wait for
	 */
	public static void waitTillFutureIsDone(Future<?> future) {
		long start = System.currentTimeMillis();

		while (!future.isDone()) {
			try {
				Thread.sleep(TestConstants.PollInterval.toMillis());
			} catch (InterruptedException ignore) {
			}

			if ((System.currentTimeMillis() - start) > TestConstants.WaitTimeout.toMillis()) {
				throw new RuntimeException("Future did not finish before wait timeout elapsed.");
			}
		}
	}
	
	/**
	 * Waits for future to throw an error, but will timeout after {@link TestConstants#WaitTimeout},
	 * and will be polled every {@link TestConstants#PollInterval}
	 * 
	 * @param future - the future to wait for
	 */
	public static void waitTillFutureThrowsException(Future future, Class<?> expected) {
		long start = System.currentTimeMillis();

		while (true) {
			try {
				Thread.sleep(TestConstants.PollInterval.toMillis());
				future.get();
			} catch (InterruptedException ignore) {
			} catch (Throwable e) {
				if(e.getClass().equals(expected)) {
					return; //expected
				}
			}

			if ((System.currentTimeMillis() - start) > TestConstants.WaitTimeout.toMillis()) {
				throw new RuntimeException("Future did not throw exception before wait timeout elapased.");
			}
		}
	}
	
	/**
	 * Waits until thread is finished, but will timeout after {@link TestConstants#WaitTimeout},
	 * and will be polled every {@link TestConstants#PollInterval}
	 * 
	 * @param thread - the thread to wait for
	 */
	public static void waitTillThreadFinish(Thread thread) {
		long start = System.currentTimeMillis();

		while (thread.isAlive()) {
			try {
				Thread.sleep(TestConstants.PollInterval.toMillis());
			} catch (InterruptedException ignore) {
			}

			if ((System.currentTimeMillis() - start) > TestConstants.WaitTimeout.toMillis()) {
				throw new RuntimeException("Thread did not finish before wait timeout elapsed.");
			}
		}
	}
	
	//########## Custom assertions ##########

	public static void assertInRange(Object[] range, Object actual) {
		String expected = "";
		for (Object each : range) {
			expected += each.toString();
			expected += ",";
		}
		expected = expected.substring(0, expected.length() - 1);
		String msg = "expected in " + expected + " but you got " + actual;
		for (Object each : range) {
			if (each.equals(actual)) {
				return;
			}
		}
		fail(msg);
	}

	public static void assertIntInRange(int low, int high, int actual) {
		String msg = "expected in range " + low + " , " + high;
		msg += " but you got " + actual;
		if (actual < low || actual > high) {
			fail(msg);
		}
	}
}
