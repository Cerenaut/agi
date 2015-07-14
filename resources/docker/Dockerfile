# Dockerfile for dashing 
# http://shopify.github.io/dashing/#overview

FROM ubuntu:14.10

MAINTAINER Gideon Kowadlo <gideon@agi.io>

RUN apt-get update && apt-get install -y \
git maven openjdk-7-jre

    
# Install dependencies
WORKDIR /tmp

RUN export SWAGGER_CODEGEN_DIR=/root/dev/swagger-codegen
RUN export AGI_PROJECT_DIR=/root/dev/agi

# Starting directory
WORKDIR /root/dev
