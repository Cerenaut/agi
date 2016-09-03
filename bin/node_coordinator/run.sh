#!/bin/bash

#######################################################
## RUN AGIEF EXPERIMENT with various options
#
# *** WARNING ***
#     For postgres, assumes DB_HOST is set, and it is 
#     NOT defined in variables.sh, make sure you define it explicitly, 
#     or overwrite the database-url key/val pair as a parameter
#
#######################################################

variables_file=${VARIABLES_FILE:-"variables.sh"}
echo "Using variables file = \"$variables_file\""
source $(dirname $0)/../$variables_file

usage="Usage: `basename $0` NODE_PROPERTIES ENTITIES_FILE(optional) DATA_FILE(optional)"
 
while getopts ":p:e:d:l:m:k:v:c:" opt; do
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
    d)	# name of input file for data (optional, default is null)
      opt_d=$OPTARG
        ;;
    e)	# name of input file for entity (optional, default is null)
      opt_e=$OPTARG
      ;;
    c)	# name of input file for config (optional, default is null)
      opt_c=$OPTARG
      ;;
    k)	# property key (optional, there is default)
      opt_p_key=$OPTARG
      ;;
    v)	# property value (optional, there is default)
      opt_p_val=$OPTARG
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
entity_file=${opt_e:-null}			# if it was not defined, that is ok, it is passed as null and ignored
data_file=${opt_d:-null}	   		# if it was not defined, that is ok, it is passed as null and ignored
config_file=${opt_c:-null}			# if it was not defined, that is ok, it is passed as null and ignored
log_config=${opt_l:-log4j2.xml}
main_class=${opt_m:-io.agi.framework.Main}

dburl_default="jdbc:postgresql://$DB_HOST:$DB_PORT/agidb"

# e.g. key: database-url value: jdbc:postgresql://localhost:5432/agidb
p_key=${opt_p_key:-database-url}          # if it was not defined, that is ok, it is passed as null and ignored
p_val=${opt_p_val:-$dburl_default} 		    # if it was not defined, that is ok, it is passed as null and ignored

echo "Run home = $AGI_RUN_HOME"
cd $AGI_RUN_HOME
pwd

# run coordinator
cmd="$JAVA_HOME/bin/java -Xmx6000m -Dfile.encoding=UTF-8 -Dlog4j.configurationFile=file:$log_config \
-cp \
$AGI_HOME/code/core/target/agief-jar-with-dependencies.jar $main_class \
$node_properties $entity_file $data_file $config_file $p_key $p_val"

echo $cmd;
eval $cmd;
