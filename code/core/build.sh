#!/bin/bash

# Update project version
./src/main/scripts/update-version.sh

# Build project
mvn package
