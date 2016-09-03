import json
import os
import errno
import subprocess
import dpath.util
import requests
import time
import boto3

help_generic = """
run-framework.py allows you to run each step of the AGIEF (AGI Experimental Framework), locally and on AWS.
Each step can be toggled with a parameter prefixed with 'step'. See parameter list for description of parameters.
As with all scripts that are part of AGIEF, the environment variables in VARIABLES_FILES are used.
The main ones being $AGI_HOME (code) and $AGI_RUN_HOME (experiment definitions).

Note that script runs the experiment by updating the Experiment Entity until termination.
The script imports input files to set up the experiment, and exports experimental results for archive.

See README.md for installation instructions and longer explanation of the end-to-end AGIEF system.

Assumptions:
- Experiment entity exists, with 'terminated' field
- The VARIABLES_FILE is used for env variables
"""


# run the chosen instance specified by instanceId
def aws_run_ec2(instanceId):
    print "....... starting ec2"
    ec2 = boto3.resource('ec2')
    instance = ec2.Instance(instanceId)
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


def aws_close(instanceid):
    ec2 = boto3.resource('ec2')
    instance = ec2.Instance(instanceid)
    response = instance.stop()

    if log:
        print "LOG: stop ec2: ", response


# launch AGIEF on AWS
# hang till framework is up and running
def launch_framework_aws(task_name, baseurl):
    print "....... launching framework on AWS"
    aws_runtask(task_name)
    wait_framework_up(baseurl)


# launch AGIEF on locally
# hang till framework is up and running
def launch_framework_local(baseurl, main_class=""):
    print "....... launching framework locally"
    cmd = "../node_coordinator/run.sh "
    if main_class is not "":
        cmd = "../node_coordinator/run-demo.sh -m " + main_class
    subprocess.Popen(cmd,
                     shell=True,
                     stdout=subprocess.PIPE,
                     stderr=subprocess.STDOUT)
    wait_framework_up(baseurl)


def wait_framework_up(baseurl):
    print "....... wait till framework has started at = " + baseurl

    version = "** could not parse version number **"
    while True:
        try:
            response = requests.get(baseurl + '/version')
            if log:
                print "LOG: response = ", response

            responseJson = response.json()
            if 'version' in responseJson:
                version = responseJson['version']
            break
        except requests.ConnectionError:
            time.sleep(1)
            print "  - no connection yet ......"

    print "  - framework is up, running version: " + version


def terminate_framework():
    print "...... terminate framework"
    response = requests.get(baseurl + '/stop')

    if log:
        print "LOG: response text = ", response.text


# Run ecs task
def aws_runtask(task_name):

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


# Return when the the config parameter has achieved the value specified
# entity = name of entity, param_path = path to parameter, delimited by '.'
def agief_wait_till_param(baseurl, entity_name, param_path, value):
    while True:
        try:
            param_dic = {'entity': entity_name}
            r = requests.get(baseurl + '/config', params=param_dic)

            if log:
                print "LOG: /config with params " + json.dumps(param_dic) + ", response = ", r
                print "LOG: response text = ", r.text
                print "LOG: url: ", r.url

            if r.json()["value"] is not None:
                parameter = dpath.util.get(r.json(), "value." + param_path, '.')
                if parameter == value:
                    if log:
                        print "LOG: ... parameter: " + entity_name + "." + param_path + ", has achieved value: " + str(
                            value) + "."
                    break
        except requests.exceptions.ConnectionError:
            print "Oops, ConnectionError exception"
        except requests.exceptions.RequestException:
            print "Oops, request exception"

        if log:
            print "LOG: ... parameter: " + entity_name + "." + param_path + ", has not achieved value: " + str(
                value) + ",   wait 2s and try again ........"
        time.sleep(2)  # sleep for n seconds)


# setup the running instance of AGIEF with the input files
def agief_import(entity_filepath=None, data_filepath=None):
    with open(entity_filepath, 'rb') as entity_data_file:
        with open(data_filepath, 'rb') as data_data_file:
            files = {'entity-file': entity_data_file, 'data-file': data_data_file}
            response = requests.post(baseurl + '/import', files=files)
            if log:
                print "LOG: Import entity file, response = ", response
                print "LOG: response text = ", response.text
                print "LOG: url: ", response.url
                print "LOG: post body = ", files


def agief_run_experiment():
    payload = {'entity': 'experiment', 'event': 'update'}
    response = requests.get(baseurl + '/update', params=payload)
    if log:
        print "LOG: Start experiment, response = ", response

    # wait for the task to finish
    agief_wait_till_param(baseurl, 'experiment', 'terminated', True)  # poll API for 'Terminated' config param


# export_type can be 'entity' or 'data'
def create_folder(filepath):
    if not os.path.exists(os.path.dirname(filepath)):
        try:
            os.makedirs(os.path.dirname(filepath))
        except OSError as exc:  # Guard against race condition
            if exc.errno != errno.EEXIST:
                raise


