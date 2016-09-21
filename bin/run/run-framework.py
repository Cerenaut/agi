import json
import os
import subprocess
from enum import Enum

from agief_experiment import agief
from agief_experiment import aws
from agief_experiment import experiment
from agief_experiment import utils
from agief_experiment import valincrementer

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


def launch_framework_aws(task_name):
    """ Launch AGIEF on AWS. Hang till framework is up and running.

    :param task_name:
    :return:
    """

    print "....... launching framework on AWS"
    aws.run_task(task_name)
    fwk.wait_up()


def launch_framework_local(main_class=""):
    """ Launch AGIEF locally. Hang till framework is up and running.

    If main_class is specified, then use run-demo.sh,
    which builds entity graph and data from the relevant Demo project defined by the Main Class.
    WARNING: In this case, the properties file used is hardcoded to node.properties
    WARNING: and the prefix used is the global variable PREFIX

    :param main_class:
    :return:
    """

    print "....... launching framework locally"
    cmd = "../node_coordinator/run.sh "
    if main_class is not "":
        cmd = "../node_coordinator/run-demo.sh node.properties " + main_class + " " + TEMPLATE_PREFIX

    if log:
        print "Running: " + cmd

    subprocess.Popen(cmd,
                     shell=True,
                     stdout=subprocess.PIPE,
                     stderr=subprocess.STDOUT,
                     executable="/bin/bash")
    fwk.wait_up()


def run_exp(entity_file, data_file, param_description):
    """
    Import input files
    Run Experiment and Export experiment
    The input files specified by params ('entity_file' and 'data_file')
    have parameters modified, which are described in parameters 'param_description'

    :param entity_file:
    :param data_file:
    :param param_description:
    :return:
    """

    entity_file_path = exp.inputfile(entity_file)
    data_file_path = exp.inputfile(data_file)

    if log:
        print "LOG: Entity file full path = " + entity_file_path

    if not os.path.isfile(entity_file_path):
        print "ERROR: The entity file " + entity_file + ", at path " + entity_file_path + \
              ", does not exist.\nCANNOT CONTINUE."
        exit()

    print "....... Launch Framework"
    launch_framework()

    print "....... Import Experiment"
    fwk.import_experiment(entity_file_path, data_file_path)

    print "....... Run Experiment"
    fwk.run_experiment()

    print "....... Export Experiment"
    new_entity_file = utils.append_before_ext(entity_file, "___" + param_description)
    new_data_file = utils.append_before_ext(data_file, "___" + param_description)

    fwk.export_experiment(exp.entity_with_prefix("experiment"),
                          exp.outputfile(new_entity_file),
                          exp.outputfile(new_data_file))

    print "....... Terminate Framework"
    fwk.terminate()


def setup_parameter_sweep_counters(param_sweep, counters):
    """
    For each 'param' in a set, get details and setup counter
    The result is an array of counters
    Each counter represents one parameter

    :param param_sweep:
    :param counters:
    :return:
    """

    param_i = 0
    for param in param_sweep['parameter-set']:  # set of params for one 'sweep'

        if False:
            print "LOG: Parameter sweep set part: " + str(param_i)
            print json.dumps(param, indent=4)
        param_i += 1

        entity_name = param['entity-name']
        param_path = param['parameter-path']
        # exp_type = param['val-type']
        val_begin = param['val-begin']
        val_end = param['val-end']
        val_inc = param['val-inc']

        entity_name = exp.entity_with_prefix(entity_name)

        incrementer = valincrementer.ValIncrementer(val_begin, val_end, val_inc)

        counter = {'incrementer': incrementer, 'entity-name': entity_name, 'param-path': param_path}
        counters.append(counter)


def inc_parameter_set(entity_file, counters):
    """
    Iterate through counters, incrementing each parameter in the set
    Set the new values in the input file, and then run the experiment
    First counter to reset, return False

    :param entity_file:
    :param counters:
    :return: reset (True if any counter has reached above max), description of parameters (string)
    """

    # inc all counters, and set parameter in entity file
    param_description = None
    reset = False
    for counter in counters:
        incrementer = counter['incrementer']
        is_counting = incrementer.increment()
        if is_counting is False:
            if log:
                print "LOG: Sweeping has concluded for this sweep-set, due to the parameter: " + \
                      counter['entity-name'] + '.' + counter['param-path']
            reset = True
            break

        val = incrementer.value()
        entity_file_path = exp.inputfile(entity_file)
        fwk.set_parameter(entity_file_path, counter['entity-name'], counter['param-path'], val)

        delta = counter['entity-name'] + "." + counter['param-path'] + "=" + str(val)
        if param_description is None:
            param_description = delta
        else:
            param_description += "_" + delta

    if log:
        print "LOG: Parameter sweep of params/vals: " + param_description

    return reset, param_description


