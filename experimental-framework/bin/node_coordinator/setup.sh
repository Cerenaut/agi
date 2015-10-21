#!/bin/bash

# !!!!!  YOU MUST `source variables.sh` before running this script

echo "------- Build and install libs -------"
$AGI_HOME/experimental-framework/bin/node_coordinator/build-libs.sh

echo "------- build the experimental framework -------"
cd $AGI_HOME/experimental-framework/code/core-modules
$MAVEN_BIN clean package
