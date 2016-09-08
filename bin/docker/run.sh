#!/bin/bash

variables_file=${VARIABLES_FILE:-"variables.sh"}
echo "Using variables file = \"$variables_file\""
source $(dirname $0)/../$variables_file

if [ "$1" == "-h" -o "$1" == "--help" ]; then
  echo "Usage: `basename $0` IMAGE_NAME(default=agief) COORD_PORT CONTAINER_NAME PSQL_CONTAINER_NAME DB_PORT"
  echo "All arguments are optional. 
  		This command runs a container from the docker image with the name IMAGE_NAME.
  		If PSQL_CONTAINER_NAME is specified, then the container is 'linked' to this container, if not specified, then no action is taken."
  exit 0
fi

# which image to use for this contaienr
image_name=${1:-agief}

# give this container a name 
if [ -z "$2" ]
then
  containter_name_cmd="";
else
	containter_name_cmd="--name $2";		# --name AGI_NODE
fi

# map port - for GUI to get access to coordinator
if [ -z "$3" ]
then
  coord_map_port_cmd="";
else
	coord_map_port_cmd="-p $3:$3";			# -p 8491:8491
fi

# link containers (usually to link to postgres)
if [ -z "$4" ]
then
  psql_container_cmd="";
else
	psql_container_cmd="--link $4";			# --link postgres:db 
fi

# map port - Database port
if [ -z "$5" ]
then
  db_map_port_cmd="";
else
	db_map_port_cmd="-p $5:$5";
fi

cmd="docker run $containter_name_cmd $psql_container_cmd $coord_map_port_cmd $db_map_port_cmd -i -d -v $AGI_HOME:/root/dev/agi -v $AGI_RUN_HOME:/root/dev/run $image_name";
echo $cmd;
eval $cmd;