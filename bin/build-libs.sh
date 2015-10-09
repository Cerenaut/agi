#!/bin/sh

echo "------- build all the library dependencies of the experimental-framework -------"
cd $AGI_HOME/src/core
mvn clean install

cd $AGI_HOME/lib/CoordinatorClientLib
mvn clean install

cd $AGI_HOME/lib/CoordinatorServerLib
mvn clean install

cd $AGI_HOME/lib/PersistenceClientLib
mvn clean install
