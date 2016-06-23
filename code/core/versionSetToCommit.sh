#!/bin/bash

####################################################################################
# Based on discussion here:
# http://stackoverflow.com/questions/13583953/deriving-maven-artifact-version-from-git-branch
#
####################################################################################


echo 'Update version in pom.xml files ...'

version=`mvn -Dexec.executable='echo' -Dexec.args='${project.version}' --non-recursive exec:exec | grep -v '\['`

# get current branch name
branch=$(git rev-parse --abbrev-ref HEAD)
commit=$(git rev-parse --short HEAD)

# run maven versions plugin to set new version
mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$commit

echo "Changed version in pom.xml files to $commit"
