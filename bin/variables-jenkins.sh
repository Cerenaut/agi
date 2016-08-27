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
export AGI_RUN_HOME=$WORKSPACE/experiment-definitions/classifier

# Database
export DB_PORT=5432
export DB_HOST=localhost