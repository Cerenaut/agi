#!/bin/bash

#######################################################
## RUN AGIEF EXPERIMENT with various options
#######################################################

variables_file=${VARIABLES_FILE:-"variables.sh"}
echo "Using variables file = \"$variables_file\""
source $(dirname $0)/../$variables_file

usage="Usage: `basename $0` NODE_PROPERTIES ENTITIES_FILE(optional) DATA_FILE(optional)"
 
while getopts ":p:e:d:l:m:" opt; do
  case $opt in
    p)	# name of properties file (optional, there is a default)
      opt_p=$OPTARG
      ;;
	l)	# name of log4j2 xml file (optional, there is a default)
      opt_l=$OPTARG
      ;;
	m)	# name of main class (optional, there is a default)
      opt_m=$OPTARG
      ;;
	d)	# name of input file for data (optional, can be unspecified)
      opt_d=$OPTARG
      ;;
	e)	# name of input file for entity (optional, can be unspecified)
      opt_e=$OPTARG
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      exit 1
      ;;
    :)
      echo "Option -$OPTARG requires an argument." >&2
      exit 1
      ;;
  esac
done

node_properties=${opt_p:-node.properties}
entity_file=$opt_e			# if it was not defined, that is ok, it is not passed into the runtime
data_file=$opt_d			# if it was not defined, that is ok, it is not passed into the runtime
log_config=${opt_l:-log4j2.xml}
main_class=${opt_m:-io.agi.framework.Main}

echo "Run home = $AGI_RUN_HOME"
cd $AGI_RUN_HOME
pwd


# run coordinator
cmd="$JAVA_HOME/bin/java -Xmx6000m -Dfile.encoding=UTF-8 -Dlog4j.configurationFile=file:$log_config \
-cp \
$AGI_HOME/code/core/target/agief-jar-with-dependencies.jar $main_class \
$node_properties $entity_file $data_file"

echo $cmd;
eval $cmd;
