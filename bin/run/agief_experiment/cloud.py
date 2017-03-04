import boto3
import os
import botocore
import utils


class Cloud:

    log = False
    subnet_id = 'subnet-0b1a206e'   # ec2 instances will be launched into this subnet (in a vpc)
    cluster = 'default'             # for ecs, which cluster to use
    mainkeyname = 'nextpair'        # when creating ec2 instances, the root ssh key to use
    ec2_compute_securitygroup_id = 'sg-98d574fc'    # for compute hosts, which the security group to use
    availability_zone = 'ap-southeast-2a'           # az for all ec2 instances
    placement_group = 'MNIST-PGroup'                # placement group for ec2 instances
    # client_token = 'this_is_the_client_token_la_la_34'  # Unique, case-sensitive identifier you provide to ensure the idempotency of the request.
    network_interface_id = 'eni - b2acd4d4'

    def __init__(self, log):
        self.log = log

    def sync_experiment_rsync(self, host, keypath):
        """
        Sync experiment from this machine to remote machine
        Assumes there exists a private key for the given ec2 instance, at keypath
        """

        print "....... Use remote-sync-experiment.sh to rsync relevant folders."

        cmd = "../remote/remote-sync-experiment.sh " + host + " " + keypath
        utils.run_bashscript_repeat(cmd, 15, 6, verbose=self.log)

    def sync_experiment_s3(self, prefix, host, remote_keypath):
        # remote download of /output/prefix folder

        print "....... Use sync_experiment_s3.sh to copy files from s3 (typically input and data files) with prefix = "\
              + prefix

        cmd = "../remote/remote-download-output.sh " + " " + prefix + " " + host + " " + remote_keypath
        utils.run_bashscript_repeat(cmd, 15, 6, verbose=self.log)

    def launch_compute_docker(self, host, keypath):
        """ Assumes there exists a private key for the given ec2 instance, at keypath """

        print "....... Use remote-run.sh to launch compute node in a docker container on a remote host."

        cmd = "../remote/remote-run.sh " + host + " " + keypath
        utils.run_bashscript_repeat(cmd, 15, 6, verbose=self.log)

    def run_task_ecs(self, task_name):
        """ Run task 'task_name' and return the Task ARN """

        print "....... Running task on ecs "
        client = boto3.client('ecs')
        response = client.run_task(
            cluster=self.cluster,
            taskDefinition=task_name,
            count=1,
            startedBy='pyScript'
        )

        if self.log:
            print "self.log: ", response

        length = len(response['failures'])
        if length > 0:
            print "ERROR: could not initiate task on AWS."
            print "reason = " + response['failures'][0]['reason']
            print "arn = " + response['failures'][0]['arn']
            print " ----- exiting -------"
            exit(1)

        if len(response['tasks']) <= 0:
            print "ERROR: could not retrieve task arn when initiating task on AWS - something has gone wrong."
            exit(1)

        task_arn = response['tasks'][0]['taskArn']
        return task_arn

    def stop_task_ecs(self, task_arn):

        print "....... Stopping task on ecs "
        client = boto3.client('ecs')

        response = client.stop_task(
            cluster=self.cluster,
            task=task_arn,
            reason='pyScript said so!'
        )

        if self.log:
            print "self.log: ", response

    def run_ec2(self, instance_id):
        """
        Run the chosen instance specified by instance_id
        :return: the instance AWS public and private ip addresses
        """
        
        print "....... Starting ec2 (instance id " + instance_id + ")"
        ec2 = boto3.resource('ec2')
        instance = ec2.Instance(instance_id)
        response = instance.start()

        if self.log:
            print "self.log: Start response: ", response

        instance_id = instance.instance_id

        ips = self.wait_till_running_ec2(instance_id)
        return ips

    def wait_till_running_ec2(self, instance_id):
        """
        :return: the instance AWS public and private ip addresses
        """

        ec2 = boto3.resource('ec2')
        instance = ec2.Instance(instance_id)

        if self.log:
            print "wait_till_running for instance: ", instance

        instance.wait_until_running()

        ip_public = instance.public_ip_address
        ip_private = instance.private_ip_address

        print "Instance is up and running."
        print "Instance public IP address is: ", ip_public
        print "Instance private IP address is: ", ip_private

        return {'ip_public': ip_public, 'ip_private': ip_private}

    def stop_ec2(self, instance_id):
        print "...... Closing ec2 instance (instance id " + str(instance_id) + ")"
        ec2 = boto3.resource('ec2')
        instance = ec2.Instance(instance_id)

        ip_public = instance.public_ip_address
        ip_private = instance.private_ip_address

        print "Instance public IP address is: ", ip_public
        print "Instance private IP address is: ", ip_private

        response = instance.stop()

        if self.log:
            print "self.log: stop ec2: ", response

    def upload_experiment_s3(self, prefix, dest_name, source_filepath):
        """
        Upload experiment.
        :param prefix: experiment prefix (used in the full name of uploaded bucket)
        :param dest_name: the name for the eventual uploaded s3 object (it can be file or folder)
        :param source_filepath: the file or folder to be uploaded
        :return:
        """

        print "...... Uploading experiment to S3"

        bucket_name = "agief-project"
        key = "experiment-output/" + prefix + "/" + dest_name

        if os.path.isfile(source_filepath):
            self.upload_file_s3(bucket_name, key, source_filepath, self.log)
        else:
            for root, dirs, files in os.walk(source_filepath):
                for file in files:
                    filepath = os.path.join(source_filepath, file)
                    filekey = os.path.join(key, file)
                    self.upload_file_s3(bucket_name, filekey, filepath, self.log)

    def launch_from_ami_ec2(self, name, ami_id, min_ram):
        """
        :param ami_id: ami id
        :param min_ram: (integer), minimum ram to allocate to ec2 instance
        :return: ip addresses: public and private, and instance id
        """

        print "....... Launching ec2 from AMI (AMI id " + ami_id + ", with minimum " + str(min_ram) + "GB RAM)"

        instance_type = None      # minimum size, 15GB on machine, leaves 13GB for compute
        if min_ram < 6:
            instance_type = 'm4.large'      # 8
        elif min_ram < 13:
            instance_type = 'r3.large'      # 15.25
        elif min_ram < 28:
            instance_type = 'r3.xlarge'     # 30.5
        else:
            print "ERROR: cannot create an ec2 instance with that much RAM"
            exit(1)

        ec2 = boto3.resource('ec2')
        subnet = ec2.Subnet(self.subnet_id)

        instance = subnet.create_instances(
            DryRun=False,
            ImageId=ami_id,
            MinCount=1,
            MaxCount=1,
            KeyName=self.mainkeyname,

            SecurityGroupIds=[
                self.ec2_compute_securitygroup_id,
            ],
            InstanceType=instance_type,
            Placement={
                'AvailabilityZone': self.availability_zone,
                # 'GroupName': self.placement_group,
                'Tenancy': 'default'                # | 'dedicated' | 'host',
            },
            Monitoring={
                'Enabled': False
            },
            DisableApiTermination=False,
            InstanceInitiatedShutdownBehavior='terminate',      # | 'stop'
            # ClientToken=self.client_token,
            AdditionalInfo='started by run-framework.py',
            # IamInstanceProfile={
            #     'Arn': 'string',
            #     'Name': 'string'
            # },
            EbsOptimized=False
        )

        instance_id = instance[0].instance_id

        if self.log:
            print "Instance launched ", instance_id

        # set name
        response = ec2.create_tags(
            DryRun=False,
            Resources=[
                instance_id,
            ],
            Tags=[
                {
                    'Key': 'Name',
                    'Value': name
                },
            ]
        )

        if self.log:
            print "Set Name tag on instanceid: ", instance_id
            print "Response is: ", response

        ips = self.wait_till_running_ec2(instance_id)
        return ips, instance_id

    def upload_file_s3(self, bucket_name, key, source_filepath, log=False):

        if not os.path.exists(source_filepath):
            print "WARNING: file does not exist, cannot upload: " + source_filepath
            return

        s3 = boto3.resource('s3')

        exists = True
        try:
            s3.meta.client.head_bucket(Bucket=bucket_name)
        except botocore.exceptions.ClientError as e:
            # If a client error is thrown, then check that it was a 404 error.
            # If it was a 404 error, then the bucket does not exist.
            error_code = int(e.response['Error']['Code'])
            if error_code == 404:
                exists = False

        if not exists:
            print "WARNING: s3 bucket " + bucket_name + " does not exist, creating it now."
            s3.create_bucket(Bucket=bucket_name)

        print " ... file = " + source_filepath + ", to bucket = " + bucket_name + ", key = " + key
        response = s3.Object(bucket_name=bucket_name, key=key).put(Body=open(source_filepath, 'rb'))

        if self.log:
            print response
