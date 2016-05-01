#!/bin/bash

######################################################################
# Use this 'variables.sh' if you are running the system locally, 
# or if you are are deploying Docker containters from this local environment.
######################################################################


# ----------------------------------------
# AGI Home 
# ---------------------------------------
export AGI_HOME=~/Development/ProjectAGI/AGIEF/agi

# RUN Home
export AGI_RUN_HOME=~/Development/ProjectAGI/AGIEF/runFolders/runImage

# Database port
export DB_PORT=5432

# ----------------------------------------
# Dependencies
# ---------------------------------------

# POSTGRESQL
export POSTGRESQL_BIN=/Applications/Server.app/Contents/ServerRoot/usr/bin/psql

# MAVEN
export MAVEN_BIN=/usr/local/bin/mvn

# Java
export JAVA_HOME=`/usr/libexec/java_home -v 1.8`


# ----------------------------------------
# Set Path
# ----------------------------------------
export PATH=${MAVEN_HOME}/bin:${PATH}
export PATH=${JAVA_HOME}/bin:${PATH}
