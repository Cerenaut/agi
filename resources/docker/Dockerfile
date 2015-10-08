# Dockerfile for dashing 
# http://shopify.github.io/dashing/#overview

FROM ubuntu:14.10

MAINTAINER Gideon Kowadlo <gideon@agi.io>

RUN apt-get update && apt-get install -y \
git maven openjdk-8-jdk

# Install dependencies
WORKDIR /tmp

# Starting directory
WORKDIR /root/dev/agi
