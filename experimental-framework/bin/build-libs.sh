#!/bin/bash

echo "------- build all the library dependencies of the experimental-framework -------"
cd $AGI_HOME/algorithms/code/core
mvn clean install

cd $AGI_HOME/experimental-framework/lib/CoordinatorClientLib
mvn clean install

cd $AGI_HOME/experimental-framework/lib/CoordinatorServerLib
mvn clean install

cd $AGI_HOME/experimental-framework/lib/PersistenceClientLib
mvn clean install
