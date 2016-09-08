#!/bin/bash

variables_file=${VARIABLES_FILE:-"variables.sh"}
echo "Using variables file = \"$variables_file\""
source $(dirname $0)/../$variables_file


if [ "$1" == "-h" -o "$1" == "--help" ]; then
  echo "Usage: `basename $0` DATABASE_HOST DATABASE_PORT"
  echo "All arguments are optional."
  exit 0
fi


database_host=${1:-$DB_HOST} 
database_port=${2:-$DB_PORT}

# create agidb in database_host
$POSTGRESQL_BIN -h $database_host -p $database_port -U postgres -f $AGI_HOME/bin/db/agidb.sql



