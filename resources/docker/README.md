#AGIEF Development Environment

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

### Building the Docker image
```sh
$ cd <path_to>/agi
$ docker build -t agi/dev resources/docker/
```

Note: this creates a container with the name **agi/dev** with the docker file at **resources/docker**

### Start the container
The container has all pre requisites set up to run AGIEF. 
The codebase is in the mapped folder and it should be built and run with **Maven** according to the instructions. 

```sh
$ docker run -i -d -p 3030:3030 -v $AGI_PROJECT_DIR:/root/dev/agi -v $SWAGGER_CODEGEN_DIR:/root/dev/swagger-codegen source ./bin/.bashrc
```

OR if you prefer to start docker in the foreground logged into the container.
```sh
$ docker run -i -d -p 3030:3030 -v $AGI_PROJECT_DIR:/root/dev/agi -v $SWAGGER_CODEGEN_DIR:/root/dev/swagger-codegen source ./bin/.bashrc
```

Note: This runs the container we just created, called **agi/dev**, maps the docker container port 3030 to localhost:3030, maps the volumes specified after ‘-v’ so that those folders are available inside the container.

### Stop the container
Stopping a running container is possible via the docker api. If only one instance of this container is running this command will stop it:
```sh
$ docker stop `sudo docker ps |grep agi/dev |cut -d\  -f1`
```

## Hacking
The project uses git flow see https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow.
Modify the codebase within any environment, you need not work within the Docker container.

Contact Gideon Kowadlo (gideon@agi.io) to perform a release.

## Contributors
* Gideon Kowadlo (gideon@agi.io) 

## License
Copyright Project AGI 2015
