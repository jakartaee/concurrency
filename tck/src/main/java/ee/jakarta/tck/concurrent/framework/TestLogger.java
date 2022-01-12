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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract out the logging framework so that in the future it can be replaced if needed.
 */
public final class TestLogger {
	private static final String nl = System.lineSeparator();

	private Logger log;

	/*
	 * Private constructor since there should only be one TestLogger per class
	 */
	private TestLogger(Class<?> clazz) {
		log = Logger.getLogger(clazz.getCanonicalName());
	}

	public static TestLogger get(Class<?> clazz) {
		return new TestLogger(clazz);
	}

	public void severe(String s) {
		log.severe(s);
	}

	public void severe(String s, Throwable t) {
		log.severe(messageWithThrowable(s, t));
	}

	public void warning(String s) {
		log.warning(s);
	}

	public void warning(String s, Throwable t) {
		log.warning(messageWithThrowable(s, t));
	}

	public void info(String s) {
		log.info(s);
	}

	public void info(String s, Throwable t) {
		log.info(messageWithThrowable(s, t));
	}
	
	public void info(String s, Object... objs) {
		log.log(Level.INFO, s + getObjectSuffix(objs), removeNewLines(objs));
	}

	public void config(String s) {
		log.config(s);
	}

	public void config(String s, Throwable t) {
		log.config(messageWithThrowable(s, t));
	}

	public void fine(String s) {
		log.fine(s);
	}

	public void fine(String s, Throwable t) {
		log.fine(messageWithThrowable(s, t));
	}

	public void finer(String s) {
		log.fine(s);
	}

	public void finer(String s, Throwable t) {
		log.finer(messageWithThrowable(s, t));
	}

	public void finest(String s) {
		log.fine(s);
	}

	public void finest(String s, Throwable t) {
		log.finest(messageWithThrowable(s, t));
	}

	public void enter(Method method, Object... objs) {
		log.log(Level.INFO, "--> " + method.getName() + getObjectSuffix(objs), removeNewLines(objs));
	}

	public void enter(String method, Object... objs) {
		log.log(Level.INFO, "--> " + method + getObjectSuffix(objs), removeNewLines(objs));
	}

	public void exit(Method method, Object... objs) {
		log.log(Level.INFO, "<-- " + method.getName() + getObjectSuffix(objs), removeNewLines(objs));
	}

	public void exit(String method, Object... objs) {
		log.log(Level.INFO, "<-- " + method + getObjectSuffix(objs), removeNewLines(objs));
	}

	private String messageWithThrowable(String s, Throwable t) {
		Writer buffer = new StringWriter();
		PrintWriter pw = new PrintWriter(buffer);
		t.printStackTrace(pw);
		return s + nl + buffer.toString();
	}
	
	private String getObjectSuffix(Object[] objs) {
		if (objs == null || objs.length == 0)
			return "";
		
		String suffix = nl + "[ ";
        for (int i = 0; i < objs.length; i++) {
        	suffix = suffix + "{" + i + "}, ";
        }
        return suffix.substring(0, suffix.length() -2) + " ]";
	}
	
	private String[] removeNewLines(Object[] objs) {
		if (objs == null || objs.length == 0)
			return new String[] {};
		
		String[] result = new String[objs.length];
		
		for (int i = 0; i < objs.length; i++) {
			if(objs[i] instanceof String) {
				result[i] = ((String) objs[i]).replace(nl, "");
			} else {
				result[i] = String.valueOf(objs[i]).replace(nl, "");
			}
		}

        return result;
	}
}
