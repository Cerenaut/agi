#!/bin/bash

variables_file=${VARIABLES_FILE:-"variables.sh"}
echo "Using variables file = \"$variables_file\""
source $(dirname $0)/../$variables_file

if [ "$1" == "-h" -o "$1" == "--help" ]; then
  echo "Usage: `basename $0` IMAGE_NAME CONTAINER_NAME PSQL_CONTAINER_NAME"
  echo "All arguments are optional. 
  		This command runs a container from the docker image with the name IMAGE_NAME.
  		If PSQL_CONTAINER_NAME is specified, then the container is 'linked' to this container, if not specified, then no action is taken."
  exit 0
fi

image_name=${1:-agief}

if [ -z "$2" ]
then
    containter_name_cmd="";
else
	containter_name_cmd="--name $2";		# --name AGI_NODE
fi

if [ -z "$3" ]
then
    psql_container_cmd="";
else
	psql_container_cmd="--link $3";			# --link postgres:db 
fi

if [ -z "$DB_PORT" ]
then
    db_map_port_cmd="";
else
	db_map_port_cmd="-p $DB_PORT:$DB_PORT";
fi

cmd="docker run $containter_name_cmd $psql_container_cmd $db_map_port_cmd -i -d -v $AGI_HOME:/root/dev/agi -v $AGI_RUN_HOME:/root/dev/run $image_name";
echo $cmd;
eval $cmd;