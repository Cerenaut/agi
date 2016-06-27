#!/bin/bash

#######################################################
## RUN EXPERIMENT defined by input json files
#######################################################


variables_file=${VARIABLES_FILE:-"variables.sh"}
echo "Using variables file = \"$variables_file\""
source $(dirname $0)/../$variables_file


if [ "$1" == "-h" -o "$1" == "--help" ]; then
  echo "Usage: `basename $0` NODE_PROPERTIES ENTITIES_FILE(optional) DATA_FILE(optional)"
  echo "If entities file is specified, load it, if entities and data file are specified, load them both."
  echo "All arguments are optional."
  exit 0
fi

node_properties=${1:-node.properties}
entity_file=$2
data_file=$3

log_config="log4j2.xml"

main_class="io.agi.framework.Main"

cd $AGI_RUN_HOME
pwd


# run coordinator
cmd="$JAVA_HOME/bin/java -Dfile.encoding=UTF-8 -Dlog4j.configurationFile=file:$log_config \
-cp \
$AGI_HOME/code/core/target/agief-1.0.0-jar-with-dependencies.jar $main_class \
$node_properties $entity_file $data_file"

echo $cmd;
eval $cmd;