def agief_export_rootentity(filepath, root_entity, export_type):
    payload = {'entity': root_entity, 'type': export_type}
    response = requests.get(baseurl + '/export', params=payload)
    if log:
        print "LOG: Export entity file, response text = ", response.text
        print "LOG: resonse url = ", response.url

    # write back to file
    output_json = response.json()
    create_folder(filepath)
    with open(filepath, 'w') as data_file:
        data_file.write(json.dumps(output_json, indent=4))


# Export the full experiment state from the running instance of AGIEF
# that consists of entity graph and the data
def agief_export_experiment(entity_filepath=None, data_filepath=None):
    agief_export_rootentity(entity_filepath, 'experiment', 'entity')
    agief_export_rootentity(data_filepath, 'experiment', 'data')


# Load AGIEF with the input files, then run the experiment
def exp_run(entity_filepath, data_filepath):
    print "....... Run Experiment"
    agief_import(entity_filepath, data_filepath)
    agief_run_experiment()


# Export the experiment
def exp_export(output_entity_filepath, output_data_filepath):
    print "....... Export Experiment"
    agief_export_experiment(output_entity_filepath, output_data_filepath)


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

    if not entity:
        print "ERROR: the experiment file (" + entity_filepath + ") did not contain matching entity name (" \
              + entity_name + ") and entity file name in field 'file-entities'."
        print "CANNOT CONTINUE"
        exit()

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

# return the full path to the inputfile specified by simple filename (AGI_RUN_HOME/input/filename)
def experiment_inputfile(filename):
    return filepath_from_env_variable("input/" + filename, "AGI_RUN_HOME")

# return the full path to the output file specified by simple filename (AGI_RUN_HOME/output/filename)
def experiment_outputfile(filename):
    return filepath_from_env_variable("output/" + filename, "AGI_RUN_HOME")


def filepath_from_env_variable(filename, path_env):
    variables_file = os.getenv('VARIABLES_FILE', 'variables.sh')
    subprocess.call(["source ../" + variables_file], shell=True)

    cmd = "source ../" + variables_file + " && echo $" + path_env
    output, error = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE).communicate()

    path_from_env = output.strip()
    filepath = os.path.join(path_from_env, filename)
    return filepath


def append_before_ext(filename, text):
    filesplit = os.path.splitext(filename)
    new_filename = filesplit[0] + "_" + text + filesplit[1]
    return new_filename


def run_experiments(exps_file):
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

        entity_filepath = experiment_inputfile(entity_file)
        data_filepath = experiment_inputfile(data_file)

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

                short_descr = param_path + "=" + str(val)

                new_entity_file = append_before_ext(entity_file, short_descr)
                output_entity_filepath = experiment_outputfile(new_entity_file)

                new_data_file = append_before_ext(data_file, short_descr)
                output_data_filepath = experiment_outputfile(new_data_file)

                exp_run(entity_filepath, data_filepath)
                exp_export(output_entity_filepath, output_data_filepath)


def getbaseurl(host, port):
    return 'http://' + host + ':' + port


def generate_input_files_locally():
    entity_filepath = experiment_inputfile("entity.json")
    data_filepath = experiment_inputfile("data.json")
    agief_export_experiment(entity_filepath, data_filepath)


# assumes there exists a private key for the given ec2 instance, at ~/.ssh/ecs-key
def aws_sync_experiment(host, keypath):
    print "....... syncing code to ec2 container instance"

    # code
    filepath = filepath_from_env_variable("", "AGI_HOME")
    cmd = "rsync -ave 'ssh -i " + keypath + "  -o \"StrictHostKeyChecking no\" ' " + filepath + " ec2-user@" + host + ":~/agief-project/agi --exclude={\"*.git/*\",*/src/*}"
    if log:
        print cmd
    output, error = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE).communicate()
    if log:
        print output
        print error

    # experiments
    filepath = filepath_from_env_variable("", "AGI_RUN_HOME")
    cmd = "rsync -ave 'ssh -i " + keypath + "  -o \"StrictHostKeyChecking no\" ' " + filepath + " ec2-user@" + host + ":~/agief-project/run --exclude={\"*.git/*\"}"
    if log:
        print cmd
    output, error = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE).communicate()
    if log:
        print output
        print error

