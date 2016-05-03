#!/bin/bash

variables_file=${VARIABLES_FILE:-"variables.sh"}
echo "Using variables file = \"$variables_file\""
source $(dirname $0)/../$variables_file


if [ "$1" == "-h" -o "$1" == "--help" ]; then
  echo "Usage: `basename $0` NODE_PROPERTIES ENTITIES_FILE DATA_FILE"
  echo "All arguments are optional."
  exit 0
fi

node_properties=${1:-$AGI_RUN_HOME/node.properties}
entity_file=${2:-"$AGI_RUN_HOME/entities.json"}
data_file=${3:-"$AGI_RUN_HOME/data.json"}

log_config="$AGI_RUN_HOME/log4j2.xml"

main_class="io.agi.framework.demo.mnist.MNISTDemo"


# run coordinator
cmd="$JAVA_HOME/bin/java -Dfile.encoding=UTF-8 -Dlog4j.configurationFile=file:$log_config \
-cp \
$AGI_HOME/code/core/target/io-agi-core-1.0.0-jar-with-dependencies.jar $main_class \
$node_properties $entity_file $data_file"

echo $cmd;
eval $cmd;
