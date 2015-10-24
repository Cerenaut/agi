#!/bin/bash

variables_file=${VARIABLES_FILE:-"variables.sh"}
echo "Using variables file = \"$variables_file\""
source $(dirname $0)/../$variables_file


cmd="$AGI_HOME/experimental-framework/bin/codegen/genServer.sh"
echo $cmd;
eval $cmd;

cmd="$AGI_HOME/experimental-framework/bin/codegen/genClient.sh"
echo $cmd;
eval $cmd;

cmd="$AGI_HOME/experimental-framework/bin/codegen/genPersistence.sh"
echo $cmd;
eval $cmd;