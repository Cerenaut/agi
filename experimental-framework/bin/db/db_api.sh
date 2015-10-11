#!/bin/bash

# Runs postgREST HTTP service
# This creates a HTTP interface to the database, which persists all data for the distributed system.
#
# NB: Default bash arguments: http://stackoverflow.com/questions/9332802/how-to-write-a-bash-script-that-takes-optional-input-arguments
if [ "$1" == "-h" -o "$1" == "--help" ]; then
  echo "Usage: `basename $0` API_PORT DATABASE_HOST DATABASE_PASSWORD DATABASE_PORT"
  echo "All arguments are optional."
  exit 0
fi

#./postgrest-0.2.10.0 --db-host ${2:localhost}  --db-port 5432 --db-name agidb  --db-user agiu --db-pass password --db-pool 200 --anonymous agiu --port 8080 --v1schema public
$AGI_HOME/experimental-framework/bin/db/postgrest-0.2.10.0 --db-host ${2:-localhost}  --db-port ${4:-5432} --db-name agidb  --db-user agiu --db-pass ${3:-password} --db-pool 200 --anonymous agiu --port ${1:-8080} --v1schema public
