#!/bin/bash

# synch code and run folder with ecs instance (via S3)
./aws-s3-sync-up.sh
ssh -i "nextpair.pem" ec2-user@ec2-52-63-215-106.ap-southeast-2.compute.amazonaws.com 'bash -s' < aws-s3-sync-down.sh