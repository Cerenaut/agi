# Dockerfile for dashing 
# http://shopify.github.io/dashing/#overview

FROM ubuntu:14.10

MAINTAINER Gideon Kowadlo <gideon@agi.io>

RUN apt-get update && apt-get install -y \
git maven openjdk-7-jre

    
# Install dependencies
WORKDIR /agi
RUN git pull git@github.com:ProjectAGI/agi.git
RUN git pull git@github.com:swagger-api/swagger-codegen.git
RUN export SWAGGER_CODEGEN=./swagger-codegen
RUN export AGI_PROJECT_DIR=./agi

# Starting directory
WORKDIR /agi
