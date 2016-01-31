

## Background

### Variables
* A common file contains all the environment variables for the framework, they define parameter values and relevant paths
* By default, this file is */bin/variables.sh*. You can override this by specifying the path to an alternative file in an environment variable *VARIABLES_FILE*
* If using the defaulat, *variables.sh*, modify it to set the environment variables for your system
* Every script begins by sourcing this file

### Scripts
* Scripts assume the directory structure. They may refer to other scripts via relative paths

### Setting up and running
* Change directory to the node type that you wish to run (e.g. ./bin/node_coordinator)
* Run *setup.sh*
* Run *run.sh*

NOTE:
If you want to run the system in Docker containers, you must launch a Postgres container, and then an agi/ef container, linked to the Postgres container. The commands are contained at /bin/docker/README-docker-commands.md


The system has been set up so that
* It is possible to setup and run db on a separate machine