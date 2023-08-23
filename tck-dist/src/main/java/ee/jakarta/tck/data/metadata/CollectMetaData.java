/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package ee.jakarta.tck.data.metadata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;

import ee.jakarta.tck.concurrent.framework.junit.anno.Assertion;
import ee.jakarta.tck.concurrent.framework.junit.anno.Challenge;

/**
 * This is a utility class that will analyze the TCK and generate documentation for the following information:
 * 1. The name of tests that are disabled (due to challenges)
 * 2. The number of tests that need to pass for certification
 * 3. Expected JUnit output
 * 4. Expected Signature test output
 *
 * This will be run automatically each time the tck-dist module is built.
 * Meaning that we don't need to update our doc each time we add/remove/disable a test.
 *
 * Files are output to:
 *  ${project.basedir}/src/main/asciidoc/generated/
 *
 * File names are:
 *  expected-output.adoc
 *  expected-sig-output.adoc
 *  runtime-tests.adoc
 *  successful-challenges.adoc
 */
public final class CollectMetaData {
    // Constants
    private static final String FRAMEWORK_PACKAGE_PREFIX = "ee/jakarta/tck/concurrent/framework";
    
    private static final String RUNTIME_TESTS_FILE = "runtime-tests.adoc";
    private static final String CHALLENGED_TESTS_FILE = "successful-challenges.adoc";
    private static final String SIG_OUTPUT_FILE = "expected-sig-output.adoc";
    private static final String EXPECTED_OUTPUT_FILE = "expected-output.adoc";
    private static final String TEST_PROPERTIES_FILE = "test-properties.adoc";
        
    // Data holders
    private static boolean debug = false;
    private static List<String> apiPackages;
    private static List<Class<?>> testClasses;
    private static File adocGeneratedLocation;
    
    private CollectMetaData() {
        //Do nothing
    }
    
    /**
     * Main method, expects three inputs:
     * 0. boolean - enable debug
     * 1. path to TCK jar - used to collect metadata
     * 2. output directory - where generated pages should be output
     *
     * @param args - the arguments needed to run
     * @throws Exception - thrown if any documentation cannot be generated
     */
    public static void main(final String[] args) throws Exception {
        if (args.length != 3) {
            throw new RuntimeException("CollectMetaData expected exactly 3 arguments [debug, path-to-tck, output-file-location]");
        }
        
        //Load arguments
        debug = Boolean.valueOf(args[0]);
        testClasses = getClassNames(args[1]);
        adocGeneratedLocation = new File(args[2]);
        
        //Check asciidoctor generated folder exists
        if (!adocGeneratedLocation.exists()) {
            adocGeneratedLocation.mkdirs();
        }
        
        //Collect test metadata
        final List<TestMetaData> testMetaData = collectMetaData();
        
        //Write the generated asciidoc files
        writeTestCounts(testMetaData, new File(adocGeneratedLocation, RUNTIME_TESTS_FILE));
        writeSuccessfulChallenges(testMetaData, new File(adocGeneratedLocation, CHALLENGED_TESTS_FILE));
        writeSigOutput(new File(adocGeneratedLocation, SIG_OUTPUT_FILE));
        writeOutput(testMetaData, new File(adocGeneratedLocation, EXPECTED_OUTPUT_FILE));
        writeGitIgnore(new File(adocGeneratedLocation, ".gitignore"), RUNTIME_TESTS_FILE, CHALLENGED_TESTS_FILE, SIG_OUTPUT_FILE, EXPECTED_OUTPUT_FILE, TEST_PROPERTIES_FILE);
    }

