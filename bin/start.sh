#!/bin/sh

# build all the library dependencies of the experimental-framework
cd $AGI_PROJECT_DIR/src/core
mvn clean install

cd $AGI_PROJECT_DIR/lib/CoordinatorClientLib
mvn clean install

cd $AGI_PROJECT_DIR/lib/CoordinatorServerLib
mvn clean install

cd $AGI_PROJECT_DIR/lib/PersistenceClientLib
mvn clean install

# build the experimental framework 
cd $AGI_PROJECT_DIR/src/experimental-framework/core-modules
mvn clean package