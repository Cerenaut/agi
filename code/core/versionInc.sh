#!/bin/bash

####################################################################################
# Based on discussion here:
# http://stackoverflow.com/questions/3545292/how-to-get-maven-project-version-to-the-bash-command-line
#
# NOTE: At the moment, this cannot cope with versions that contain a git commit, they must be of the format:
# X.Y.Z-STRING
####################################################################################


# Advances the last number of the given version string by one.
function advance_version () {
    local v=$1
    # Get the last number. First remove any suffixes (such as '-SNAPSHOT').
    local cleaned=`echo $v | sed -e 's/[^0-9][^0-9]*$//'`
    local last_num=`echo $cleaned | sed -e 's/[0-9]*\.//g'`
    local next_num=$(($last_num+1))
    # Finally replace the last number in version string with the new one.
    echo $v | sed -e "s/[0-9][0-9]*\([^0-9]*\)$/$next_num/"
}


echo 'Update version in pom.xml files ...'

# get version from pom.xml
version=`mvn -Dexec.executable='echo' -Dexec.args='${project.version}' --non-recursive exec:exec | grep -v '\['`
new_version=$(advance_version $version)

# run maven versions plugin to set new version
mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$new_version 2 > /dev/null
echo "Changed version in pom.xml files to $new_version"
