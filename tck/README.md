# Jakarta Concurrency TCK User's Guide

The Jakarta Concurrency Technology Compatibility Kit (TCK) is an automated test project that
will certify that Application Servers which implement the Jakarta Concurrency API meet the specification
requirements.

## Background

This guide will help you build and run the TCK on a supported Jakarta EE Application Server or Container.

### System Requirements
You will need to download and install the following software before you begin.

- [JDK 11 or higher](https://adoptopenjdk.net/?variant=openjdk11)
- [Maven 3.6.0 or higher](https://maven.apache.org/download.cgi)
- Jakarta EE Application Server or Container [Glassfish, Open Liberty, JBoss, WebLogic, etc.]

### Testing Framework
To better understand how this TCK works, knowing what testing frameworks are being utilized is helpful.
Knowledge of how these frameworks operate and interact will help during the project setup.

- [TestNG](https://testng.org/doc/documentation-main.html) is used for test execution and lifecycle.
- [Arquillian](https://arquillian.org/guides/) is used for application management and deployment.

## Getting Started

### Building the TCK

To build the TCK locally, first clone this repository and then use the Maven install goal to create the API and TCK modules.

```sh
git clone git@github.com:jakartaee/concurrency.git
cd concurrency-api
mvn install
```

### Getting the TCK
Including the TCK as a dependency in your project can be obtained from Maven Central.

```xml
<dependency>
    <groupId>jakarta.enterprise.concurrent</groupId>
    <artifactId>jakarta.enterprise.concurrent-tck</artifactId>
    <version>3.0.0</version>
</dependency>
```

## Understanding the TCK

The TCK acts as a `Test Client` that will install test applications onto your Application Server.
The Application Server will act as a `Test Server` and run tests based on incoming requests from the `Test Client`.
Assertions will occur both on the client and server sides. 

### What is included
The TCK is a test library that includes four types of packages:

- `ee.jakarta.tck.concurrent.api.*` these are basic API tests that ensure methods throw the correct exceptions and return the valid values.
- `ee.jakarta.tck.concurrent.spec.*` these are more complex SPEC tests that ensure that implementations behave as expected based on the specification.
- `ee.jakarta.tck.concurrent.common.*` these are common packages shared between test packages.
- `ee.jakarta.tck.concurrent.framework` this package is an abstraction layer to make writing tests using TestNG, Arquillian, SigTest, and java.util.logging easier.

### What is not included
The TCK uses but does not provide the necessary application servers, test frameworks, APIs, SPIs, or implementations required to run. 
It is up to the tester to include those dependencies and set up a test project to run the TCK.

Here is an essential checklist of what you will need, and in the later sections, we will give an example configuration for how to satisfy these requirements:
- An Application Server to test against
- An Application Server configuration with specific security roles
- An Arquillian library available to the `Test Client`
- An Arquillian SPI implementation for your Application Server
- A TestNG library available to both the `Test Client` and `Test Server`
- A TestNG configuration file available to the `Test Client`
- A Derby JDBC driver available to the Test Server

## Set up your project

### `Test Client` Dependencies
The entry point to running the TCK will be on the client-side using TestNG. 
The `Test Client` will need to be configured with the dependencies necessary to run the TCK.
Some of these dependencies will depend on the application server you are using, and 
comments have been added to this sample describing the customizations necessary.

Example /pom.xml:

```xml
<!-- The Arquillian test framework -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.jboss.arquillian</groupId>
            <artifactId>arquillian-bom</artifactId>
            <version>1.6.0.Final</version>
            <scope>import</scope>
            <type>pom</type>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- The TCK -->
    <dependency>
        <groupId>jakarta.enterprise.concurrent</groupId>
        <artifactId>jakarta.enterprise.concurrent-tck</artifactId>
        <version>3.0.0</version>
    </dependency>
    <!-- APIs -->
    <dependency>
        <groupId>jakarta.enterprise.concurrent</groupId>
        <artifactId>jakarta.enterprise.concurrent-api</artifactId>
        <version>3.0.0</version>
    </dependency>
    <dependency>
        <groupId>jakarta.servlet</groupId>
        <artifactId>jakarta.servlet-api</artifactId>
        <version>6.0.0</version>
    </dependency>
    <!-- Test frameworks -->
    <dependency>
        <groupId>org.testng</groupId>
        <artifactId>testng</artifactId>
        <version>6.14.3</version>
    </dependency>
    <dependency>
        <groupId>org.jboss.arquillian.testng</groupId>
        <artifactId>arquillian-testng-container</artifactId>
        <version>1.6.0.Final</version>
    </dependency>
    <!-- Container specific implementation of the Arquillian SPI -->
</dependencies>
```
### `Test Server` Dependencies

If the `Test Server` is running on a separate JVM (recommended), then the `Test Server` 
will also need access to the TestNG library and the Derby JDBC driver for database testing.
The `Test Server` dependencies are copied over during the build phase.

Example /pom.xml:

```xml
<plugin>
<groupId>org.apache.maven.plugins</groupId>
<artifactId>maven-dependency-plugin</artifactId>
<version>3.2.0</version>
<executions>
    <execution>
    <id>copy-dependencies</id>
    <phase>package</phase>
    <goals>
        <goal>copy-dependencies</goal>
    </goals>
    <configuration>
        <artifactItems>
            <artifactItem>
                <groupId>org.testng</groupId>
                <artifactId>testng</artifactId>
                <version>6.14.3</version>
            </artifactItem>
            <artifactItem>
                <groupId>org.apache.derby</groupId>
                <artifactId>derby</artifactId>
                <version>10.15.2.0</version>
            </artifactItem>
        </artifactItems>
        <outputDirectory>${project.basedir}/target/${application.server.libs}</outputDirectory>
    </configuration>
    </execution>
</executions>
</plugin>
```

### Configure TestNG

TestNG needs to be configured to know which packages contain tests to run.
This configuration is done via a configuration file. 

Example /src/test/resources/tck-suite.xml: 

```xml
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd" >

<suite name="jakarta-concurrency" verbose="2" configfailurepolicy="continue">
    <test name="jakarta-concurrency.tck">
        <packages>
            <package name="ee.jakarta.tck.concurrent.api.*"/>
            <package name="ee.jakarta.tck.concurrent.spec.*"/>
        </packages>
    </test>
</suite>
```

Example /pom.xml:

```xml
<plugin>
<groupId>org.apache.maven.plugins</groupId>
<artifactId>maven-surefire-plugin</artifactId>
<version>2.17</version>
    <configuration>
        <systemPropertyVariables>
            <!-- Good place for Arquillian variables -->
            <hostname>localhost</hostname>
        </systemPropertyVariables>
        <suiteXmlFiles>
            <suiteXmlFile>${project.basedir}/target/${path.to.suiteXmlFile}</suiteXmlFile>
        </suiteXmlFiles>
        <testSourceDirectory>${basedir}src/main/java</testSourceDirectory>
    </configuration>
</plugin>
```

### Configure Arquillian

Application Servers that implement the Arquillian SPI use a configuration file to define properties, such as hostname, port, username, password, etc. 
These properties will allow Arquillian to connect to the application server, install applications, and get test results.

Example src/test/resources/arquillian.xml:

```xml
<arquillian xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlns="http://jboss.org/schema/arquillian"
            xsi:schemaLocation="http://jboss.org/schema/arquillian http://jboss.org/schema/arquillian/arquillian_1_0.xsd">
    <engine>
        <property name="deploymentExportPath">${application_export_location}</property>
    </engine>
    <container qualifier="${app_server}" default="true">
        <configuration>
            <!-- Properties set in surefire plugin could be used here -->
            <property name="hostname">${hostname}</property>
        </configuration>
    </container>
</arquillian> 
```

### Configure Application Server

The TCK uses default objects (`java:comp/DefaultManagedExecutorService`) and annotation-based configurations (`@ManagedExecutorDefinition`) 
to ensure that application servers require minimal customization to run the TCK.

However, the TCK does require that Application Servers define a security context for security-based tests. 

- Username: javajoe
- Password: javajoe
- Group: Manager

The TCK uses external libraries that also need to be available on the Application Server's class path. 

- org.apache.derby:derby - For database testing
- org.testng:testng - For test assertions
- org.netbeans.tools:sigtest-maven-plugin - For signature testing


### Signature Tests

The Concurrency TCK will run signature tests on the application server itself, and not as part of a separate plugin.
This means that the signature tests will be ran during the maven `test` phase.

You need to configure your application server with a JVM property `-Djimage.dir=<path-your-server-has-access-to>`.
When running the signature tests on JDK 9+ we need to convert the JDK modules back into class files for signature testing.

The signature test plugin we use will also attempt to perform reflective access of classes, methods, and fields.
Due to the new module system in JDK 9+ special permissions need to be added in order for these tests to run: 

If you are using a Security Manager add the following permissions to the `sigtest-maven-plugin`:

```txt
permission java.lang.RuntimePermission "accessClassInPackage.jdk.internal";
permission java.lang.RuntimePermission "accessClassInPackage.jdk.internal.reflect";
permission java.lang.RuntimePermission "accessClassInPackage.jdk.internal.vm.annotation";
```

By default the java.base module only exposes certain classes for reflective access. 
It has been observed that the Concurrency TCK test will need access to the jdk.internal.vm.annotation class.
To give the `sigtest-maven-plugin` access to this class set the following JVM properties: 

```properties
--add-exports java.base/jdk.internal.vm.annotation=ALL-UNNAMED
--add-opens java.base/jdk.internal.vm.annotation=ALL-UNNAMED
```

Some JDKs will mistake the space in the prior JVM properties as delimiters between properties
In this case use:

```properties
--add-exports=java.base/jdk.internal.vm.annotation=ALL-UNNAMED
--add-opens=java.base/jdk.internal.vm.annotation=ALL-UNNAMED
```


For more information about generating the signature test file, and how the test run read: [ee.jakarta.tck.concurrent.framework.signaturetest/README.md](https://github.com/jakartaee/concurrency/blob/master/tck/src/main/java/ee/jakarta/tck/concurrent/framework/signaturetest/README.md)


### Advanced Configuration

Some application servers may have custom deployment descriptors that they would like to include 
as part of the applications that are being deployed to their server. 
The custom deployment descriptors can be included in a programmatic way using ShrinkWrap and the Arquillian SPI.

Example ApplicationArchiveProcessor:
```java
public class MyApplicationArchiveProcessor implements ApplicationArchiveProcessor {
    List<String> appNames;

    @Override
    public void process(Archive<?> archive, TestClass testClass) {
        if(appNames.contains(archive.getName())){
            ((WebArchive) archive).addAsWebInfResource("my-custom-sun-web.xml", "sun-web.xml");
        }
    }
```

Example LoadableExtension:
```java
public class MyLoadableExtension implements LoadableExtension {
    @Override
    public void register(ExtensionBuilder extensionBuilder) {
        extensionBuilder.service(ApplicationArchiveProcessor.class, MyApplicationArchiveProcessor.class);
    }
}
```

Example META-INF/services/org.jboss.arquillian.core.spi.LoadableExtension:
```txt
my.custom.test.package.MyLoadableExtension
```

## Running TCK

Once everything is configured, the TCK is run using the Maven test goal.

```sh
cd concurrency-api
mvn clean test
```

## Where to File Challenges

To file a challenge against the TCK, open a [new issue](https://github.com/jakartaee/concurrency/issues/new)
against the concurrency-api project. Add the `challenge` label,
and follow all of the process that is defined under the Challenges section of the
[Jakarta EE TCK Process](https://jakarta.ee/committees/specification/tckprocess/).

## Compatible Implementation

Open Liberty has a candidate compatible implementation with a [working TCK project](https://github.com/OpenLiberty/open-liberty/tree/release/dev/io.openliberty.jakarta.concurrency.3.0_fat_tck) that can be referenced as an example of how to configure a TCK project.

## Rules for Jakarta Concurrency Products

The following rules apply for each version of an operating system, software component,
and hardware platform Documented as supporting the Product:

- **Concurrency1** The Product must be able to satisfy all applicable compatibility requirements,
  including passing all Conformance Tests, in every Product Configuration and in every combination
  of Product Configurations, except only as specifically exempted by these Rules.
  For example, if a Product provides distinct Operating Modes to optimize performance,
  then that Product must satisfy all applicable compatibility requirements for a Product
  in each Product Configuration, and combination of Product Configurations, of those Operating Modes.

- **Concurrency1.1** If an Operating Mode controls a Resource necessary for the basic execution of the Test Suite,
  testing may always use a Product Configuration of that Operating Mode providing that Resource,
  even if other Product Configurations do not provide that Resource. Notwithstanding such exceptions,
  each Product must have at least one set of Product Configurations of such Operating Modes
  that is able to pass all the Conformance Tests.
  For example, a Product with an Operating Mode that controls a security policy (i.e., Security Resource)
  which has one or more Product Configurations that cause Conformance Tests to fail
  may be tested using a Product Configuration that allows all Conformance Tests to pass.

- **Concurrency1.2** A Product Configuration of an Operating Mode that causes the Product to report only
  version, usage, or diagnostic information is exempted from these compatibility rules.

- **Concurrency1.3** An API Definition Product is exempt from all functional testing requirements defined here,
  except the signature tests.

- **Concurrency2** Some Conformance Tests may have properties that may be changed.
  Properties that can be changed are identified in the configuration interview.
  Properties that can be changed are identified in the JavaTest Environment (.jte) files in the Test Suite installation.
  Apart from changing such properties and other allowed modifications described in this User's Guide (if any),
  no source or binary code for a Conformance Test may be altered in any way without prior written permission.
  Any such allowed alterations to the Conformance Tests will be provided via the Jakarta EE Specification Project website
  and apply to all vendor compatible implementations.

- **Concurrency3** The testing tools supplied as part of the Test Suite or as updated by the
  Maintenance Lead must be used to certify compliance.

- **Concurrency4** The Exclude List associated with the Test Suite cannot be modified.

- **Concurrency5** The Maintenance Lead can define exceptions to these Rules.
  Such exceptions would be made available as above, and will apply to all vendor implementations.

- **Concurrency6** All hardware and software component additions, deletions, and modifications to a
  Documented supporting hardware/software platform, that are not part of the Product but required
  for the Product to satisfy the compatibility requirements, must be Documented and available to users of the Product.
  For example, if a patch to a particular version of a supporting operating system is required for the
  Product to pass the Conformance Tests, that patch must be Documented and available to users of the Product.

- **Concurrency7** The Product must contain the full set of public and protected classes and interfaces
  for all the Libraries. Those classes and interfaces must contain exactly the set of public and
  protected methods, constructors, and fields defined by the Specifications for those Libraries.
  No subsetting, supersetting, or modifications of the public and protected API of the Libraries
  are allowed except only as specifically exempted by these Rules.

- **Concurrency7.1** If a Product includes Technologies in addition to the Technology Under Test,
  then it must contain the full set of combined public and protected classes and interfaces.
  The API of the Product must contain the union of the included Technologies.
  No further modifications to the APIs of the included Technologies are allowed.

- **Concurrency8** Except for tests specifically required by this TCK to be rebuilt (if any),
  the binary Conformance Tests supplied as part of the Test Suite or as updated by the
  Maintenance Lead must be used to certify compliance.

- **Concurrency9** The functional programmatic behavior of any binary class or interface must be
  that defined by the Specifications.
