#!/bin/bash

######################################################################
# Use this 'variables.sh' if you are running the system locally, 
# or if you are are deploying Docker containters from this local environment.
######################################################################


# ----------------------------------------
# AGI Home 
# ---------------------------------------
export AGI_HOME=$WORKSPACE/agi

# RUN Home
#export AGI_RUN_HOME=$WORKSPACE/experiment-definitions/mnist-gng-v1
export AGI_RUN_HOME=$WORKSPACE/experiment-definitions/mnist-autoencoder-v2

# DATA Home (set to the home of the data for the actual experiment - i.e. in the environment where they'll be running)
# export AGI_DATA_HOME=~/Development/ProjectAGI/AGIEF/datasets/MNIST      # when running locally
export AGI_DATA_HOME=/root/dev/data

# Database
export DB_PORT=5432
# export DB_HOST=localhost		# IMPORTANT!!!  DO NOT DEFINE THIS VARIABLE.   It will be defined by scripts at runtime, and we don't want it to get defined by sourcing this file

