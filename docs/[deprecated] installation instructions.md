April 2017 - This is superseded by the README. TO BE updated.




These instructions will help you to setup the Artficial General Intelligence Experimental Framework (AGIEF).

# Supported Operating Systems

- Linux 
- Mac OS X

We aim to support Microsoft Windows in future. However, it requires a custom build of the database HTTP API.

## Installation Instructions

You need to install the following items:

- Java Development Kit (JDK) version 1.8 or later
- Maven (package management for Java)
- PostgreSQL database
- PostgREST HTTP API

To administer the database manually (not essential, but useful for examining the state of the system), we recommend the PGAdmin utility.

We provide project files to help you build and browse code using IntelliJ IDEA.
If you wish to take advantage of this convenience, you should also install IntelliJ.

# Download Links

## Essential Dependencies

* Java (Development Kit) (JDK)   [[http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html]]

* Maven build dependency system for Java [[http://maven.apache.org/install.html]]

* PostgreSQL database [[http://www.postgresql.org/download]]

* PostgREST database RESTful HTTP API [[https://github.com/begriffs/postgrest/releases/latest]]

## Developer tools (nice to have)

* PGAdmin database admin tool [[http://www.pgadmin.org/download]]

* IntelliJ IDEA Java dev environment [[https://www.jetbrains.com/idea]]

# Installation instructions

We will describe installing and running the system on a single host. However, all processes can be distributed onto any host. We will describe how to configure process discovery later.

1. Download and install all the dependencies above.

2. Checkout the agi repository to a folder, which we will name AGI_DIR

3. Open a command prompt and navigate to AGI_DIR/sql

4. Execute the command:

`sudo -u postgres psql -f agidb.sql`

# Execution instructions

Once the install has been completed, the following must be run to start the framework. These services must be run after every boot.

1. We assume that during the install, the database has been configured to run on boot (e.g. as a daemon service).

2. Open a command prompt and navigate to the directory in which you installed PostgREST. 

3. Execute the command:

`./postgrest-0.2.10.0 --db-host localhost  --db-port 5432 --db-name agidb  --db-user agiu --db-pass password --db-pool 200  --anonymous agiu --port 3000 --v1schema public`

This will create the database HTTP API service. Note you can modify the database password as required. Also, PostgREST does not have to run on the same host as the database; it can run on any host.

4. Set your JAVA_HOME environment variable to the location of the unpacked JDK directory.

For example, in bash, do:

`export JAVA_HOME=/home/dave/java/jdk1.8.0_60`

5. Build the core library:

TODO fix this up.

`/home/dave/maven/apache-maven-3.3.3/bin/mvn install` 

4. Next, we must run a coordinator process. Open a command prompt and navigate to the directory AGI_DIR/bin

5. Execute the command:

`TODO`