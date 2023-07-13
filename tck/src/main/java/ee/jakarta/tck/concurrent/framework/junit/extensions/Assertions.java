package ee.jakarta.tck.concurrent.framework.junit.extensions;

import java.util.Iterator;

/**
 * Helper class for custom assertions not supported by JUnit 5
 */
public class Assertions {

    private Assertions() {
        // helper method
    }

    /**
     * Asserts expected integer is within the range ( lowerBound, upperBound )
     * (exclusive).
     */
    public static void assertWithin(int expected, int lowerBound, int upperBound) {
        if (lowerBound < expected && upperBound > expected) {
            return; // pass
        }

        String message = "Expected " + expected + " to be within the range ( " + lowerBound + ", " + upperBound + " )";
        throw new AssertionError(message);
    }

    /**
     * Asserts expected integer is within the range [ lowerBound, upperBound ]
     * [inclusive].
     */
    public static void assertBetween(int expected, int lowerBound, int upperBound) {
        if (lowerBound <= expected && upperBound >= expected) {
            return; // pass
        }

        String message = "Expected " + expected + " to be within the range [ " + lowerBound + ", " + upperBound + " ]";
        throw new AssertionError(message);
    }

    /**
     * Asserts expected object is within a range represented by an Iterable
     */
    public static void assertRangeContains(Object expected, Iterable<Object> range) {
        Iterator<?> it = range.iterator();
        while (it.hasNext()) {
            if (it.equals(expected)) {
                return; // pass
            }
        }

        String message = "Expected " + expected + " to be within the range " + range.toString();
        throw new AssertionError(message);
    }

}
