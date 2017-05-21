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

default="$(dirname $0)/../variables.sh"
variables_file=${VARIABLES_FILE:-$default}
echo "Using variables file = $variables_file" 
source $variables_file


while getopts ":p:e:d:l:m:k:v:c:f" opt; do
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
    f)  # redirect stdout and stderr to log files
      std_stream=" > stdout.log 2> stderr.log "
      ;;
    r)  # specify RAM available to JVM
      opt_r=$OPTARG
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
max_ram=${opt_r:-28}

mem="-Xmx"$max_ram"g" #" -Xms"$min_ram"g -Xmx"$min_ram"g "     # set min and max to be the same to minimise garbage collection
dburl_default="jdbc:postgresql://$DB_HOST:$DB_PORT/agidb"

# e.g. key: database-url value: jdbc:postgresql://localhost:5432/agidb
p_key=${opt_p_key:-database-url}          # if it was not defined, that is ok, it is passed as null and ignored
p_val=${opt_p_val:-$dburl_default} 		    # if it was not defined, that is ok, it is passed as null and ignored

echo "Create classpath file cp.txt"
cd $AGI_HOME/code/core
pwd
cmd="mvn dependency:build-classpath -Dmdep.outputFile=$AGI_RUN_HOME/cp.txt"
eval $cmd;

echo "Run home = $AGI_RUN_HOME"
cd $AGI_RUN_HOME
pwd

# run coordinator
cmd="$JAVA_HOME/bin/java $mem -Dfile.encoding=UTF-8 -Dlog4j.configurationFile=file:$log_config \
-cp `cat cp.txt`:$AGI_HOME/code/core/target/agief.jar $main_class \
$node_properties $entity_file $data_file $config_file $p_key $p_val $std_stream"

echo $cmd;
eval $cmd;
