import json
import json
import os

import boto3
import subprocess
import dpath.util
import requests
import time

help_generic = """ 
Run an experiment using AGIEF on AWS (including ability to specify a parameter sweep).
Uses the version of code in $AGI_HOME and the experiment 'run' folder specified in $AGI_RUN_HOME
See README.md for installation instructions.

The script does the following:
- launch ec2 container instance
- sync $AGI_HOME folder (excluding source), and $AGI_RUN_HOME folder to the ec2 instance
- sweep parameters as specified in experiment input file, and for each parameter value,
- run the ecs task, which launches the framework, but does not run the experiment
- imports the experiment from the data files located in $AGI_RUN_HOME (*to implement*)
- runs the experiment until termination (*to implement*)
- exports the experiment to $AGI_RUN_HOME (*to implement*)

The script runs sync-experiment.sh, which relies on the ssh alias ec2-user to ssh into the desired ec2 instance. 
The instanceId of the same ec2 instance needs to be specified as a parameter when running the script
(there is a default value).
--> these must match  (to be improved in the future)
"""


def setup_aws(instanceId):
    print "....... starting ec2"
    ec2 = boto3.resource('ec2')
    instance = ec2.Instance(instanceId)
    response = instance.start()

    if log: print "LOG: Start response: ", response

    instance.wait_until_running()

    print "....... syncing files to ec2 container instance"
    subprocess.call(["../aws-ecs/ecs-sync-experiment.sh"])


def close_aws(instanceid):
    ec2 = boto3.resource('ec2')
    instance = ec2.Instance(instanceid)
    response = instance.stop()

    if log:
        print "LOG: stop ec2: ", response


# launch AGIEF on AWS
# hang till framwork is up and running
def launch_framework_aws(task_name, baseurl):
    print "....... launching framework on AWS"
    experiments_aws_setup(task_name)
    wait_framwork_up(baseurl)


# launch AGIEF on locally
# hang till framwork is up and running
def launch_framework_local(baseurl):
    print "....... launching framework locally"
    output = subprocess.Popen(["../node_coordinator/run.sh"],
                              stdin=subprocess.PIPE,
                              stdout=subprocess.PIPE,
                              stderr=subprocess.STDOUT)
    wait_framwork_up(baseurl)


def wait_framwork_up(baseurl):
    print "....... wait till framework has started"

    while True:
        try:
            response = requests.get(baseurl + '/entities')
            if log:
                print "LOG: response = ", response
            break
        except requests.ConnectionError:
            time.sleep(1)
            print "  - no connection yet ......"


def experiments_aws_setup(task_name):
    print "....... syncing files to ec2 container instance to " \
          "copy across the new parameter values in config due to sweep"
    subprocess.call(["../aws-ecs/ecs-sync-experiment.sh"])

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


# return when the the config parameter has achieved the value specified
# entity = name of entity, param_path = path to parameter, delimited by '.'
def wait_till_param(baseurl, entity_name, param_path, value):
    parameter = '';
    while parameter != value:
        response = requests.get(baseurl + '/config', params={'entity': entity_name})
        parameter = dpath.util.get(response.json(), "value." + param_path, '.')
        time.sleep(2)  # sleep for n seconds


def run_experiment(baseurl, entity_filepath, data_filepath):
    print "....... Run Experiment"

    # import experiment files (entities.json and data.json)
    payload = {'entity-file': entity_filepath, 'data-file': data_filepath}
    response = requests.get(baseurl + '/import', params=payload)
    if log:
        print "LOG: Import entity file, response = ", response
        print "LOG: url: ", response.url
        print "LOG: raw: ", response.raw
        print "LOG: json string: ", json.dump(response.json())

    # start the experiment
    payload = {'entity': 'experiment', 'event': 'update'}
    response = requests.get(baseurl + '/update', params=payload)
    if log:
        print "LOG: Start experiment, response = ", response

    # wait for the task to finish
    wait_till_param(baseurl, 'experiment', 'terminated', True)  # poll API for 'Terminated' config param

    # export experiment files
    payload = {'entity': 'experiment', type: 'entity'}
    response = requests.get(baseurl + '/export', params=payload)
    if log:
        print "LOG: Export entity file, response = ", response

    # write back to file
    entity_json = response.json()
    with open("output-" + entity_filepath, 'w') as data_entity_file:
        data_entity_file.write(json.dumps(entity_json))

    payload = {'entity': 'experiment', type: 'data'}
    response = requests.get(baseurl + '/export', params=payload)
    if log:
        print "LOG: Export data file, response = ", response

    # write back to file
    data_json = response.json()
    with open("output-" + data_filepath, 'w') as data_data_file:
        data_data_file.write(json.dumps(data_json))


