#!/bin/sh

TGT_DIR="$AGI_HOME/lib/PersistenceClientLib"
SPEC_FILE="$AGI_HOME/src/experimental-framework/api/api-spec/persistence.yaml"
CONFIG_FILE="$AGI_HOME/src/experimental-framework/api/api-spec/persistenceConfig.json"

mkdir -p $AGI_HOME/lib

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
