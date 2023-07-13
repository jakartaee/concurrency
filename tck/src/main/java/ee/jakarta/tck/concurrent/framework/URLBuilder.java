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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility method to ensure all classes use a common URL manipulation tool.
 * baseURL will be provided by Arquillian using the
 * <code>ArquillianResource</code> annotation to get the URL of the servlet.
 */
public final class URLBuilder {
    private static final TestLogger log = TestLogger.get(URLBuilder.class);

    public static final String TEST_METHOD = "testMethod";

    private URL baseURL;
    private ArrayList<String> queries;
    private ArrayList<String> paths;
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
    public URLBuilder withBaseURL(final URL baseURL) {
        this.baseURL = baseURL;
        return this;
    }

    /**
     * Additional queries to tack onto the end of the URL. Example: baseURL =
     * http://localhost:80/servlet/ query = count=5 result =
     * http://localhost:80/servlet/?count=5
     */
    public URLBuilder withQueries(final String... queries) {
        if (this.queries == null) {
            this.queries = new ArrayList<>(Arrays.asList(queries));
        } else {
            this.queries.addAll(Arrays.asList(queries));
        }
        return this;
    }

    /**
     * Additional paths to tack onto the end of the URL. Example: baseURL =
     * http://localhost:80/servlet/ path = app, inventory result =
     * http://localhost:80/servlet/app/inventory
     */
    public URLBuilder withPaths(final String... paths) {
        if (this.paths == null) {
            this.paths = new ArrayList<>(Arrays.asList(paths));
        } else {
            this.paths.addAll(Arrays.asList(paths));
        }
        return this;
    }

    /**
     * Additional testName query to tack onto the end of the URL. Example: baseURL =
     * http://localhost:80/servlet/ testName = transactionTest result =
     * http://localhost:80/servlet/?testMethod=transactionTest
     */
    public URLBuilder withTestName(final String testName) {
        if (testNameSet) {
            throw new UnsupportedOperationException("Cannot call withTestName more than once.");
        }

        String query = TEST_METHOD + "=" + testName;

        if (this.queries == null) {
            this.queries = new ArrayList<>(Arrays.asList(query));
        } else {
            this.queries.add(query);
        }

        testNameSet = true;
        return this;
    }

    /**
     * This will build the URL tacking on the additional queries, paths, and
     * testName.
     */
    public URL build() {
        if (baseURL == null) {
            throw new RuntimeException("Cannot build URL without a baseURL");
        }

        log.enter("build", baseURL, queries, paths);

        URL extendedURL = baseURL;

        extendedURL = extendQuery(extendedURL, queries);
        extendedURL = extendPath(extendedURL, paths);

        log.exit("build", extendedURL);

        return extendedURL;
    }

    public static URL extendQuery(final URL baseURL, final List<String> queries) {
        if (queries == null)
            return baseURL;

        // Get existing query part
        boolean existingQuery = baseURL.getQuery() != null;
        String extendedQuery = existingQuery ? "?" + baseURL.getQuery() : "?";

        // Append additional query parts
        for (String queryPart : queries) {
            extendedQuery += queryPart + "&";
        }

        // Cleanup trailing symbol(s)
        extendedQuery = extendedQuery.substring(0, extendedQuery.length() - 1);

        // Generate and return new URL
        try {
            return new URL(baseURL.getProtocol(), baseURL.getHost(), baseURL.getPort(),
                    baseURL.getPath() + extendedQuery, null);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static URL extendPath(final URL baseURL, final List<String> paths) {
        if (paths == null)
            return baseURL;

        // Get existing path part
        boolean existingPath = baseURL.getPath() != null;
        String extendedPath = existingPath ? baseURL.getPath() : "";

        // Append additional path parts
        for (String pathPart : paths) {
            pathPart = pathPart.replace("/", ""); // Remove existing /
            extendedPath += pathPart + "/";
        }

        // cleanup trailing symbol(s)
        extendedPath = extendedPath.substring(0, extendedPath.length() - 1);

        // Generate and return new URL
        try {
            return new URL(baseURL.getProtocol(), baseURL.getHost(), baseURL.getPort(),
                    extendedPath + (baseURL.getQuery() == null ? "" : "?" + baseURL.getQuery()), null);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
