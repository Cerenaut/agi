#!/bin/bash

######################################################################
# Use this 'variables.sh' if you are running the system locally, 
# or if you are are deploying Docker containters from this local environment.
######################################################################


# ----------------------------------------
# AGI Home 
# ---------------------------------------
export AGI_HOME=~/Development/AI/ProAGI/agi-clean


# ----------------------------------------
# Dependencies
# ---------------------------------------

# Swagger
export SWAGGER_HOME=~/Development/Tools/swagger-codegen

# POSTGRESQL
export POSTGRESQL_BIN=/Applications/Server.app/Contents/ServerRoot/usr/bin/psql

# POSTGREST
export POSTGREST_BIN=$AGI_HOME/experimental-framework/bin/node_db/postgrest/osx/postgrest-0.2.11.1

# MAVEN
export MAVEN_BIN=/usr/local/bin/mvn

# Java
export JAVA_HOME=`/usr/libexec/java_home -v 1.8`


# ----------------------------------------
# Set Path
# ----------------------------------------
export PATH=${MAVEN_HOME}/bin:${PATH}
export PATH=${JAVA_HOME}/bin:${PATH}


# ----------------------------------------
# Set parameters for other scripts
# ----------------------------------------
export DATABASE_API_HOST=localhost		# the host of the postgrest api
export DATABASE_API_PORT=8080			# the port of the postgrest api
export DATABASE_HOST=localhost 			# the host of the postgresql db
export DATABASE_PASSWORD=password		# password of the agiu user of the postresql db
export DATABASE_PORT=5432				# the port of the postgresql db
export COMPUTE_NODE_NAME=CoordNode 		# the label given to the coordinator node
export COMPUTE_NODE_PORT=8081			# the port of the coordinator node
