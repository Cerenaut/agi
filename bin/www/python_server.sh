#!/bin/bash

default="$(dirname $0)/../variables.sh"
variables_file=${VARIABLES_FILE:-$default}
echo "Using variables file = $variables_file" 
source $variables_file

cd $AGI_HOME/code/www
python -m SimpleHTTPServer 8000
