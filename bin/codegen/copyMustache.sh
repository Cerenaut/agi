#!/bin/sh

MUSTACHE_DIR="$AGI_HOME/resources/swagger-mustache"

cmd="cp -r $MUSTACHE_DIR/* $SWAGGER_HOME/modules/swagger-codegen/src/main/resources/"

echo $cmd;
eval $cmd;