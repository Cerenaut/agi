# Documentation
This directory contains additional technical information, including documentation for the API, Docker, Jenkins as well as tips and gotchas.

## Table of Contents
- [Docker](./docker.md)
- [Jenkins](./jenkins.md)
- [Postgres](./postgres.md)
- [Notes & Gotchas](./notes.md)
- [API Documentation](./API/http.swagger.yaml)

## System Overview
![image](./diagrams/AGIEF%20v3.0%20-%20System%20Architecture%20-%20conceptual.png)

The diagram provides a high-level overview of the AGIEF system architecture, and highlights the interactions between the main system components. Please refer to the [Wiki](https://github.com/ProjectAGI/agi/wiki) for details on the architecture and [our website](https://agi.io/) for information about on the ongoing research and experiments.

## Code Structure
This repository consists of:

- Java core algorithmic components in `/code/core/src/main/java/io/agi/core`
- Java experimental framework components in `/code/core/src/main/java/io/agi/framework`
- Web based graphical UI in `/code/www`
- Associated scripts `/bin`

Compute nodes have a RESTful API, so it is possible to implement components in
other languages, or write alternative visualisations. Refer to the full full [API documentation](./docs/API/http.swagger.yaml) for
more details.

## Development Environment
This section builds upon the basic instructions given in the README. The following instructions only applies for setting up
the local environment for development purposes.

### Requirements
These are additional requirements necessary for setting up a development environment.

- [Maven](https://maven.apache.org/) build dependency system for Java
- [Java](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) Development Kit (JDK) version 1.8 or later

Installation of the following is optional (keep reading to see when appropriate):

- [PostgreSQL](http://www.postgresql.org/download) database
   - In-memory persistence is currently preferred, but PostgreSQL can be used instead
- [PGAdmin](http://www.pgadmin.org/download) database administration tool
   - If using PostgreSQL, to administer the database manually (not essential, but useful for examining the state of the system), we recommend the PGAdmin utility.
- [IntelliJ IDEA](https://www.jetbrains.com/idea) Java development environment
   - We provide project files to help you build and browse code using IntelliJ IDEA. If you wish to take advantage of this convenience, you should also install IntelliJ.

### Building
The project can be easily compiled and built by executing the `/bin/node_coordinator/build.sh`. This script performs a version update as well as a clean build using Maven.

### Testing
The unit tests are written using the [JUnit](http://junit.org/) testing framework and executed using the [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/).

- **Testing during Build:** Tests are disabled by default during builds using Maven. To re-enable them during builds, change the `skipTests` flag in the properties of the `pom.xml` file

- **Executing All Tests:** The tests can be executed using `mvn surefire:test -dskipTests=false`, or `mvn test -DskipTests=false` to also execute a build beforehand

- **Execute a Single Test:** A single test can be executed using `mvn surefire:test -DskipTests=false -Dtest=CLASS_NAME` where `CLASS_NAME` is the name of the unit test class, for e.g. `LogisticRegressionTest`
