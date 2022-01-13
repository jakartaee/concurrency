/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
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
package ee.jakarta.tck.concurrent.spec.signature;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.TreeSet;

import ee.jakarta.tck.concurrent.framework.TestLogger;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("SignatureTestServlet")
public class SignatureTestServlet extends TestServlet {
    private static final long serialVersionUID = 1L;
    
    private static final TestLogger log = TestLogger.get(SignatureTestServlet.class);

    /**
     * Perform signatures tests for the specified class.
     *
     * @param className fully qualified class name.
     */
    public void testSignatures(HttpServletRequest req, HttpServletResponse resp) throws Throwable {
    	String className = req.getParameter("action");
    	assertNotNull(className, "unknown or missing action for " + getClass().getName() + ": " + className);
    	
    	log.info("Class name is " + className);
    	
        TreeSet<String> observed = new TreeSet<String>();

        Class<?> c = Class.forName(className);

        StringBuilder b = new StringBuilder(200).append("#").append(c.toGenericString());
        if (c.getSuperclass() != null)
            b.append(" extends " + c.getSuperclass().getName());
        Class<?>[] i = c.getInterfaces();
        if (i.length > 0)
            b.append(c.isInterface() ? " extends " : " implements ").append(Arrays.toString(i));
        observed.add(b.toString());

        for (Annotation a : c.getAnnotations())
        	observed.add(toString(a));

        for (Constructor<?> ctor : c.getConstructors())
            if (c.equals(ctor.getDeclaringClass())) {
                b = new StringBuilder();
                Annotation[] ctorAnnos = ctor.getAnnotations();
                for (Annotation a : ctorAnnos)
                	b.append(toString(a)).append(" ");
                b.append(ctor.toGenericString());
                observed.add(b.toString());
            }

        for (Field f : c.getFields())
            if (c.equals(f.getDeclaringClass())) {
                b = new StringBuilder();
                Annotation[] fieldAnnos = f.getAnnotations();
                for (Annotation a : fieldAnnos)
                	b.append(toString(a)).append(" ");
                b.append(f.toGenericString());
                observed.add(b.toString());
            }

        for (Method m : c.getMethods())
            if (c.equals(m.getDeclaringClass())) {
                b = new StringBuilder();
                Annotation[] methodAnnos = m.getAnnotations();
                for (Annotation a : methodAnnos)
                	b.append(toString(a)).append(" ");
                b.append(m.toGenericString());
                Object annoDefault = m.getDefaultValue();
                if (annoDefault != null) {
                    b.append(" default ");
                    toStringBuilder(b, annoDefault);
                }
                observed.add(b.toString());
            }

        String fileName = "/WEB-INF/signaturetest/" + className.replace('$', '-') + ".sig";
        InputStream input = getServletContext().getResourceAsStream(fileName);
        if (input == null) {
            // When adding a new spec class, copy from System.out to create its expected .sig file,
            log.info("--- Missing signatures from");
            log.info(fileName);
            log.info("Observed signatures are:");
            for (String item : observed)
                log.info(item);
            
            fail("Error: missing signatures file: " + fileName);
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

        TreeSet<String> extras = new TreeSet<String>(observed);
        extras.removeAll(expected);
        
        assertEquals(missing, Collections.EMPTY_SET,
                "Found " + className + " that lacks the identified aspects of the specification class. " +
                                                "Instead includes " + extras.toString() + ".");

        assertEquals(extras, Collections.EMPTY_SET,
                     "Found " + className + " that differs from the specification class.");
    }
    
    private static String toString(Annotation a) {
        Class<?> type = a.annotationType();
        StringBuilder b = new StringBuilder()
                        .append("@")
                        .append(type.getName())
                        .append("(");
        int count = 0;
        for (Method m : type.getMethods())
            if (type.equals(m.getDeclaringClass()) && m.getParameterCount() == 0) {
                Object value;
                try {
                    value = m.invoke(a);
                } catch (IllegalAccessException | InvocationTargetException x) {
                    x.printStackTrace();
                    value = null;
                }
                if (value != null) {
                    if (++count > 1)
                        b.append(", ");
                    b.append(m.getName()).append("=");
                    toStringBuilder(b, value);
                }
            }
        b.append(")");
        return b.toString();
    }

    private static void toStringBuilder(StringBuilder b, Object value) {
        if (value instanceof Class) {
            b.append(((Class<?>) value).getName()).append(".class");
        } else if (value.getClass().isArray()) {
            b.append("{");
            for (int v = 0; v < Array.getLength(value); v++) {
                if (v > 0)
                    b.append(", ");
                toStringBuilder(b, Array.get(value, v));
            }
            b.append("}");
        } else {
            b.append(value);
        }
    }
}
