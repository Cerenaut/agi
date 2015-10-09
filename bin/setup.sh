#!/bin/sh

echo "------- Set env variables and path -------"
source variables

echo "------- Build and install libs -------"
$AGI_HOME/bin/build-libs.sh

echo "------- build the experimental framework -------"
cd $AGI_HOME/src/experimental-framework/core-modules
mvn clean package

echo "------- setup database -------"
$AGI_HOME/bin/db/db_setup.sh