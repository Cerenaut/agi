# Dockerfile for dashing 
# http://shopify.github.io/dashing/#overview

FROM ubuntu:14.10

MAINTAINER Gideon Kowadlo <gideon@agi.io>

RUN apt-get update && apt-get install -y \
git \
maven \
openjdk-8-jdk \
postgresql postgresql-contrib

# Setup environment (build and install libs)
WORKDIR /root/dev/agi/bin
RUN setup.sh

# Starting directory
WORKDIR /root/dev/agi/

CMD ["run.sh"]