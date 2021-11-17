package jakarta.enterprise.concurrent.tck.framework;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility method to ensure all classes use a common URL manipulation tool. 
 * baseURL will be provided by Arquillian using the <code>ArquillianResource</code>
 * annotation to get the URL of the servlet.
 */
public class URLBuilder {
	public static final String TEST_METHOD = "testMethod";

	private URL baseURL;
	private List<String> queries;
	private List<String> paths;
	private boolean testNameSet = false;

	private URLBuilder() {
		// Make constructor private so no instances can be created
	}

	/**
	 * Get the builder
	 */
	public static URLBuilder get() {
		return new URLBuilder();
	}

	/**
	 * Base URL obtained from <code>ArquillianResource</code>
	 */
	public URLBuilder withBaseURL(URL baseURL) {
		this.baseURL = baseURL;
		return this;
	}

	/**
	 * Additional queries to tack onto the end of the URL.
	 * Example:
	 *   baseURL = http://localhost:80/servlet/
	 *   query   = count=5
	 *   result  = http://localhost:80/servlet/?count=5
	 */
	public URLBuilder withQueries(String... queries) {
		if (this.queries == null) {
			this.queries = Arrays.asList(queries);
		} else {
			this.queries.addAll(Arrays.asList(queries));
		}
		return this;
	}

	/**
	 * Additional paths to tack onto the end of the URL.
	 * Example:
	 *   baseURL = http://localhost:80/servlet/
	 *   path    = app, inventory
	 *   result  = http://localhost:80/servlet/app/inventory
	 */
	public URLBuilder withPaths(String... paths) {
		if (this.paths == null) {
			this.paths = Arrays.asList(paths);
		} else {
			this.paths.addAll(Arrays.asList(paths));
		}
		return this;
	}

	/**
	 * Additional testName query to tack onto the end of the URL.
	 * Example: 
	 *   baseURL  = http://localhost:80/servlet/
	 *   testName = transactionTest
	 *   result   = http://localhost:80/servlet/?testMethod=transactionTest
	 */
	public URLBuilder withTestName(String testName) {
		if(testNameSet) {
			throw new UnsupportedOperationException("Cannot call withTestName more than once.");
		}
		
		String query = TEST_METHOD + "=" + testName;
		
		if (this.queries == null) {
			this.queries = Arrays.asList(query);
		} else {
			this.queries.add(query);
		}
		
		testNameSet = true;
		return this;
	}

	/**
	 * This will build the URL tacking on the additional queries, paths, and testName.
	 * @return
	 */
	public URL build() {
		if (baseURL == null) {
			throw new RuntimeException("Cannot build URL without a baseURL");
		}

		URL extendedURL = baseURL;

		extendedURL = extendQuery(extendedURL, queries);
		extendedURL = extendPath(extendedURL, paths);

		return extendedURL;
	}

	public static URL extendQuery(URL baseURL, List<String> queries) {
		if (queries == null)
			return baseURL;

		String extendedQuery = baseURL.getQuery();
		for (String queryPart : queries) {
			extendedQuery += "&" + queryPart;
		}
		try {
			return new URL(baseURL.getProtocol(), baseURL.getHost(), baseURL.getPort(),
					baseURL.getPath() + "?" + extendedQuery, null);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public static URL extendPath(URL baseURL, List<String> paths) {
		if (paths == null)
			return baseURL;

		String extendedPath = baseURL.getPath();
		for (String pathPart : paths) {
			extendedPath += "/" + pathPart;
		}
		try {
			return new URL(baseURL.getProtocol(), baseURL.getHost(), baseURL.getPort(),
					extendedPath + "?" + baseURL.getQuery(), null);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}
