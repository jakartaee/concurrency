package jakarta.enterprise.concurrent.tck.framework;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract out the logging framework so that in the future it can be replaced
 * if needed.
 */
public class TestLogger {
	private static final String nl = System.lineSeparator();

	private Class<?> c;
	private Logger log;

	/*
	 * Private constructor since this should be a singleton class
	 */
	private TestLogger(Class<?> clazz) {
		c = clazz;
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
		log.log(Level.CONFIG, "ENTER: " + c.getName() + "#" + method.getName(), objs);
	}

	public void enter(String method, Object... objs) {
		log.log(Level.CONFIG, "ENTER: " + c.getName() + "#" + method, objs);
	}

	public void exit(Method method, Object... objs) {
		log.log(Level.CONFIG, "EXIT: " + c.getName() + "#" + method.getName(), objs);
	}

	public void exit(String method, Object... objs) {
		log.log(Level.CONFIG, "EXIT: " + c.getName() + "#" + method, objs);
	}

	private String messageWithThrowable(String s, Throwable t) {
		return s + nl + t.getMessage();
	}
}
