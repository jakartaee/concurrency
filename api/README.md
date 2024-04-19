Jakarta Concurrency API
=================================

This project builds and tests the Jakarta Concurrency API

Building
--------

Prerequisites:

* JDK 17+
* Maven 3.0.3+

Build and test the API:

`mvn package`

Build the API for publication:

`mvn package -file api/pom.xml -Dspec.version=X.Y`

Where `X.Y` indicates the Major and Minor version. Example: `3.1`

Look for any error in the checkstye:
- target/checkstyle/checkstyle.html