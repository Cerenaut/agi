#!/bin/bash

variables_file=${VARIABLES_FILE:-"variables.sh"}
echo "Using variables file = \"$variables_file\""
source $(dirname $0)/../$variables_file


echo "------- Copy AGIEF customised Mustache templates -------"
./copyMustache.sh

echo "------- Re-build Swagger -------"
cd $SWAGGER_HOME
$MAVEN_BIN clean package
