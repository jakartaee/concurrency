Jakarta Concurrency Specification
=================================

This project generates the Jakarta Concurrency Specification.

Building
--------

Prerequisites:

* JDK 21+
* Maven 3.8.5+

Run the full build:

`mvn install`

Run the build to generate specifciation documentation for publication:

`mvn package --file specification/pom.xml -Dstatus=FINAL -Dspec.version=X.Y`

Where `FINAL` indicates the status of this documentation.
Where `X.Y` indicates the Major and Minor version. Example: `3.1`

Locate the html files:
- target/generated-docs/Concurrency.html

Locate the PDF files:
- target/generated-docs/Concurrency.pdf
