#!/bin/bash

variables_file=${VARIABLES_FILE:-"variables.sh"}
echo "Using variables file = \"$variables_file\""
source $(dirname $0)/../$variables_file


if [ "$1" == "-h" -o "$1" == "--help" ]; then
  echo "Usage: `basename $0` DATABASE_API_HOST DATABASE_API_PORT COORD_NODE_NAME COORD_NODE_PORT"
  echo "All arguments are optional."
  exit 0
fi


database_api_host=${1:-$DATABASE_API_HOST}   
database_api_port=${2:-$DATABASE_API_PORT}   
coord_node_name=${3:-COORD_NODE_NAME} 
coord_node_port=${4:-COORD_NODE_PORT}

main_class="io.agi.ef.http.node.NodeMain"
properties_file="$AGI_HOME/experimental-framework/run/server.properties"
node_type="coordinator"		# if 'coordinator', then the coordinator will be set to 'master' mode

# run coordinator
$JAVA_HOME/bin/java -Dfile.encoding=UTF-8  \
-cp \
$AGI_HOME/experimental-framework/lib/com.sun.jersey/jersey.core/1.18/jersey-core-1.18.jar:\
$AGI_HOME/experimental-framework/code/core-modules/target/io-agi-agief-core-modules-1.1.0-jar-with-dependencies.jar \
$main_class $api_host $api_port $coord_node_name $((coord_node_port)) $properties_file $node_type