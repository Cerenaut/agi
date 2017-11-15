#!/bin/bash

########################################################################################################
# Usage: use this script to build or run in a docker container.
# 
# BUILD
# To build, use the command 'build' and then any paramters that you want passed to build.
# This will use bin/node_coordinator/build.sh
# e.g. ./run-in-docker.sh build [params].       (at the moment, no params are necessary)
# 
# RUN
# To run, just pass parameters. This will use /bin/node_coordinator/run.sh
# e.g. ./run-in-docker.sh [params]
# 
########################################################################################################

default="$(dirname $0)/../variables.sh"
variables_file=${VARIABLES_FILE:-$default}
echo "Using variables file = $variables_file" 
source $variables_file

detached=0

while getopts ":d" opt; do
  case $opt in
    d)  # run in detached mode (i.e. it will return, and the container will exit when the running command terminates)
      detached=1
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


shift $(($OPTIND - 1))                  # so that we can deal with mass-arguments as per normal

set -e                                  # stops the execution of a script if a command or pipeline has an error 
cd "$(dirname $BASH_SOURCE)"            # change to directory of the script

maven_cache_repo="$HOME/.m2/repository"
myname="$(basename $BASH_SOURCE)"

if [ "$1" = "build" ]; then
        cmd="./build.sh"
        shift
        args="$@"
else
        run_script="run.sh"
        
        # Check if script exists
        if [ ! -f "$run_script" ]; then
                echo "ERROR File not found: $run_script"
                # echo "ERROR Did you forget to './$myname mvn package'?"
                exit 1
        fi
        
        cmd="./$run_script -f"
        args="$@"
fi

set +e

mkdir -p "$maven_cache_repo"


if [ $detached -eq 1 ]; then
        switch="-d"
else
        echo "NOT DETACHED"
        if [ "`tty`" == "not a tty" ]; then
                switch="-i"
        else
                switch="-it"
        fi
fi


# should use the script /docker/run.sh,  but thinking of deprecating, not worth maintaining another script that isn't that useful
dcmd="docker run $switch
        -w /root/dev/agi/bin/node_coordinator
        -e AGI_LOGZIO_TOKEN=$AGI_LOGZIO_TOKEN
        -e VARIABLES_FILE='/root/dev/variables/variables-docker.sh'
        -v $AGI_HOME:/root/dev/agi
        -v $AGI_DATA_HOME:/root/dev/data
        -v $AGI_EXP_HOME/../variables:/root/dev/variables
        -v $AGI_RUN_HOME:/root/dev/run
        -v '${maven_cache_repo}:/root/.m2/repository'
        -p 8491:8491 -p 5432:5432
        gkowadlo/agief:2.1 $cmd $args"

# echo $dcmd

set -x

eval $dcmd
