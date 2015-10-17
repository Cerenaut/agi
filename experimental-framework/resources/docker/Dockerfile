# Dockerfile for AGI Experimental Framework
# Container for deployment of the framework
# http://agi.io

FROM ubuntu:15.04

MAINTAINER Gideon Kowadlo <gideon@agi.io>

RUN apt-get update && apt-get install -y \
git \
maven \
openjdk-8-jdk

# Setup environment (build and install libs)
WORKDIR /root/dev/agi/experimental-framework/bin
RUN source variables-docker.sh

CMD ["./node_coordinator/setup.sh"]