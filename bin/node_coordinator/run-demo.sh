#!/bin/bash


#######################################################
## RUN EXPERIMENT defined in the demo main function
#######################################################

variables_file=${VARIABLES_FILE:-"variables.sh"}
echo "Using variables file = \"$variables_file\""
source $(dirname $0)/../$variables_file


if [ "$1" == "-h" -o "$1" == "--help" ]; then
  echo "Usage: `basename $0` NODE_PROPERTIES MAIN_CLASS"
  echo "All arguments are optional."
  exit 0
fi

node_properties=${1:-node.properties}
main_class=${2:-io.agi.framework.demo.classifier.ClassifierDemo}

log_config="log4j2.xml"

cd $AGI_RUN_HOME
pwd

# run coordinator
cmd="$JAVA_HOME/bin/java -Dfile.encoding=UTF-8 -Dlog4j.configurationFile=file:$log_config \
-cp \
$AGI_HOME/code/core/target/agief-jar-with-dependencies.jar $main_class \
$node_properties create"

echo $cmd;
eval $cmd;
