package jakarta.enterprise.concurrent.util;

import java.util.Properties;

@SuppressWarnings("serial")
public class TestProperties extends Properties {

	/**
	 * Searches for an existing property with the specified key in the superclasse's
	 * property list. If not found in the superclasse's property list, search for
	 * specified key in system property list. The method returns {@code null} if the
	 * property is not found.
	 */
	@Override
	public String getProperty(String key) {
		String val = super.getProperty(key);
		if (val == null) {
			val = System.getProperty(key);
			if (val != null) {
				this.setProperty(key, val);
			}
		}
		return val;
	}
}
