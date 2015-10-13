#!/bin/bash

if [ "$1" == "-h" -o "$1" == "--help" ]; then
  echo "Usage: `basename $0` API_PORT DATABASE_HOST DATABASE_PASSWORD DATABASE_PORT COORD_NODE_NAME COORD_NODE_PORT"
  echo "All arguments are optional."
  exit 0
fi

echo "------- Set env variables and path -------"
echo "** variable.sh must be in the current folder **"
source variables.sh

api_port=${1:-$API_PORT}   
database_host=${2:-$DATABASE_HOST} 
database_password=${3:-$DATABASE_PASSWORD}
database_port=${4:-$DATABASE_PORT}
coord_node_name=${5:-COORD_NODE_NAME} 
coord_node_port=${6:-COORD_NODE_PORT}


# run postgrest db api
$AGI_HOME/experimental-framework/bin/db/db_api.sh $api_port $database_host $database_password $database_port &

# run coordinator
$JAVA_HOME/bin/java -Dfile.encoding=UTF-8  \
-cp \
$AGI_HOME/experimental-framework/lib/com.sun.jersey/jersey.core/1.18/jersey-core-1.18.jar:\
$AGI_HOME/experimental-framework/code/core-modules/target/io-agi-agief-core-modules-1.1.0-jar-with-dependencies.jar \
io.agi.ef.http.node.NodeMain $database_host $api_port $coord_node_name $coord_node_port $AGI_HOME/experimental-framework/run/server.properties coordinator