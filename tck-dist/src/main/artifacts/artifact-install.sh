#!/usr/bin/env bash

## A sample script to install the artifact directory contents into a local maven repository

POM=$(ls *.pom)
TCK=$(ls *.jar | grep -v sources)

NO_EXT=${POM%.*}      # jakarta.concurrent-parent-3.1.0-SNAPSHOT.pom > jakarta.concurrent-parent-3.1.0-SNAPSHOT
NO_REPO=${NO_EXT#*-}  # jakarta.concurrent-parent-3.1.0-SNAPSHOT > parent-3.1.0-SNAPSHOT
VERSION=${NO_REPO#*-} # parent-3.1.0-SNAPSHOT > 3.1.0-SNAPSHOT

echo "Installing $POM with version $VERSION"
mvn org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file \
-Dfile=$POM \
-DgroupId=jakarta.concurrent \
-DartifactId=jakarta.concurrent.parent \
-Dversion=$VERSION \
-Dpackaging=pom

echo "Installing $TCK"
mvn org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file \
-Dfile=$TCK
