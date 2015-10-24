#!/bin/bash

variables_file=${VARIABLES_FILE:-"variables.sh"}
echo "Using variables file = \"$variables_file\""
source $(dirname $0)/../$variables_file


MUSTACHE_DIR="$AGI_HOME/experimental-framework/resources/swagger-mustache"

cmd="cp -r $MUSTACHE_DIR/* $SWAGGER_HOME/modules/swagger-codegen/src/main/resources/"

echo $cmd;
eval $cmd;