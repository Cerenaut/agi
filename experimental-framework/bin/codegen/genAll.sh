#!/bin/bash

cmd="$AGI_HOME/experimental-framework/bin/codegen/genServer.sh"
echo $cmd;
eval $cmd;

cmd="$AGI_HOME/experimental-framework/bin/codegen/genClient.sh"
echo $cmd;
eval $cmd;

cmd="$AGI_HOME/experimental-framework/bin/codegen/genPersistence.sh"
echo $cmd;
eval $cmd;