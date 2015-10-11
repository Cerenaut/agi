#!/bin/bash

TGT_DIR="$AGI_HOME/experimental-framework/lib/CoordinatorServerLib"
SPEC_FILE="$AGI_HOME/experimental-framework/api/api-spec/coordinator.yaml"
CONFIG_FILE="$AGI_HOME/experimental-framework/api/api-spec/serverConfig.json"

mkdir -p $AGI_HOME/experimental-framework/lib

cmd="java -jar $SWAGGER_HOME/modules/swagger-codegen-cli/target/swagger-codegen-cli.jar generate \
  -i $SPEC_FILE \
  -c $CONFIG_FILE \
  -l jaxrs \
  -o $TGT_DIR"

echo $cmd;

bRun=true;
if [ "$bRun" = true ] ; then

eval $cmd;

bBuild=true;
if [ "$bBuild" = true ] ; then
    echo 'Move files, build and install lib'

  mkdir -p $TGT_DIR/tmp/src/gen/

  rm -rf $TGT_DIR/tmp/src/gen/impl
  mv $TGT_DIR/src/main/java/io/agi/ef/serverapi/api/impl $TGT_DIR/tmp/src/gen/impl

  cd $TGT_DIR
  mvn package -q	# build quietly
  mvn install
fi

fi