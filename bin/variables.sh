#!/bin/bash

######################################################################
# Use this 'variables.sh' if you are running the system locally, 
# or if you are are deploying Docker containters from this local environment.
######################################################################


# ----------------------------------------
# AGI Home 
# ---------------------------------------
export AGI_HOME=~/Development/ProjectAGI/AGIEF/agi
#export AGI_HOME=/home/dave/workspace/agi.io/agi

# RUN Home
# export AGI_RUN_HOME=~/Development/ProjectAGI/AGIEF/runFolders/runImage 
export AGI_RUN_HOME=~/Development/ProjectAGI/AGIEF/experiment-definitions/classifier
#export AGI_RUN_HOME=/home/dave/workspace/agi.io/agi/resources/run-empty

# Database
export DB_PORT=5432
export DB_HOST=localhost

# ----------------------------------------
# Dependencies
# ---------------------------------------

# POSTGRESQL
#export POSTGRESQL_BIN=/Applications/Server.app/Contents/ServerRoot/usr/bin/psql
export POSTGREST_BIN=$AGI_HOME/experimental-framework/bin/node_db/postgrest/linux/postgrest-0.2.11.1

# MAVEN
#export MAVEN_BIN=/usr/local/bin/mvn
export MAVEN_BIN=/home/dave/workspace/maven/apache-maven-3.3.3/bin/mvn

# Java
#export JAVA_HOME=`/usr/libexec/java_home -v 1.8`
export JAVA_HOME=/home/dave/workspace/agi.io/java/jdk1.8.0_60


# ----------------------------------------
# Set Path
# ----------------------------------------
export PATH=${MAVEN_HOME}/bin:${PATH}
export PATH=${JAVA_HOME}/bin:${PATH}
