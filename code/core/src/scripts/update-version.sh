#!/bin/bash

####################################################################################
# Based on discussions here:
# 	http://stackoverflow.com/questions/3545292/how-to-get-maven-project-version-to-the-bash-command-line
# 	http://stackoverflow.com/questions/13583953/deriving-maven-artifact-version-from-git-branch
#
# Version Format:
# 	X.Y.Z(git hash)
####################################################################################


# Advances the last number of the given version string by one.
# param1 = version : Format of version is:   X.Y.Z(githash)		(note: can include X, X.Y, or X.Y.Z)
# param2 = new git commit hash
function advance_version () {
    local v=$1
    local new_commit_hash=$2
    # Get the last number. First strip off trailing githash
    local cleaned=`echo $v | sed -e 's/([0-9 a-z A-Z]*)//'`
    local last_num=`echo $cleaned | sed -e 's/[0-9]*\.//g'`
    local next_num=$(($last_num+1))
    # Finally replace the last number in version string with the new one.
    echo $cleaned | sed -e "s/[0-9]*$/$next_num($new_commit_hash)/"
}

echo 'Update version in pom.xml files ...'

# get current branch name
branch=$(git rev-parse --abbrev-ref HEAD)
commit=$(git rev-parse --short HEAD)


# get version from pom.xml
version=`mvn -Dexec.executable='echo' -Dexec.args='${project.version}' --non-recursive exec:exec | grep -v '\['`
new_version=$(advance_version $version $commit)

# run maven versions plugin to set new version
mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$new_version 2 > /dev/null
echo "Changed version in pom.xml files to $new_version"
