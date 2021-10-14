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

package jakarta.enterprise.concurrent;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.junit.Test;

/**
 * This test includes the code examples from the specification and JavaDoc
 * for asychronous methods, verifying that they compile.
 * The Asynchronous.Result class is also tested.
 */
public class AsynchronousTest {
    /**
     * Example from the section on Asynchronous Methods in the Concurrency spec.
     */
    @Asynchronous(executor = "java:module/env/concurrent/myExecutorRef")
    public CompletableFuture<Set<Item>> findSimilar(Cart cart, History h) {
        Set<Item> combined = new LinkedHashSet<Item>();
        for (Item item : cart.items())
            combined.addAll(item.similar());
        for (Item item : h.recentlyViewed(3))
            combined.addAll(item.similar());
        combined.removeAll(cart.items());

        try (Connection con = ((DataSource) InitialContext.doLookup(
                              "java:comp/env/jdbc/ds1")).getConnection()) {
            PreparedStatement stmt = con.prepareStatement("...");
            for (Item item : combined) {
                // ... Remove if the similar item is unavailable
            }
        } catch (NamingException | SQLException x) {
            throw new CompletionException(x);
        }
        return Asynchronous.Result.complete(combined);
    }

    /**
     * Second example from the Asynchronous class JavaDoc.
     */
    @Asynchronous
    public CompletableFuture<List<Itinerary>> findSingleLayoverFlights(Location source, Location dest) {
        try {
            ManagedExecutorService executor = InitialContext.doLookup(
                "java:comp/DefaultManagedExecutorService");

            return executor.supplyAsync(source::flightsFrom)
                           .thenCombine(executor.completedFuture(dest.flightsTo()),
                                        Itinerary::destMatchingSource);
        } catch (NamingException x) {
            throw new CompletionException(x);
        }
    }

    /**
     * First example from the Asynchronous class JavaDoc.
     */
    @Asynchronous
    public CompletableFuture<Double> hoursWorked(LocalDate from, LocalDate to) {
        // Application component's context is made available to the async method,
        try (Connection con = ((DataSource) InitialContext.doLookup(
            "java:comp/env/jdbc/timesheetDB")).getConnection()) {
            // ...
            double total = 40.0; // added to make the example compile
            return Asynchronous.Result.complete(total);
        } catch (NamingException | SQLException x) {
            throw new CompletionException(x);
        }
    }

    /**
     * Example from the Asynchronous.Result class JavaDoc.
     */
    @Asynchronous
    public CompletableFuture<Double> hoursWorked(LocalDateTime from, LocalDateTime to) {
        CompletableFuture<Double> future = Asynchronous.Result.getFuture();
        if (future.isDone())
            return future;

        try (Connection con = ((DataSource) InitialContext.doLookup(
            "java:comp/env/jdbc/timesheetDB")).getConnection()) {
            PreparedStatement stmt = con.prepareStatement("SQL"); // added to make the example compile
            // ...
            for (ResultSet result = stmt.executeQuery(); result.next() && !future.isDone(); )
                ; // ...
            double total = 40.0; // added to make the example compile
            future.complete(total);
        } catch (NamingException | SQLException x) {
            future.completeExceptionally(x);
        }
        return future;
    }

    /**
     * Used by Concurrency spec example code under Asynchronous Methods section.
     * This code doesn't do anything; we only need the method signatures to verify compilation of the example.
     */
    static class Cart {
        public List<Item> items() {
            return Collections.emptyList();
        }
    }

    /**
     * Used by Concurrency spec example code under Asynchronous Methods section.
     * This code doesn't do anything; we only need the method signatures to verify compilation of the example.
     */
    static class Customer {
        public Cart getCart() {
            return new Cart();
        }

        public History getHistory() {
            return new History();
        }
    }

    /**
     * Used by Concurrency spec example code under Asynchronous Methods section.
     * This code doesn't do anything; we only need the method signatures to verify compilation of the example.
     */
    static class History {
        public Set<Item> recentlyViewed(int max) {
            return Collections.emptySet();
        }
    }

    /**
     * Used by Concurrency spec example code under Asynchronous Methods section.
     * This code doesn't do anything; we only need the method signatures to verify compilation of the example.
     */
    static class Item {
        public Set<Item> similar() {
            return Collections.emptySet();
        }
    }

    /**
     * Used by example from Asynchronous class JavaDoc.
     * This code doesn't do anything; we only need the method signatures to verify compilation of the example.
     */
    static class Itinerary {
        static List<Itinerary> destMatchingSource(List<Itinerary> toDest, List<Itinerary> fromSource) {
            return Collections.emptyList();
        }
        static List<Itinerary> sortByPrice(List<Itinerary> list) {
            return list;
        }
    }

    /**
     * Used by example from Asynchronous class JavaDoc.
     * This code doesn't do anything; we only need the method signatures to verify compilation of the example.
     */
    static class Location {
        List<Itinerary> flightsFrom() {
            return Collections.emptyList();
        }
        List<Itinerary> flightsTo() {
            return Collections.emptyList();
        }
    }