if __name__ == '__main__':
    import argparse
    from argparse import RawTextHelpFormatter

    parser = argparse.ArgumentParser(description=help_generic, formatter_class=RawTextHelpFormatter)

    # generate input files from the java experiment description
    parser.add_argument('--step_gen_input', dest='main_class', required=False,
                        help='Generate input files for experiments, then exit. '
                             'The value is the Main class to run, that defines the experiment, '
                             'before exporting the experimental input files entities.json and data.json. ')

    # main program flow
    parser.add_argument('--step_aws', dest='aws', action='store_true',
                        help='Run AWS instances to run framework. Then InstanceId and Task need to be specified.')
    parser.add_argument('--step_exps', dest='exps_file', required=False,
                        help='Run experiments, defined in the file that is set with this parameter.'
                             'Filename is within AGI_RUN_HOME that defines the '
                             'experiments to run (with parameter sweeps) in json format (default=%(default)s).')
    parser.add_argument('--step_sync', dest='sync', action='store_true',
                        help='Sync the code and run folder (relevant for --step_aws).'
                             'Requires setting key path with --ec2_keypath')
    parser.add_argument('--step_agief', dest='launch_framework', action='store_true',
                        help='Launch the framework.')
    parser.add_argument('--step_shutdown', dest='shutdown', action='store_true',
                        help='Shutdown instances and framework after other stages.')

    # how to reach the framework
    parser.add_argument('--host', dest='host', required=False,
                        help='Host where the framework will be running (default=%(default)s). '
                             'THIS IS IGNORED IF RUNNING ON AWS (in which case the IP of the instance '
                             'specified by the Instance ID is used)')
    parser.add_argument('--port', dest='port', required=False,
                        help='Port where the framework will be running (default=%(default)s).')

    # aws details
    parser.add_argument('--instanceid', dest='instanceid', required=False,
                        help='Instance ID of the ec2 container instance (default=%(default)s).')
    parser.add_argument('--task_name', dest='task_name', required=False,
                        help='The name of the ecs task (default=%(default)s).')
    parser.add_argument('--ec2_keypath', dest='ec2_keypath', required=False,
                        help='Path to the private key for the ecs ec2 instance, '
                             'used for syncing over ssh (default=%(default)s).')
    parser.add_argument('--pg_instance', dest='pg_instance', required=False,
                        help='Instance ID of the Postgres ec2 instance (default=%(default)s). '
                             'If you want to use a running postgres instance, just specify the host (e.g. localhost). '
                             'WARNING: assumes that if the string starts with "i-", then it is an Instance ID')

    parser.add_argument('--logging', dest='logging', action='store_true', help='Turn logging on.')

    parser.set_defaults(host="localhost")
    parser.set_defaults(port="8491")
    parser.set_defaults(instanceid="i-057e0487")
    parser.set_defaults(pg_instance="i-b1d1bd33")
    parser.set_defaults(task_name="mnist-spatial-task:8")
    parser.set_defaults(ec2_keypath=filepath_from_env_variable(".ssh/ecs-key", "HOME"))

    args = parser.parse_args()

    global log
    log = args.logging
    if log:
        print "LOG: Arguments: ", args

    baseurl = getbaseurl(args.host, args.port)

    # 1) Generate input files
    if args.main_class:
        launch_framework_local(baseurl, args.main_class)
        generate_input_files_locally()
        exit()

    # 2) Setup Infrastructure (on AWS or nothing to do locally)
    ips = {'ip_public': args.host}
    if args.pg_instance[:2] is not 'i-':
        ips_pg = {'ip_private': args.pg_instance}

    if args.aws:
        if not args.instanceid and not args.task_name:
            print "ERROR: You must specify an EC2 Instance ID (--instanceid) " \
                  "and ECS Task Name (--task_name) to run on AWS."
            exit()

        ips = aws_run_ec2(args.instanceid)

        if args.pg_instance[:2] is 'i-':
            ips_pg = aws_run_ec2(args.pg_instance)

    baseurl = getbaseurl(ips['ip_public'], args.port)  # re-define baseurl with aws host if relevant

    # set the DB_HOST environment variable, which
    os.putenv("DB_HOST", ips_pg["ip_private"])

    # 3) Sync code and run-home
    if args.sync:
        if not args.aws:
            print "ERROR: Syncing is meaningless unless you're running aws (use param --step_aws)"
            exit()
        aws_sync_experiment(ips["ip_public"], args.ec2_keypath)

    # 4) Launch framework (on AWS or locally)
    if args.launch_framework:
        if args.aws:
            launch_framework_aws(args.task_name, baseurl)
        else:
            launch_framework_local(baseurl)

    # 5) run experiments
    if args.exps_file:
        if not args.launch_framework:
            print "WARNING: Running experiment is meaningless unless you're already running framework " \
                  "(use param --step_agief)"
        filepath = filepath_from_env_variable(args.exps_file, "AGI_RUN_HOME")
        run_experiments(filepath)

    # 6) Shutdown framework
    if args.shutdown:
        terminate_framework()

        # Shutdown Infrastructure
        if args.aws:
            aws_close(args.instanceid)

            if args.pg_instance[:2] is 'i-':
                aws_close(args.pg_instance)
