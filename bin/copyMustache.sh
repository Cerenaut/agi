#!/bin/sh

MUSTACH_DIR="$AGI_PROJECT_DIR/resources/swagger-mustache"

cmd="cp -r $MUSTACH_DIR/* $SWAGGER_CODEGEN_DIR/modules/swagger-codegen/src/main/resources/"

echo $cmd;
eval $cmd;