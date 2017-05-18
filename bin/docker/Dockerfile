# Dockerfile for AGI Experimental Framework
# Container for deployment of the framework
# http://agi.io

FROM ubuntu:15.04

MAINTAINER Gideon Kowadlo <gideon@agi.io>

RUN apt-get update && apt-get install -y \
openjdk-8-jdk

RUN apt-get update && apt-get install -y \
maven \
git \
curl

# Use docker specific variables.sh file (install of default at /bin/variables.sh)
ENV VARIABLES_FILE variables-docker.sh

# Run coordinator
WORKDIR /root/dev/agi/bin
CMD ["./node_coordinator/run.sh"]