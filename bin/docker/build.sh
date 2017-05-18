#!/bin/bash

default="$(dirname $0)/../variables.sh"
variables_file=${VARIABLES_FILE:-$default}
echo "Using variables file = $variables_file"	
source $variables_file

if [ "$1" == "-h" -o "$1" == "--help" ]; then
  echo "Usage: `basename $0` IMAGE_NAME"
  echo "All arguments are optional."
  exit 0
fi

image_name=${1:-agief}

docker build -t $image_name -f $AGI_HOME/bin/docker/Dockerfile $AGI_HOME/..