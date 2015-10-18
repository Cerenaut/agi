#!/bin/bash

echo "------- build all the library dependencies of the experimental-framework -------"
cd $AGI_HOME/algorithms/code/core
$MAVEN_BIN clean install

cd $AGI_HOME/experimental-framework/lib/CoordinatorClientLib
$MAVEN_BIN clean install

cd $AGI_HOME/experimental-framework/lib/CoordinatorServerLib
$MAVEN_BIN clean install

cd $AGI_HOME/experimental-framework/lib/PersistenceClientLib
$MAVEN_BIN clean install
