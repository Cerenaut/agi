#!/bin/sh

TGT_DIR="$AGI_PROJECT_DIR/lib/CoordinatorServerLib"
SPEC_FILE="$AGI_PROJECT_DIR/ApiSpec/coordinator.yaml"
CONFIG_FILE="$AGI_PROJECT_DIR/ApiSpec/serverConfig.json"

mkdir -p $PROJECT_DIR/lib

cmd="java -jar $SWAGGER_CODEGEN/modules/swagger-codegen-cli/target/swagger-codegen-cli.jar generate \
  -i $SPEC_FILE \
  -c $CONFIG_FILE \
  -l jaxrs \
  -o $TGT_DIR"


echo $cmd;
eval $cmd;

bBuild=true
if [ "$bBuild" = true ] ; then
    echo 'Move files, build and install lib'

	mkdir -p $TGT_DIR/tmp/src/gen/

    rm -rf $TGT_DIR/tmp/src/gen/impl
    mv $TGT_DIR/src/main/java/io/agi/ef/serverapi/api/impl $TGT_DIR/tmp/src/gen/impl

    cd $TGT_DIR
    mvn package -q	# build quietly
    mvn install
fi