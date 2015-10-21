#!/bin/bash


# run container for Postgres DB
# use kitematic for now

# run container for Coordinator Node
docker run --name AGI_Node --link postgres:db -i -d -p $API_PORT:$API_PORT -v $AGI_HOME:/root/dev/agi -v $SWAGGER_HOME:/root/dev/swagger-codegen agi/dev bash

# inside the container, 
# 1. run node_db/setup.sh 				- which will use the linked postgres container
# 2. run node_coordinator/setup.sh 		- setup the container for being a Coordinator Node
