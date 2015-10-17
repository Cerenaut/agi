#!/bin/bash


######################################################################
# Use this 'variables.sh' if you are running the system in a 
# Docker container.  
# BEWARE: Some of the parameters NEED to be the same inside and outside the container 
# (such as API_PORT, depending on the port mappings).
######################################################################


# ----------------------------------------
# AGI Home 
# ---------------------------------------
export AGI_HOME=~/dev/agi


# ----------------------------------------
# Dependencies
# ---------------------------------------

# Swagger
export SWAGGER_HOME=~/dev/swagger-codegen

# POSTGRESQL (if used apt-get and using the system version, there is no need to set this explicitly)
# export POSTGRESQL_HOME=

# POSTGREST
export POSTGREST_BIN=$AGI_HOME/experimental-framework/bin/node_db/postgrest/linux/postgrest-0.2.11.1

# MAVEN (if used apt-get and using the system version, there is no need to set this explicitly)
# export MAVEN_HOME=/usr/bin

# Java
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64


# ----------------------------------------
# Set Path
# ----------------------------------------
export PATH=${MAVEN_HOME}/bin:${PATH}
export PATH=${JAVA_HOME}/bin:${PATH}


# ----------------------------------------
# Set parameters for other scripts
# ----------------------------------------
export API_HOST=localhost						# the host of the postgrest api
export API_PORT=8080							# the port of the postgrest api
export DATABASE_HOST=$DB_PORT_5432_TCP_ADDR 	# the host of the postgresql db
export DATABASE_PASSWORD=password				# password of the agiu user of the postresql db
export DATABASE_PORT=$DB_PORT_5432_TCP_PORT		# the port of the postgresql db
export COORD_NODE_NAME=CoordNode 				# the label given to the coordinator node
export COORD_NODE_PORT=8081						# the port of the coordinator node
