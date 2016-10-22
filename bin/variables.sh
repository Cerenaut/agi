#!/bin/bash

######################################################################
# Use this 'variables.sh' if you are running the system locally, 
# or if you are are deploying Docker containters from this local environment.
######################################################################


# ----------------------------------------
# AGI Home Folders
# ---------------------------------------

# Code Home (set to the home of the code where it is developed and compiled, it will then be rsynced to where it is executed)
export AGI_HOME=/Users/gideon/Development/ProjectAGI/AGIEF/agi
#export AGI_HOME=/home/dave/workspace/agi.io/agi

# RUN Home (set to the home of the experiment definitions - i.e. the environment where you are launching experiments from, which is not necessarily where they are being executed)
# export AGI_RUN_HOME=~/Development/ProjectAGI/AGIEF/runFolders/runImage 
export AGI_RUN_HOME=/Users/gideon/Development/ProjectAGI/AGIEF/experiment-definitions/mnist-gng-v1	# mnist-autoencoder-v1
#export AGI_RUN_HOME=/home/dave/workspace/agi.io/agi/resources/run-empty

# DATA Home (set to the home of the data for the actual experiment - i.e. in the environment where they'll be running)
export AGI_DATA_HOME=/Users/gideon/Development/ProjectAGI/AGIEF/datasets/MNIST      # when running locally
# export AGI_DATA_HOME=/root/dev/data


# Database
export DB_PORT=5432
# export DB_HOST=localhost		# IMPORTANT!!!  DO NOT DEFINE THIS VARIABLE.   It will be defined by scripts at runtime, and we don't want it to get defined by sourcing this file

# ----------------------------------------
# Dependencies
# ---------------------------------------

# MAVEN
export MAVEN_BIN=/usr/local/bin/mvn
# export MAVEN_BIN=/home/dave/workspace/maven/apache-maven-3.3.3/bin/mvn

# Java
export JAVA_HOME=`/usr/libexec/java_home -v 1.8`
#export JAVA_HOME=/home/dave/workspace/agi.io/java/jdk1.8.0_60


# ----------------------------------------
# Set Path
# ----------------------------------------
export PATH=${JAVA_HOME}/bin:${PATH}
