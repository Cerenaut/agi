#!/bin/bash


# !!!!!  YOU MUST `source variables.sh` before running this script


if [ "$1" == "-h" -o "$1" == "--help" ]; then
  echo "Usage: `basename $0` API_PORT DATABASE_HOST DATABASE_PASSWORD DATABASE_PORT"
  echo "All arguments are optional."
  exit 0
fi


api_port=${1:-$API_PORT}   
database_host=${2:-$DATABASE_HOST} 
database_password=${3:-$DATABASE_PASSWORD}
database_port=${4:-$DATABASE_PORT}

# create agidb in database_host
psql -h $database_host -p $database_port -U postgres -f $AGI_HOME/experimental-framework/resources/sql/agidb.sql

# run postgrest db api
$AGI_HOME/experimental-framework/bin/node_db/db_api.sh $api_port $database_host $database_password $database_port



