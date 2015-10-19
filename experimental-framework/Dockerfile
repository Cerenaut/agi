# Dockerfile for AGI Experimental Framework
# Container for deployment of the framework
# http://agi.io

FROM debian:jessie 

MAINTAINER Gideon Kowadlo <gideon@agi.io>

RUN apt-get update && apt-get install -y \
git \
maven \
&& add-apt-repository ppa:openjdk-r/ppa \
&& apt-get update \
&& apt-get install openjdk-8-jdk

# Setup environment (build and install libs)
WORKDIR /root/dev/agi/experimental-framework/bin
RUN source variables-docker.sh

CMD ["./node_coordinator/setup.sh"]