#!/bin/sh

TGT_DIR="$AGI_PROJECT_DIR/lib/CoordinatorClientLib"
SPEC_FILE="$AGI_PROJECT_DIR/ApiSpec/coordinator.yaml"
CONFIG_FILE="$AGI_PROJECT_DIR/ApiSpec/clientConfig.json"

mkdir -p $AGI_PROJECT_DIR/lib

cmd="java -jar $SWAGGER_CODEGEN_DIR/modules/swagger-codegen-cli/target/swagger-codegen-cli.jar generate \
  -i $SPEC_FILE \
  -c $CONFIG_FILE \
  -l java \
  -o $TGT_DIR"

echo $cmd;
eval $cmd;

bBuild=true
if [ "$bBuild" = true ] ; then
    echo 'Build and install lib'

    cd $TGT_DIR
    mvn package -q	# build quietly
    mvn install
fi
