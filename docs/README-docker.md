# AGIEF Development Environment

**Table of Contents**
- [Description](#markdown-header-description)
- [Running](#markdown-header-running)

## Description

This project encapsulates an AGIEF Development Environment project within a docker container that can be deployed to developers and production machines. 

### Install Docker
Docker is available for OSX via Kitematic. Available from https://kitematic.com

### Clone the AGI repository.
```sh
$ git clone git@github.com:ProjectAGI/agi.git
```

## Running
Select the DOCKER CLI button within Kitematic to work within the Docker virtual environment.
All the docker commands must be executed within the Docker virtual environment terminal.

### Building the Docker image for Development
```sh
$ cd <path_to>/agi/experimental-framework/bin/docker
$ docker build -t agief-dev -f Dockerfile-dev ../../../
```

### Building the Docker image for Deployment
```sh
$ cd <path_to>/agi/experimental-framework/bin/docker
$ docker build -t agief -f Dockerfile ../../../
```


Note: this creates a container with the name **agief-dev** or **agief** with the docker files in **bin/docker**

### Start the container
The container has all pre-requisites set up to dev (for agief-dev) or run (for agief) AGIEF. 
The codebase is in the mapped folder and it should be built and run with the scripts in ```/run``` as in the README documents.

This container uses a postgres database running in another container which you must run before you run this one. 
--> MORE INFO on how to run it

You start the docker container, linking it to the running Postgres container, with the following commands.

### Case 1 Deployment: 
```sh
$docker run --name AGI_Node --link postgres:db -i -d -p $DATABASE_API_PORT:$DATABASE_API_PORT -v $AGI_HOME:/root/dev/agi agief
```

### Case 2a: Development: You want to use the framework as is (Most common):
```sh
$docker run --name AGI_Node --link postgres:db -i -d -p $DATABASE_API_PORT:$DATABASE_API_PORT -v $AGI_HOME:/root/dev/agi -v agief-dev
```

## Case 2b: Development: You want to modify the framework:
This may require modifying the http API's, which then requires modifying the Swagger spec and running code generation.
```sh
$docker run --name AGI_Node --link postgres:db -i -d -p $DATABASE_API_PORT:$DATABASE_API_PORT -v $AGI_HOME:/root/dev/agi -v $SWAGGER_HOME:/root/dev/swagger-codegen agief-dev
```

Note: This runs the container we just created, called **agi/ef[-dev]**, maps the docker container port DATABASE_API_PORT to localhost:DATABASE_API_PORT, maps the volumes specified after ‘-v’ so that those folders are available inside the container.

### Stop the container
Stopping a running container is possible via the docker api. If only one instance of this container is running this command will stop it:
```sh
$ docker stop `sudo docker ps |grep agief-dev |cut -d\  -f1`
```

## Hacking
The project uses git flow see https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow.
Modify the codebase within any environment, you need not work within the Docker container.

Contact Gideon Kowadlo (gideon@agi.io) to perform a release.

## Contributors
* Gideon Kowadlo (gideon@agi.io) 

## License
Copyright Project AGI 2015
