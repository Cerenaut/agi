#!/bin/sh

cmd="$AGI_HOME/bin/genServer.sh"
echo $cmd;
eval $cmd;

cmd="$AGI_HOME/bin/genClient.sh"
echo $cmd;
eval $cmd;

cmd="$AGI_HOME/bin/genPersistence.sh"
echo $cmd;
eval $cmd;