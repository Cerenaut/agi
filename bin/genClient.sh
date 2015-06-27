#!/bin/sh

PROJECT_DIR="$HOME/Development/AI/ProAGI/agi"
TGT_DIR="$PROJECT_DIR/lib/CoordinatorClientLib"
SPEC_FILE="$PROJECT_DIR/ApiSpec/coordinator.yaml"

mkdir -p $PROJECT_DIR/lib

cmd="java -jar $SWAGGER_CODEGEN/modules/swagger-codegen-cli/target/swagger-codegen-cli.jar generate \
  -i $SPEC_FILE \
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
