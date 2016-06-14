import json
import boto3
import subprocess
import dpath.util

########################################################################
# This runs sync-experiment.sh, which relies on this line
# ssh ec2-user@c.x.agi.io    ssh-ing into the desired ec2 instance
# the same instance, for which the id needs to be specified here (hard coded currently, but should be command line or otherwise)
#


def setup_aws(instanceId):
    print "....... starting ec2"
    ec2 = boto3.resource('ec2')
    instance = ec2.Instance(instanceId)
    response = instance.start()

    if log : print "LOG: Start respones: ", response

    instance.wait_until_running()


def close_aws():
    ec2 = boto3.resource('ec2')
    instance = ec2.Instance(instanceId)
    response = instance.stop()

    if log : print "LOG: stop ec2: ", response


def run_experiment(task_name):
    print "....... Run Experiment"

    print "....... syncing files to ec2 container instance"
    subprocess.call(["../aws-ecs/ecs-sync-experiment.sh"])

    print "....... running task on ecs "
    client = boto3.client('ecs')
    response = client.run_task(
        cluster='default',
        taskDefinition=task_name,
        count=1,
        startedBy='pyScript'
    )

    if log : print "LOG: run task: ", response

    # wait for the task to finish
    # do this by polling an API or one of the config params
    #  -------- TODO             <-------------  ********* To Be Implemented on the Java side **********


def modify_param(file_entities, entity_name, param_path, val):

    print "Modify Param: ", file_entities, param_path, val

    # open the json
    with open(file_entities) as data_file:    
        data = json.load(data_file)

    # get the first element in the array with dictionary field "entity-name" = entity_name
    entity = dict()
    for entity_i in data :
        if not entity_i["name"] == entity_name : continue

        entity = entity_i
        break;

    # get the config field, and turn it into valid JSON
    configStr = entity["config"]
    configStr = configStr.replace("\\\"", "\"")
    config = json.loads(configStr)

    if log : print "LOG: config(t)   = ", config, '\n'

    # modify the param
    if log : print "param_path, val", param_path, val, '\n'

    dpath.util.set(config, param_path, val, '.')
    if log : print "LOG: config(t+1) = ", config, '\n'

    # put the escape characters back in the config str and write back to file
    configStr = json.dumps(config)
    configStr = configStr.replace("\"", "\\\"")
    entity["config"] = configStr

    # write back to file
    with open(file_entities, 'w') as data_file:    
        data_file.write(json.dumps(data))


if __name__ == '__main__':
    import argparse
    parser = argparse.ArgumentParser(
	description='Run an AGIEF experiment, potentially with parameter sweep.')
    parser.add_argument('--inputFile', dest='expFile', required=True,
            help='filename, in json format, that contains a list of params to sweep, with a range.')
    parser.add_argument('--instancdId', dest='instanceId', required=False,
            help='Instance ID of the ec2 container instance.')
    parser.add_argument('--taskName', dest='task_name', required=False,
            help='The name of the ecs task.')
    parser.add_argument('--dont_close_aws', dest='dont_close_aws', action='store_true',
            help='Shut down aws setup when finished (at the moment just ec2)')
    parser.add_argument('--logging', dest='logging', action='store_true', help='Turn logging on.')
    parser.set_defaults(logging=False)
    parser.set_defaults(instanceId="i-06d6a791")
    parser.set_defaults(task_name="mnist-spatial-task:3")
    parser.set_defaults(dont_close_aws=False)
    args = parser.parse_args()

    global log 
    log = args.logging

    instanceId = args.instanceId
    task_name = args.task_name
    dont_close_aws = args.dont_close_aws

    if log : print "LOG: Arguments: ", args

    if args.expFile:

        # setup_aws(instanceId)

        with open(args.expFile) as data_file:    
            data = json.load(data_file)

        for experiments in data["experiments"]:
            import_files = experiments["import-files"]                         # import files dictionary 

            if log:
                print "LOG: Import Files Dictionary = "
                print "LOG: ", import_files

            file_entities = import_files["file-entities"]

            for param_sweep in experiments["parameter-sweeps"]:
                entity_name = param_sweep["entity-name"]
                param_path  = param_sweep["parameter-path"]
                exp_type    = param_sweep["val-type"]
                val_begin   = param_sweep["val-begin"]
                val_end     = param_sweep["val-end"]
                val_inc     = param_sweep["val-inc"]

                if log:
                    print "LOG: Parameter Sweep Dictionary"
                    print "LOG: ", param_sweep

                for val in xrange(val_begin, val_end, val_inc):
                    modify_param(file_entities, entity_name , param_path, val)
                    #run_experiment(task_name)

        if not dont_close_aws: close_aws()