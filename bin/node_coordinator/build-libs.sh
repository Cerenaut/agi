#!/bin/bash


### NOTE: temporarily deprecated until we split the project again


default="$(dirname $0)/../variables.sh"
variables_file=${VARIABLES_FILE:-$default}
echo "Using variables file = $variables_file" 
source $variables_file


echo "------- build all the library dependencies of the experimental-framework -------"
cd ../../../algorithms/code/core
$MAVEN_BIN clean install
