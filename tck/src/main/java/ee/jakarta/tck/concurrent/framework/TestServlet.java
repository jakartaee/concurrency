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
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Standard superclass for test servlets that accepts a `testMethod` parameter
 * to the doGet / doPost methods that will attempt to run that method on the
 * subclass.
 * 
 * The doGet / doPost methods will append `SUCCESS` to the response if the test
 * is successfully. Otherwise, SUCCESS will not be appended to the response.
 */
public class TestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final TestLogger log = TestLogger.get(TestServlet.class);

    public static final String nl = System.lineSeparator();

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

        if (runBeforeClass) {
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
                writer.println(FAILURE);
                String message = "Caught exception attempting to call test method " + method + " on servlet "
                        + getClass().getName();
                log.warning(message, t);
                writer.println(message);
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

    /**
     * HTTP convenience method for servlets to get a response from another servlet.
     * Test clients should extend the {@link TestClient} class that has its own HTTP
     * methods.
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
     * HTTP convenience method for servlets to create a URLConnection and post
     * properties to that connection.
     * 
     * Test clients should extend the {@link TestClient} class that has its own HTTP
     * methods.
     * 
     * @param url   - the URL to open a connection to
     * @param props - the properties to put into the connection input stream
     * 
     * @return the connection for further testing
     * @throws IOException
     */
    public static URLConnection sendPostData(URL url, Properties props) throws IOException {
        log.info("Opening url connection to: " + url.toString());
        URLConnection urlConn = url.openConnection();
        // Begin POST of properties to SERVLET
        String argString = TestClient.toEncodedString(props);
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
}