    /**
     * Verify that the first usage example in the Asynchronous class JavaDoc compiles.
     */
    @Test
    public void testAsynchronousJavaDocUsageExample1() {
        LocalDateTime mon = LocalDateTime.of(2021, 9, 13, 0, 0, 0);
        LocalDateTime fri = LocalDateTime.of(2021, 9, 17, 23, 59, 59);

        // Normally, the Jakarta EE Product Provider would do this on the same
        // thread where the asynchronous method executes, just before it starts.
        CompletableFuture<Double> future = new CompletableFuture<Double>();
        Asynchronous.Result.setFuture(future);

        // There is no interceptor when running these tests, so this won't actually run async.
        try {
            hoursWorked(mon, fri).thenAccept(total -> {
                try {
                    DataSource ds = InitialContext.doLookup(
                        "java:comp/env/jdbc/payrollDB");
                    // ...
                } catch (NamingException x) {
                    throw new CompletionException(x);
                }
            });
        } finally {
            // Normally, the Jakarta EE Product Provider would do this on the same
            // thread where the asynchronous method executes, just after it ends.
            Asynchronous.Result.setFuture(null);
        }

        // Naming lookup will have failed,
        assertTrue(future.isCompletedExceptionally());
    }

    /**
     * Verify that the second usage example in the Asynchronous class JavaDoc compiles.
     */
    @Test
    public void testAsynchronousJavaDocUsageExample2() {
        Location RST = new Location();
        Location DEN = new Location();

        try {
            CompletableFuture<List<Itinerary>> leastToMostExpensive =
                findSingleLayoverFlights(RST, DEN).thenApply(Itinerary::sortByPrice);
        } catch (CompletionException x) {
            // Expected in these tests because this is no interceptor to run the async method on another thread
        }
    }

    /**
     * Verify that the Asynchronous annotation can be configured at method level
     * and that its executor field defaults to the JNDI name of the
     * built-in ManagedExecutorService that is provided by the Jakarta EE platform.
     */
    @Test
    public void testAsynchronousMethodLevelAnnotation() throws Exception {
        Asynchronous anno = AsynchronousTest.class
                .getMethod("hoursWorked", LocalDate.class, LocalDate.class)
                .getAnnotation(Asynchronous.class);
        assertNotNull(anno);
        assertEquals("java:comp/DefaultManagedExecutorService", anno.executor());
    }

    /**
     * Invoke all of the static methods of Asynchronous.Result.
     */
    @Test
    public void testAsynchronousResult() {
        try {
            fail("Must not be present by default: " + Asynchronous.Result.getFuture());
        } catch (IllegalStateException x) {
            // Pass - detected that this was not invoked from an async method
        }

        CompletableFuture<String> stringFuture = new CompletableFuture<String>();
        Asynchronous.Result.setFuture(stringFuture);
        assertEquals(stringFuture, Asynchronous.Result.getFuture());
        Asynchronous.Result.setFuture(null);
        try {
            fail("Must not be present after removing: " + Asynchronous.Result.getFuture());
        } catch (IllegalStateException x) {
            // Pass - detected that this was not invoked from an async method
        }

        CompletableFuture<Integer> intFuture = new CompletableFuture<Integer>();
        Asynchronous.Result.setFuture(intFuture);
        assertEquals(intFuture, Asynchronous.Result.complete(100));
        Asynchronous.Result.setFuture(null);
        try {
            fail("Must not be present after removing: " + Asynchronous.Result.getFuture());
        } catch (IllegalStateException x) {
            // Pass - detected that this was not invoked from an async method
        }

        CompletableFuture<Boolean> booleanFuture = new CompletableFuture<Boolean>();
        Asynchronous.Result.setFuture(booleanFuture);
        assertEquals(booleanFuture, Asynchronous.Result.getFuture());
        assertEquals(booleanFuture, Asynchronous.Result.complete(true));
        Asynchronous.Result.setFuture(null);
        try {
            fail("Must not be present after removing: " + Asynchronous.Result.getFuture());
        } catch (IllegalStateException x) {
            // Pass - detected that this was not invoked from an async method
        }

        CompletableFuture<Void> unusedFuture = new CompletableFuture<Void>();
        Asynchronous.Result.setFuture(unusedFuture);
        Asynchronous.Result.setFuture(null);
        try {
            fail("Must not be present after removing: " + Asynchronous.Result.getFuture());
        } catch (IllegalStateException x) {
            // Pass - detected that this was not invoked from an async method
        }
    }

    /**
     * Verify that the usage example in the Asynchronous Methods section of the
     * Concurrency spec compiles.
     */
    @Test
    public void testAsynchronousUsageExampleFromSpec() throws Exception {
        Customer cust = new Customer();

        try {
            findSimilar(cust.getCart(), cust.getHistory())
                .thenAccept(recommended -> {
                    // ... Update page with recommendations
                }).join();
        } catch (CompletionException x) {
            // Naming lookup will have failed because this doesn't run in a real server.
        }
    }
}
