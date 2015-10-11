#!/bin/bash

if [ "$1" == "-h" -o "$1" == "--help" ]; then
  echo "Usage: `basename $0` API_PORT DATABASE_HOST DATABASE_PASSWORD DATABASE_PORT COORD_NODE_NAME COORD_NODE_PORT"
  echo "All arguments are optional."
  exit 0
fi

# run postgrest db api
$AGI_HOME/experimental-framework/bin/db/db_api.sh ${1:-8080} ${2:-localhost} ${3:-password} ${4:-5432} &

# run coordinator
$JAVA_HOME/bin/java -Dfile.encoding=UTF-8  \
-cp \
$AGI_HOME/experimental-framework/lib/com.sun.jersey/jersey.core/1.18/jersey-core-1.18.jar:\
$AGI_HOME/experimental-framework/code/core-modules/target/io-agi-agief-core-modules-1.1.0-jar-with-dependencies.jar \
io.agi.ef.http.node.NodeMain ${2:-localhost} ${1:-8080} ${5:-coodNode} ${6:-8081} server.properties COORDINATOR