#!/bin/bash

default="$(dirname $0)/../variables.sh"
variables_file=${VARIABLES_FILE:-$default}
echo "Using variables file = $variables_file"
source $variables_file

### NOTE: temporarily deprecated until we split the project again
# echo "------- Build and install libs -------"
# ./build-libs.sh

cd $AGI_HOME/code/core

# Update project version
$AGI_HOME/bin/node_coordinator/update-version.sh

echo "------- Build the core and experimental framework -------"
$MAVEN_BIN clean package
