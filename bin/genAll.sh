#!/bin/sh

cmd="$AGI_PROJECT_DIR/bin/genServer.sh"
echo $cmd;
eval $cmd;

cmd="$AGI_PROJECT_DIR/bin/genClient.sh"
echo $cmd;
eval $cmd;