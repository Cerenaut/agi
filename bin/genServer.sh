#!/bin/sh

java -jar ~/Development/Tools/swagger-codegen/modules/swagger-codegen-cli/target/swagger-codegen-cli.jar generate \
  -i ../APISpec/coordinator.yaml \
  -l jaxrs \
  -o ../AGICoordinatorServer