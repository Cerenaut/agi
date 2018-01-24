# Contributing Guide
The aim of this guide is to help potential contributers setup a local
development environment, understand the development process and how to build and test the code.

## Documentation
- [Technical Documentation](./docs): API documentation, other technical information & gotchas
- [AGIEF Wiki](https://github.com/ProjectAGI/agi/wiki)
- [Project AGI Website](https://agi.io)

## Code Structure
This repository consists of:

- Java core algorithmic components ```/code/core/src/io/agi/core```
- Java experimental framework components ```/code/core/src/io/agi/framework```
- Web based graphical UI ```/code/www```
- Associated scripts ```/bin```

Compute nodes have a RESTful API, so it is possible to implement components in
other languages, or write alternative visualisations. The full documentation for
the API can be found in [/docs/API/http.swagger.yaml](./docs/API/http.swagger.yaml).

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