def modify_parameters(entity_filepath, entity_name, param_path, val):
    print "Modify Parameters: ", entity_filepath, param_path, val

    # open the json
    with open(entity_filepath) as data_file:
        data = json.load(data_file)

    # get the first element in the array with dictionary field "entity-name" = entity_name
    entity = dict()
    for entity_i in data:
        if not entity_i["name"] == entity_name:
            continue
        entity = entity_i
        break

    # get the config field, and turn it into valid JSON
    configStr = entity["config"]
    configStr = configStr.replace("\\\"", "\"")
    config = json.loads(configStr)

    if log:
        print "LOG: config(t)   = ", config, '\n'

    dpath.util.set(config, param_path, val, '.')
    if log:
        print "LOG: config(t+1) = ", config, '\n'

    # put the escape characters back in the config str and write back to file
    configStr = json.dumps(config)
    configStr = configStr.replace("\"", "\\\"")
    entity["config"] = configStr

    # write back to file
    with open(entity_filepath, 'w') as data_file:
        data_file.write(json.dumps(data))


# run experiments defined in exps_file
def experiment_file_fullpath(filename, path_env):
    variables_file = os.getenv('VARIABLES_FILE', 'variables.sh')
    subprocess.call(["source ../" + variables_file], shell=True)

    cmd = "source ../" + variables_file + " && echo $" + path_env
    output, error = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE).communicate()

    path_from_env = output.strip()
    filepath = os.path.join(path_from_env, filename)
    return filepath


def run_experiments(baseurl, exps_file):
    with open(exps_file) as data_exps_file:
        data = json.load(data_exps_file)

    for experiments in data["experiments"]:
        import_files = experiments["import-files"]  # import files dictionary

        if log:
            print "LOG: Import Files Dictionary = "
            print "LOG: ", import_files

        # get experiment filenames, and expand to full path
        entity_file = import_files["file-entities"]
        data_file = import_files["file-data"]

        entity_filepath = experiment_file_fullpath(filename=entity_file, path_env='AGI_RUN_HOME')
        data_filepath = experiment_file_fullpath(filename=data_file, path_env='AGI_HOME')

        if log:
            print "LOG: Entity file full path = " + entity_filepath

        if not os.path.isfile(entity_filepath):
            print "ERROR: The entity file " + entity_file + ", at path " + entity_filepath + ", does not exist.\nCANNOT CONTINUE."
            exit()

        for param_sweep in experiments["parameter-sweeps"]:
            entity_name = param_sweep["entity-name"]
            param_path = param_sweep["parameter-path"]
            # exp_type = param_sweep["val-type"]
            val_begin = param_sweep["val-begin"]
            val_end = param_sweep["val-end"]
            val_inc = param_sweep["val-inc"]

            if log:
                print "LOG: Parameter Sweep Dictionary"
                print "LOG: ", param_sweep

            val = val_begin
            while val <= val_end:
                val += val_inc
                modify_parameters(entity_filepath, entity_name, param_path, val)
                run_experiment(baseurl, entity_filepath, data_filepath)


def getbaseurl(host, port):
    return 'http://' + host + ':' + port


if __name__ == '__main__':
    import argparse
    from argparse import RawTextHelpFormatter

    parser = argparse.ArgumentParser(description=help_generic, formatter_class=RawTextHelpFormatter)

    # launch framework only, or launch and run experiments
    parser.add_argument('--exps', dest='exps_file', required=False,
                        help='A file that defines the experiments to run (json format). '
                             'If not provided, just launch framework.')

    # how to reach the framework
    parser.add_argument('--host', dest='host', required=False,
                        help='Host where the framework will be running (default=%(default)s).')
    parser.add_argument('--port', dest='port', required=False,
                        help='Port where the framework will be running (default=%(default)s).')

    # run on aws, keep it running or shut it down afterwards
    parser.add_argument('--aws', dest='aws', action='store_true',
                        help='Use this flag to run on AWS. Then InstanceId and Task need to be specified')
    parser.add_argument('--aws_keep_running', dest='aws_keep_running', action='store_true',
                        help='Use this flag to run on AWS, and do NOT shut down the virtual machines and tasks.'
                             ' Then InstanceId and Task need to be specified')
    parser.add_argument('--instanceid', dest='instanceid', required=False,
                        help='Instance ID of the ec2 container instance (default=%(default)s).')
    parser.add_argument('--task_name', dest='task_name', required=False,
                        help='The name of the ecs task (default=%(default)s).')
    parser.add_argument('--logging', dest='logging', action='store_true', help='Turn logging on.')

    parser.set_defaults(logging=False)
    parser.set_defaults(aws=False)
    parser.set_defaults(aws_keep_running=False)
    parser.set_defaults(host="c.x.agi.io")
    parser.set_defaults(port="8491")
    parser.set_defaults(instanceid="i-06d6a791")
    parser.set_defaults(task_name="mnist-spatial-task:8")
    args = parser.parse_args()

    global log
    log = args.logging
    if log:
        print "LOG: Arguments: ", args

    baseurl = getbaseurl(args.host, args.port)

    if args.aws or args.aws_keep_running:
        if not args.instanceid and not args.task_name:
            print "ERROR: You must specify an EC2 Instance ID (--instanceid) " \
                  "and ECS Task Name (--task_name) to run on AWS."
            exit()

    # import pdb
    # pdb.set_trace()

    if args.aws or args.aws_keep_running:
        setup_aws(args.instanceid)
        launch_framework_aws(args.task_name, baseurl)
    else:
        launch_framework_local(baseurl)

    if args.exps_file:
        run_experiments(baseurl, args.exps_file)

    if args.aws and not args.aws_keep_running:
        close_aws(args.instanceid)
