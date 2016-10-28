#!/bin/bash

# ----------------------------------------
# AGI Home Folders
# ---------------------------------------

# Code Home (set to the home of the code where it is developed and compiled, it will then be rsynced to where it is executed)
export AGI_HOME=
# GK: AGI_HOME=/Users/gideon/Development/ProjectAGI/AGIEF/agi
# DR: AGI_HOME=/home/dave/workspace/agi.io/agi

# RUN Home (set to the home of the experiment definitions - i.e. the environment where you are launching experiments from, which is not necessarily where they are being executed)
export AGI_RUN_HOME=
# GK: AGI_RUN_HOME=/Users/gideon/Development/ProjectAGI/AGIEF/experiment-definitions/mnist-gng-v1	# mnist-autoencoder-v1
# DR  AGI_RUN_HOME=/home/dave/workspace/agi.io/agi/resources/run-empty

# DATA Home (set to the home of the data for the actual experiment - i.e. in the environment where they'll be running)
export AGI_DATA_HOME=
# GK: AGI_DATA_HOME=/Users/gideon/Development/ProjectAGI/AGIEF/datasets/MNIST      # when running locally
# DR: AGI_DATA_HOME=/root/dev/data


# Database
export DB_PORT=5432   # IMPORTANT!!!  DO NOT DEFINE THIS VARIABLE.   It will be defined by scripts at runtime, and we don't want it to get defined by sourcing this file

# ----------------------------------------
# Dependencies
# ---------------------------------------

# MAVEN
export MAVEN_BIN=/usr/local/bin/mvn
# GK: MAVEN_BIN=/usr/local/bin/mvn
# DR: MAVEN_BIN=/home/dave/workspace/maven/apache-maven-3.3.3/bin/mvn

# Java
export JAVA_HOME=`/usr/libexec/java_home -v 1.8`
# GK: JAVA_HOME=`/usr/libexec/java_home -v 1.8`
# DR: JAVA_HOME=/home/dave/workspace/agi.io/java/jdk1.8.0_60


# ----------------------------------------
# Set Path
# ----------------------------------------
export PATH=${JAVA_HOME}/bin:${PATH}