def run_sweeps(exps_file):
    """ Perform parameter sweep steps, and run experiment for each step.

    :param exps_file:
    :return:
    """

    with open(exps_file) as data_exps_file:
        data = json.load(data_exps_file)

    for exp_i in data['experiments']:
        import_files = exp_i['import-files']  # import files dictionary

        if log:
            print "LOG: Import Files Dictionary = "
            print "LOG: ", import_files

        # get experiment file-names, and expand to full path
        base_entity_filename = import_files['file-entities']
        base_data_filename = import_files['file-data']

        if 'parameter-sweeps' not in exp_i or len(exp_i['parameter-sweeps']) == 0:
            if log:
                print "No parameters to sweep, just run once."

            entity_filename, data_filename = exp.create_input_files(TEMPLATE_PREFIX, base_entity_filename, base_data_filename)
            run_exp(entity_filename, data_filename, "")
        else:
            for param_sweep in exp_i['parameter-sweeps']:  # array of sweep definitions

                entity_filename, data_filename = exp.create_input_files(TEMPLATE_PREFIX, base_entity_filename, base_data_filename)

                counters = []
                setup_parameter_sweep_counters(param_sweep, counters)

                is_sweeping = True
                while is_sweeping:
                    reset, param_description = inc_parameter_set(entity_filename, counters)
                    if reset:
                        is_sweeping = False
                    else:
                        run_exp(entity_filename, data_filename, param_description)


def generate_input_files_locally():
    entity_file_path = exp.inputfile("entity.json")
    data_file_path = exp.inputfile("data.json")

    root = exp.entity_with_prefix("experiment")
    fwk.export_experiment(root, entity_file_path, data_file_path)


def setup_arg_parsing():
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
    parser.set_defaults(ec2_keypath=utils.filepath_from_env_variable(".ssh/ecs-key", "HOME"))

    return parser.parse_args()


def launch_framework():
    """ Launch framework locally or on AWS. """

    if args.launch_framework:
        if is_aws:
            launch_framework_aws(args.task_name)
        else:
            launch_framework_local()


class LaunchMode(Enum):
    per_experiment = 1
    per_session = 2

if __name__ == '__main__':

    args = setup_arg_parsing()

    log = args.logging
    launch_mode = LaunchMode.per_experiment     # for now per experiment
    is_aws = args.aws
    TEMPLATE_PREFIX = "SPAGHETTI"
    PREFIX_DELIMITER = "^^"

    if log:
        print "LOG: Arguments: ", args

    exp = experiment.Experiment(TEMPLATE_PREFIX, PREFIX_DELIMITER)
    fwk = agief.AGIEF(log, None)

    # 1) Generate input files
    if args.main_class:
        fwk.base_url = utils.getbaseurl(args.host, args.port)
        launch_framework_local(args.main_class)
        generate_input_files_locally()
        fwk.terminate()
        exit()

    # 2) Setup infrastructure (on AWS or nothing to do locally)
    ips = {'ip_public': args.host, 'ip_private': None}
    ips_pg = {'ip_public': args.host, 'ip_private': None}
    if args.pg_instance[:2] is not 'i-':
        ips_pg = {'ip_private': args.pg_instance}

    if is_aws:
        if not args.instanceid and not args.task_name:
            print "ERROR: You must specify an EC2 Instance ID (--instanceid) " \
                  "and ECS Task Name (--task_name) to run on AWS."
            exit()

        ips = aws.run_ec2(args.instanceid)

        if args.pg_instance[:2] is 'i-':
            ips_pg = aws.run_ec2(args.pg_instance)

    fwk.base_url = utils.getbaseurl(ips['ip_public'], args.port)  # define base_url, with aws host if relevant

    # TEMPORARY HACK
    # Set the DB_HOST environment variable
    os.putenv("DB_HOST", ips_pg["ip_private"])

    # 3) Sync code and run-home
    if args.sync:
        if not args.aws:
            print "ERROR: Syncing is meaningless unless you're running aws (use param --step_aws)"
            exit()
        aws.sync_experiment(ips["ip_public"], args.ec2_keypath)

    # 4) Launch framework (on AWS or locally) - *** IF Mode == 'Per Session' ***
    if launch_mode is LaunchMode.per_session and args.launch_framework:
        launch_framework(args.aws)

    # 5) Run experiments
    if args.exps_file:
        if not args.launch_framework:
            print "WARNING: Running experiment is meaningless unless you're already running framework " \
                  "(use param --step_agief)"
        file_path = utils.filepath_from_env_variable(args.exps_file, "AGI_RUN_HOME")
        run_sweeps(file_path)

    # 6) Shutdown framework
    if args.shutdown:

        if launch_mode is LaunchMode.per_session:
            fwk.terminate()

        # Shutdown infrastructure
        if args.aws:
            aws.close(args.instanceid)

            if args.pg_instance[:2] is 'i-':
                aws.close(args.pg_instance)
