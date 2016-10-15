#!/bin/bash


######################################################################
# Use this 'variables.sh' if you are running the system in a 
# Docker container.  
# BEWARE: Some of the parameters NEED to be the same inside and outside the container 
# (such as API_PORT, depending on the port mappings).
######################################################################


# ---------------------------------------
# AGI Home Folders
# ---------------------------------------

# Code Home
export AGI_HOME=~/dev/agi

# RUN Home
export AGI_RUN_HOME=~/dev/run

# DATA Home
export AGI_DATA_HOME=~/dev/data

# Database 
export DB_PORT=5432
export DB_HOST=172.31.22.107		# for Task on ECS, we need the hardcoded value, because we can't go in and set an env variable before running the java process - this will need to be improved.

# ---------------------------------------
# Dependencies
# ---------------------------------------

# POSTGRESQL
export POSTGRESQL_BIN=/usr/bin/psql

# MAVEN
export MAVEN_BIN=/usr/share/maven/bin/mvn

# Java
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64


# ---------------------------------------
# Set Path
# ---------------------------------------
export PATH=${MAVEN_HOME}/bin:${PATH}
export PATH=${JAVA_HOME}/bin:${PATH}

