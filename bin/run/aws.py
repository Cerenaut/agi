import boto3
import subprocess

import utils

log = False

# assumes there exists a private key for the given ec2 instance, at ~/.ssh/ecs-key
def sync_experiment(host, keypath):
    print "....... syncing code to ec2 container instance"

    # code
    file_path = utils.filepath_from_env_variable("", "AGI_HOME")
    cmd = "rsync -ave 'ssh -i " + keypath + "  -o \"StrictHostKeyChecking no\" ' " + file_path + " ec2-user@" + host + ":~/agief-project/agi --exclude={\"*.git/*\",*/src/*}"
    if log:
        print cmd
    output, error = subprocess.Popen(cmd,
                                     shell=True,
                                     stdout=subprocess.PIPE,
                                     stderr=subprocess.PIPE,
                                     executable="/bin/bash").communicate()
    if log:
        print output
        print error

    # experiments
    file_path = utils.filepath_from_env_variable("", "AGI_RUN_HOME")
    cmd = "rsync -ave 'ssh -i " + keypath + "  -o \"StrictHostKeyChecking no\" ' " + file_path + " ec2-user@" + host + ":~/agief-project/run --exclude={\"*.git/*\"}"
    if log:
        print cmd
    output, error = subprocess.Popen(cmd,
                                     shell=True,
                                     stdout=subprocess.PIPE,
                                     stderr=subprocess.PIPE,
                                     executable="/bin/bash").communicate()
    if log:
        print output
        print error


# Run ecs task
def run_task(task_name):

    print "....... running task on ecs "
    client = boto3.client('ecs')
    response = client.run_task(
        cluster='default',
        taskDefinition=task_name,
        count=1,
        startedBy='pyScript'
    )

    if log:
        print "LOG: ", response

    length = len(response["failures"])
    if length > 0:
        print "ERROR: could not initiate task on AWS."
        print "reason = " + response["failures"][0]["reason"]
        print " ----- exiting -------"
        exit(1)


# run the chosen instance specified by instanceId
def run_ec2(instance_id):
    print "....... starting ec2"
    ec2 = boto3.resource('ec2')
    instance = ec2.Instance(instance_id)
    response = instance.start()

    if log:
        print "LOG: Start response: ", response

    ip_public = instance.public_ip_address
    ip_private = instance.private_ip_address

    instance.wait_until_running()

    print "Instance is up and running."
    print "Instance public IP address is: ", ip_public
    print "Instance private IP address is: ", ip_private

    return {'ip_public': ip_public, 'ip_private': ip_private}


def close(instance_id):
    ec2 = boto3.resource('ec2')
    instance = ec2.Instance(instance_id)
    response = instance.stop()

    if log:
        print "LOG: stop ec2: ", response
