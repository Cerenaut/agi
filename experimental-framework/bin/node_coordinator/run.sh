#!/bin/bash


if [ "$1" == "-h" -o "$1" == "--help" ]; then
  echo "Usage: `basename $0` API_HOST API_PORT COORD_NODE_NAME COORD_NODE_PORT"
  echo "All arguments are optional."
  exit 0
fi


api_host=${1:-$API_HOST}   
api_port=${2:-$API_PORT}   
coord_node_name=${3:-COORD_NODE_NAME} 
coord_node_port=${4:-COORD_NODE_PORT}


# run coordinator
$JAVA_HOME/bin/java -Dfile.encoding=UTF-8  \
-cp \
$AGI_HOME/experimental-framework/lib/com.sun.jersey/jersey.core/1.18/jersey-core-1.18.jar:\
$AGI_HOME/experimental-framework/code/core-modules/target/io-agi-agief-core-modules-1.1.0-jar-with-dependencies.jar \
io.agi.ef.http.node.NodeMain $api_host $api_port $coord_node_name $((coord_node_port)) $AGI_HOME/experimental-framework/run/server.properties coordinator