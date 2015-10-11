#!/bin/bash

MUSTACHE_DIR="$AGI_HOME/experimental-framework/resources/swagger-mustache"

cmd="cp -r $MUSTACHE_DIR/* $SWAGGER_HOME/modules/swagger-codegen/src/main/resources/"

echo $cmd;
eval $cmd;