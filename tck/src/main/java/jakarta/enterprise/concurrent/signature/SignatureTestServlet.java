/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
package jakarta.enterprise.concurrent.signature;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.TreeSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/SignatureTestServlet")
public class SignatureTestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String SUCCESS = "success";

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        System.out.println("STARTING " + getClass().getName() + "." + action);
        try {
            String result;

            // Format for 'action' parameter is testSignatures-fully.qualified.class.name
            String className = action == null || action.length() < 16 ? null : action.substring(15);
            if (className == null)
                result = "unknown or missing action for " + getClass().getName() + ": " + action;
            else
                result = testSignatures(className);

            System.out.println((SUCCESS.equals(result) ? "PASSED" : "FAILED") +
                               getClass().getName() + "." + action + ": " + result);
            resp.getWriter().println(result);
        } catch (Throwable x) {
            System.out.print("FAILED " + getClass().getName() + "." + action + ": ");
            x.printStackTrace(System.out);
            x.printStackTrace(resp.getWriter());
        }
    }

    /**
     * Perform signatures tests for the specified class.
     *
     * @param className fully qualified class name.
     */
    private String testSignatures(String className) throws Throwable {
        TreeSet<String> observed = new TreeSet<String>();

        Class<?> c = Class.forName(className.replace('-', '$'));

        StringBuilder b = new StringBuilder(200).append("#").append(c.toGenericString());
        if (c.getSuperclass() != null)
            b.append(" extends " + c.getSuperclass().getName());
        Class<?>[] i = c.getInterfaces();
        if (i.length > 0)
            b.append(c.isInterface() ? " extends " : " implements ").append(Arrays.toString(i));
        observed.add(b.toString());

        for (Annotation a : c.getAnnotations())
            observed.add(a.toString());

        for (Constructor<?> ctor : c.getConstructors())
            if (c.equals(ctor.getDeclaringClass())) {
                b = new StringBuilder(ctor.toGenericString());
                Annotation[] ctorAnnos = ctor.getAnnotations();
                if (ctorAnnos.length > 0)
                    b.append(" ").append(Arrays.toString(ctorAnnos));
                observed.add(b.toString());
            }

        for (Field f : c.getFields())
            if (c.equals(f.getDeclaringClass())) {
                b = new StringBuilder(f.toGenericString());
                Annotation[] fieldAnnos = f.getAnnotations();
                if (fieldAnnos.length > 0)
                    b.append(" ").append(Arrays.toString(fieldAnnos));
                observed.add(b.toString());
            }

        for (Method m : c.getMethods())
            if (c.equals(m.getDeclaringClass())) {
                b = new StringBuilder(m.toGenericString());
                Annotation[] methodAnnos = m.getAnnotations();
                if (methodAnnos.length > 0)
                    b.append(" ").append(Arrays.toString(methodAnnos));
                Object annoDefault = m.getDefaultValue();
                if (annoDefault != null)
                    if (annoDefault.getClass().isArray()) {
                        b.append(" default [");
                        for (int d = 0; d < Array.getLength(annoDefault); d++)
                            b.append(d == 0 ? "" : ",").append(Array.get(annoDefault, d));
                        b.append("]");
                    } else {
                        b.append(" default ").append(annoDefault);
                    }
                observed.add(b.toString());
            }

        String fileName = "/WEB-INF/signaturetest/" + className + ".sig";
        InputStream input = getServletContext().getResourceAsStream(fileName);
        if (input == null) {
            // When adding a new spec class, copy from System.out to create its expected .sig file,
            System.out.println("--- Missing signatures from");
            System.out.println(fileName);
            System.out.println("Observed signatures are:");
            for (String item : observed)
                System.out.println(item);

            return "Error: missing signatures file: " + fileName;
        }

        TreeSet<String> expected = new TreeSet<String>();
        BufferedReader in = new BufferedReader(new InputStreamReader(input));
        try {
            while (in.ready())
                expected.add(in.readLine());
        } finally {
            in.close();
        }

        TreeSet<String> missing = new TreeSet<String>(expected);
        missing.removeAll(observed);

        assertEquals(Collections.EMPTY_SET, missing,
                     "Found " + className + " that lacks the identified aspects of the specification class.");

        TreeSet<String> extras = new TreeSet<String>(observed);
        extras.removeAll(expected);

        assertEquals(Collections.EMPTY_SET, extras,
                     "Found " + className + " that differs from the specification class.");

        return SUCCESS;
    }
}
