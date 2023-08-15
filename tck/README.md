# Jakarta Concurrency TCK Developer's Guide

> NOTE: This guide is for developers of the Concurrency TCK.
> If you are a user please refer to the user guide under `tck-dist`.
> Some of the information between the two guides is repeated, but the user guide will be more verbose.

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
cd api
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

## Configure and Run the TCK

Reference the user guide which can be generated in the `tck-dist` sub-project.

```
cd tck-dist/
mvn generate-resources
cd target/generated-docs/
```