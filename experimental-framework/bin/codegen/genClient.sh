#!/bin/bash

TGT_DIR="$AGI_HOME/experimental-framework/lib/CoordinatorClientLib"
SPEC_FILE="$AGI_HOME/experimental-framework/api/api-spec/coordinator.yaml"
CONFIG_FILE="$AGI_HOME/experimental-framework/api/api-spec/clientConfig.json"

mkdir -p $AGI_HOME/experimental-framework/lib

cmd="java -jar $SWAGGER_HOME/modules/swagger-codegen-cli/target/swagger-codegen-cli.jar generate \
  -i $SPEC_FILE \
  -c $CONFIG_FILE \
  -l java \
  -o $TGT_DIR"

echo $cmd;
eval $cmd;

bBuild=true;
if [ "$bBuild" = true ] ; then
    echo 'Build and install lib'

    cd $TGT_DIR
    mvn package -q	# build quietly
    mvn install
fi
