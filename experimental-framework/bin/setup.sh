#!/bin/bash

echo "------- Set env variables and path -------"
source variables.sh

echo "------- Build and install libs -------"
$AGI_HOME/experimental-framework/bin/build-libs.sh

echo "------- build the experimental framework -------"
cd $AGI_HOME/experimental-framework/code/core-modules
mvn clean package

echo "------- setup database -------"
$AGI_HOME/experimental-framework/bin/db/db_setup.sh