    /**
     * Writes generated files to a .gitignore file.
     *
     * @param outputLocation - the output file
     * @param ignoredFiles   - files to ignore
     * @throws IOException   - exception if we cannot write to this location
     */
    private static void writeGitIgnore(final File outputLocation, final String... ignoredFiles) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputLocation))) {
            for (String ignoredFile : ignoredFiles) {
                writer.write(ignoredFile + System.lineSeparator());
            }
        }
    }
    
    /**
     * Writes example output to the generated adoc folder.
     *
     * @param testMetaData   - the test metadata we previously collected
     * @param outputLocation - the output file
     * @throws IOException   - exception if we cannot write to this location
     */
    private static void writeOutput(final List<TestMetaData> testMetaData, final File outputLocation) throws IOException {
        String output =
                """
                [source, txt]
                ----
                $ mvn clean test
                ...
                [INFO] --- maven-surefire-plugin:3.0.0-M7:test (default-test) @ tck.runner ---
                [INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
                [INFO]
                [INFO] -------------------------------------------------------
                [INFO]  T E S T S
                [INFO] -------------------------------------------------------
                $indiviualTests
                [INFO] Results:
                [INFO]
                $totalTests
                [INFO]
                [INFO] -------------------------------------------------------
                [INFO] BUILD SUCCESS
                [INFO] -------------------------------------------------------
                [INFO] Total time:  xx.xxx s
                [INFO] Finished at: yyyy-mm-ddThh:mm:ss.mmmm
                [INFO] -------------------------------------------------------
                ----"""
                .replaceAll("\\$indiviualTests", getIndividualTests(testMetaData))
                .replaceAll("\\$totalTests", getTotalTests(testMetaData));
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputLocation))) {
            writer.write(output.trim() + System.lineSeparator());
        }
    }
    
    /**
     * Collect all distinct test classes, count the number of test classes that should be run, and return to the output.
     *
     * @param testMetaData
     * @return String output
     */
    private static String getIndividualTests(final List<TestMetaData> testMetaData) {
        StringBuffer output = new StringBuffer();
        final String nl = System.lineSeparator();
        for (String testClass : testMetaData.stream().map(metaData -> metaData.testClass).distinct().collect(Collectors.toList())) {
            List<TestMetaData> theseTests = testMetaData.stream().filter(metaData -> metaData.testClass == testClass).collect(Collectors.toList());
            long testCount = theseTests.stream().filter(metaData -> !metaData.isDisabled).count();
            long disabledCount = theseTests.stream().filter(metaData -> metaData.isDisabled).count();
            output.append("[INFO] Running " + testClass + nl);
            
            if (disabledCount > 0) {
                output.append("[WARNING] Tests run: " + testCount + ", Failures: 0, Errors: 0, Skipped: " + disabledCount + ",");
            } else {
                output.append("[INFO] Tests run: " + testCount + ", Failures: 0, Errors: 0, Skipped: " + disabledCount + ",");
            }
            
            output.append("Time elapsed: y.yy s - in " +  testClass + nl);
            output.append("[INFO]" + nl);
        }
        return output.toString().trim();
    }
    
    /**
     * Collect the total number of tests, and return the output.
     *
     * @param testMetaData
     * @return String output
     */
    private static String getTotalTests(final List<TestMetaData> testMetaData) {
        long totalTestCount = testMetaData.stream().filter(metaData -> !metaData.isDisabled).count();
        long totalDisabledCount = testMetaData.stream().filter(metaData -> metaData.isDisabled).count();
        
        if (totalDisabledCount > 0) {
            return "[WARNING] Tests run: " + totalTestCount + ", Failures: 0, Errors: 0, Skipped: " + totalDisabledCount;
        } else {
            return "[INFO] Tests run: " + totalTestCount + ", Failures: 0, Errors: 0, Skipped: " + totalDisabledCount;
        }

    }
    
    /**
     * Writes example signature test output to the generated adoc folder.
     *
     * @param outputLocation - the output file
     * @throws IOException   - exception if we cannot write to this location
     */
    private static void writeSigOutput(final File outputLocation) throws IOException {
        String output =
                """
                [source, txt]
                ----
                ******************************************************
                All package signatures passed.
                    Passed packages listed below:
                $packages
                ******************************************************
                ----""".replaceAll("\\$packages", getPackages());
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputLocation))) {
            writer.write(output.trim() + System.lineSeparator());
        }
    }
    
    /**
     * Collect API packages and return the output.
     *
     * @return String output
     */
    private static String getPackages() {
        String output = "";
        for (String apiPackage : apiPackages) {
            output +=
                    """
                    $package(static mode)
                    $package(reflection mode)
                    """.indent(8).replaceAll("\\$package", apiPackage);
        }
        return output;
    }

    /**
     * Writes successful challenges to the generated adoc folder.
     *
     * @param testMetaData   - the test metadata we previously collected
     * @param outputLocation - the output file
     * @throws IOException   - exception if we cannot write to this location
     */
    private static void writeSuccessfulChallenges(final List<TestMetaData> testMetaData, final File outputLocation) throws IOException {
        String output =
                """
                |===
                |Class |Method |Link |Version
                $disabledTests
                |===""".replaceAll("\\$disabledTests", getDisabledTests(testMetaData));
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputLocation))) {
            writer.write(output.trim() + System.lineSeparator());
        }
        
    }
    
    private static String getDisabledTests(final List<TestMetaData> testMetaData) {
        List<TestMetaData> disabledTests = testMetaData.stream().filter(TestMetaData::isDisabled).toList();
        String output = "";
        for (TestMetaData disabledTest : disabledTests) {
            output +=
                    """
                    
                    |%s |%s |%s |%s
                    """.formatted(disabledTest.testClass.substring(disabledTest.testClass.lastIndexOf('.') + 1),
                            disabledTest.testName, disabledTest.challengeIssue, disabledTest.challengeVersion);
        }
        return output;
    }

    /**
     * Writes expected test counts output to the generated adoc folder.
     *
     * @param testMetaData   - the test metadata we previously collected
     * @param outputLocation - the output file
     * @throws IOException   - exception if we cannot write to this location
     */
    private static void writeTestCounts(final List<TestMetaData> testMetaData, final File outputLocation) throws IOException {
        String output =
                """
                |===
                |standalone |core |web |full
                
                |%d         |%d   |%d  |%d

                |===""".formatted(getTestCounts(testMetaData));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputLocation))) {
            writer.write(output.trim() + System.lineSeparator());
        }
    }
    
    private static Object[] getTestCounts(final List<TestMetaData> testMetaData) {
        List<TestMetaData> runnableTestMetaData = testMetaData.stream().filter(TestMetaData::isRunnable).collect(Collectors.toList());
        
        List<Object> results = new ArrayList<>();
        
        results.add(runnableTestMetaData.stream().filter(TestMetaData::isStandalone).count());
        results.add(runnableTestMetaData.stream().filter(TestMetaData::isCore).count());
        results.add(runnableTestMetaData.stream().filter(TestMetaData::isWeb).count());
        results.add(runnableTestMetaData.stream().filter(TestMetaData::isFull).count());
        
        return results.toArray();
    }
    
    /**
     * Inspects each class for methods and annotations and constructs a metadata object.
     * Collects all metadata objects and returns them as a list.
     *
     * @return list of metadata for each test
     */
    private static List<TestMetaData> collectMetaData() {
        return testClasses.stream()
                .flatMap(clazz -> AnnotationSupport.findAnnotatedMethods(clazz, Test.class, HierarchyTraversalMode.TOP_DOWN).stream())
                .map(method -> {
                    boolean isDisabled = method.isAnnotationPresent(Challenge.class);
                    TestMetaData metaData = new TestMetaData(
                            method.getDeclaringClass().getCanonicalName(),
                            method.getName(),
                            method.getAnnotation(Assertion.class).strategy(),
                            isDisabled,
                            isDisabled ? method.getAnnotation(Challenge.class).link() : "",
                            isDisabled ? method.getAnnotation(Challenge.class).version() : "",
                            findTags(method.getDeclaringClass())
                            );
                    return metaData;
                }).collect(Collectors.toList());
    }
    
    /**
     * Finds the tag(s) on a test class which could be a {@Tag} or {@Tags} annotation.
     *
     * @param clazz - the test class
     * @return - a list of tag values on this class
     */
    private static List<String> findTags(final Class<?> clazz) {
        return Arrays.stream(clazz.getAnnotations())
        // Get 1st level nested annotations
        .flatMap(anno -> Stream.concat(Arrays.stream(anno.annotationType().getAnnotations()), Stream.of(anno)))
        // Get all tag annotations
        .flatMap(anno -> {
            if (anno instanceof Tag)
                return Stream.of((Tag) anno);
            if (anno instanceof Tags)
                return Arrays.stream(((Tags) anno).value());
            return Stream.empty();
        })
        .map(anno -> anno.value())
        .collect(Collectors.toList());
    }
    
    /**
     * Finds and loads all test classes inside of a TCK jar.
     *
     * @param jarLocation - Path to the TCK jar
     * @return List of test classes
     * @throws Exception - throws exception if jar cannot be located, or classes cannot be loaded.
     */
    private static List<Class<?>> getClassNames(final String jarLocation) throws Exception {
        ArrayList<Class<?>> classList = new ArrayList<>();
        
        try (JarInputStream jar = new JarInputStream(new FileInputStream(jarLocation));) {
            for (JarEntry entry = jar.getNextJarEntry(); entry != null; entry = jar.getNextJarEntry()) {
                if (isTestClass(entry.getName())) {
                    debug("Attempting to load test class: " + entry.getName());
                    classList.add(getClass(entry.getName().replaceAll("/", "\\.")));
                } else if (entry.getName().contains("sig-test-pkg-list.txt")) {
                    debug("Attempting to read package list" + entry.getName());
                    apiPackages = new String(jar.readAllBytes(), StandardCharsets.UTF_8).lines()
                        .filter(line -> !line.contains("#"))
                        .filter(line -> !line.isBlank())
                        .collect(Collectors.toList());
                    debug("apiPackages populated with: " + apiPackages.toString());
                }
                jar.closeEntry();
            }
        }
        
        return classList;
    }
    
    /**
     * Determines if a jar resource is a test class or not.
     *
     * @param entryName - The fully qualified resource name
     * @return true - if resource is a test class, false otherwise
     */
    private static boolean isTestClass(final String entryName) {
        if (!entryName.endsWith(".class"))
            return false;
        if (entryName.contains(FRAMEWORK_PACKAGE_PREFIX))
            return false;
        if (!entryName.substring(entryName.lastIndexOf("/"), entryName.lastIndexOf(".")).toLowerCase().contains("tests"))
            return false;
            
        return true;
    }
 
    /**
     * Loads a given class and returns it.
     *
     * @param className - The name of the class with or without the .class suffix
     * @return The class object
     */
    private static Class<?> getClass(final String className) {
        try {
            return Class.forName(className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Prints message if debugging is enabled.
     *
     * @param message - The message to print
     */
    private static void debug(final String message) {
        if (debug)
            System.out.println(message);
    }
    
    /**
     * A data structure that represents data associated with test methods.
     *
     * @param testClass - Name of the test class.
     * @param testName - Name of the test method.
     * @param assertion - The test strategy.
     * @param isDisabled - True, if the test is disabled, false otherwise.
     * @param challengeIssue - The issue number if the test is challenged.
     * @param challengeVersion - The spec version if the test is challneged.
     * @param tags - A list of all the tags on the test class.
     */
    public record TestMetaData(String testClass, String testName, String assertion,
            boolean isDisabled, String challengeIssue, String challengeVersion, List<String> tags) {
        
        boolean isStandalone() {
            return tags.contains("standalone");
        }
        
        boolean isCore() {
            return tags.contains("core");
        }
        
        boolean isWeb() {
            return tags.contains("web");
        }
        
        boolean isFull() {
            return tags.contains("full");
        }
    
        boolean isRunnable() {
            return !isDisabled;
        }
        
        @Override
        public String toString() {
            return "TestMetaData [testName=" + testName + ", assertion=" + assertion + ", isDisabled=" + isDisabled
                    + ", challengeIssue=" + challengeIssue + ", challengeVersion" + challengeVersion + ", tags=" + tags + "]";
        }
    }
}
