#!/usr/bin/env bash

## A sample script to install the artifact directory contents into a local maven repository
VERSION=3.0.1-SNAPSHOT

# Parent pom
mvn org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file \
-Dfile=jakarta.enterprise.concurrent.parent-"$VERSION".pom -DgroupId=jakarta.enterprise.concurrent \
-DartifactId=jakarta.enterprise.concurrent.parent -Dversion="$VERSION" -Dpackaging=pom

# CDI TCK Installed Library - test bean archive
mvn org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file \
-Dfile=jakarta.enterprise.concurrent-tck-"$VERSION".jar
