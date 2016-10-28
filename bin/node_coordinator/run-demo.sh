#!/bin/bash


#######################################################
## RUN EXPERIMENT defined in the demo main function
#######################################################

default="$(dirname $0)/../variables.sh"
variables_file=${VARIABLES_FILE:-$default}
echo "Using variables file = $variables_file" 
source $variables_file


if [ "$1" == "-h" -o "$1" == "--help" ]; then
  echo "Usage: `basename $0` NODE_PROPERTIES MAIN_CLASS PREFIX"
  echo "All arguments are optional."
  exit 0
fi

node_properties=${1:-node.properties}
main_class=${2:-io.agi.framework.demo.classifier.ClassifierDemo}
the_prefix=${3:-"THE_PREFIX"}

log_config="log4j2.xml"

echo "Create classpath file cp.txt"
cd $AGI_HOME/code/core
pwd
cmd="mvn dependency:build-classpath -Dmdep.outputFile=$AGI_RUN_HOME/cp.txt"
eval $cmd;

cd $AGI_RUN_HOME
pwd

# run coordinator
cmd="$JAVA_HOME/bin/java -Dfile.encoding=UTF-8 -Dlog4j.configurationFile=file:$log_config \
-cp `cat cp.txt`:$AGI_HOME/code/core/target/agief.jar $main_class \
$node_properties create prefix $the_prefix"

echo $cmd;
eval $cmd;
