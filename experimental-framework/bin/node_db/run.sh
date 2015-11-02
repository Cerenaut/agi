#!/bin/bash

variables_file=${VARIABLES_FILE:-"variables.sh"}
echo "Using variables file = \"$variables_file\""
source $(dirname $0)/../$variables_file


if [ "$1" == "-h" -o "$1" == "--help" ]; then
  echo "Usage: `basename $0` DATABASE_API_PORT DATABASE_HOST DATABASE_PORT DATABASE_PASSWORD"
  echo "All arguments are optional."
  exit 0
fi

database_api_port=${1:-$DATABASE_API_PORT}   
database_host=${2:-$DATABASE_HOST} 
database_port=${3:-$DATABASE_PORT}
database_password=${4:-$DATABASE_PASSWORD}

database_name="agidb"
database_username="agiu"


# run postgrest db api
cmd="$POSTGREST_BIN --db-host $database_host  --db-port $database_port  --db-name $database_name  --db-user $database_username  --db-pass $database_password  --db-pool 200  --anonymous $database_username  --port $database_api_port  --v1schema public"
echo "---- Running: ----"
echo $cmd;
eval $cmd;