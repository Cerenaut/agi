# Dockerfile for AGI Experimental Framework
# Container for deployment of the framework
# http://agi.io

FROM ubuntu:15.04

MAINTAINER Gideon Kowadlo <gideon@agi.io>

RUN apt-get update && apt-get install -y \
maven \ 
openjdk-8-jdk \
postgresql postgresql-contrib

# Install dependencies - setup environment (build and install libs)
WORKDIR /tmp
ADD bin bin
Add lib lib
RUN /bin/bash -c "source bin/variables-docker.sh"

WORKDIR bin/node_coordinator
RUN /bin/bash -c "./setup.sh"

# set up a coordinator node
# WORKDIR /root/dev/agi/experimental-framework/bin
# CMD ["bin/node_coordinator/run.sh", "coordinator"]