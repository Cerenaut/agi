import json
import os
import subprocess
from enum import Enum
import time

from agief_experiment import compute
from agief_experiment import cloud
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


def run_parameterset(entity_file, data_file, sweep_param_vals):
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

    print "........ Run parameter set."

    _experiment.info()

    print "\nSweep Parameters:"
    for param_def in sweep_param_vals:
        print param_def
    print "\n"

    entity_file_path = _experiment.inputfile(entity_file)
    data_file_path = _experiment.inputfile(data_file)

    if log:
        print "LOG: Entity file full path = " + entity_file_path

    if not os.path.isfile(entity_file_path):
        print "ERROR: The entity file " + entity_file + ", at path " + entity_file_path + \
              ", does not exist.\nCANNOT CONTINUE."
        exit()

    if (launch_mode is LaunchMode.per_experiment) and args.launch_compute:
        task_arn = launch_compute()

    _compute_node.import_experiment(entity_file_path, data_file_path)

    set_dataset(_experiment.experiment_def_file())

    _compute_node.run_experiment(_experiment)

    if is_export:
        new_entity_file = "exported_" + entity_file  # utils.append_before_ext(entity_file, "___" + param_description)
        new_data_file = "exported_" + data_file  # utils.append_before_ext(data_file, "___" + param_description)

        out_entity_file_path = _experiment.outputfile(new_entity_file)
        out_data_file_path = _experiment.outputfile(new_data_file)

        _compute_node.export_experiment(_experiment.entity_with_prefix("experiment"),
                                        out_entity_file_path,
                                        out_data_file_path)

    if (launch_mode is LaunchMode.per_experiment) and args.launch_compute:
        shutdown_compute(task_arn)

    if is_upload:
        # upload exported output Entity file (if it exists)
        _cloud.upload_experiment_output_s3(_experiment.prefix,
                                           new_entity_file,
                                           out_entity_file_path)

        # upload exported output Data file (if it exists)
        _cloud.upload_experiment_output_s3(_experiment.prefix,
                                           new_data_file,
                                           out_data_file_path)

        # upload experiments definition file (if it exists)
        _cloud.upload_experiment_output_s3(_experiment.prefix,
                                           _experiment.experiments_def_filename,
                                           _experiment.experiment_def_file())

        # upload log4j configuration file that was used (if it exists)
        log_filename = "log4j2.log"
        log_filepath = _experiment.experimentfile(log_filename)
        _cloud.upload_experiment_output_s3(_experiment.prefix,
                                           log_filename,
                                           log_filepath)


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
                            If reset is False, there MUST be a description of the parameters that have been set
    """

    # inc all counters, and set parameter in entity file
    sweep_param_vals = []
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
        entity_file_path = _experiment.inputfile(entity_file)
        set_param = _compute_node.set_parameter_inputfile(entity_file_path,
                                                          _experiment.entity_with_prefix(counter['entity-name']),
                                                          counter['param-path'],
                                                          val)
        sweep_param_vals.append(set_param)

    if len(sweep_param_vals) == 0:
        print "WARNING: no parameters were changed."

    if log:
        if len(sweep_param_vals):
            print "LOG: Parameter sweep: ", sweep_param_vals

    if reset is False and len(sweep_param_vals) == 0:
        print "Error: inc_parameter_set() indeterminate state, reset is False, but parameter_description indicates " \
              "no parameters have been modified. If there is no sweep to conduct, reset should be True."
        exit()

    return reset, sweep_param_vals


def run_sweeps():
    """ Perform parameter sweep steps, and run experiment for each step.

    :param exps_file: full path to experiments file
    :return:
    """

    print "........ Run Sweeps"

    exps_file = _experiment.experiment_def_file()

    with open(exps_file) as data_exps_file:
        data = json.load(data_exps_file)

    for exp_i in data['experiments']:
        import_files = exp_i['import-files']  # import files dictionary

        if log:
            print "LOG: Import Files Dictionary = "
            print "LOG: ", json.dumps(import_files, indent=4)

        # get experiment file-names, and expand to full path
        base_entity_filename = import_files['file-entities']
        base_data_filename = import_files['file-data']

        if 'parameter-sweeps' not in exp_i or len(exp_i['parameter-sweeps']) == 0:
            print "No parameters to sweep, just run once."

            entity_filename, data_filename = _experiment.create_input_files(TEMPLATE_PREFIX,
                                                                            base_entity_filename,
                                                                            base_data_filename)
            run_parameterset(entity_filename, data_filename, "")
        else:
            for param_sweep in exp_i['parameter-sweeps']:  # array of sweep definitions

                counters = []
                setup_parameter_sweep_counters(param_sweep, counters)

                is_sweeping = True
                while is_sweeping:

                    entity_filename, data_filename = _experiment.create_input_files(TEMPLATE_PREFIX,
                                                                                    base_entity_filename,
                                                                                    base_data_filename)

                    reset, sweep_param_vals = inc_parameter_set(entity_filename, counters)
                    if reset:
                        is_sweeping = False
                    else:
                        run_parameterset(entity_filename, data_filename, sweep_param_vals)


def set_dataset(exps_file):
    """
    The dataset can be located in different locations on different machines. The location can be set in the
    experiments definition file (experiments.json). This method parses that file, finds the parameters to set
    relative to the AGI_DATA_HOME env variable, and sets the specified parameters.
    :param exps_file:
    :return:
    """

    print "....... Set Dataset"

    with open(exps_file) as data_exps_file:
        data = json.load(data_exps_file)

    for exp_i in data['experiments']:
        for param in exp_i['dataset-parameters']:  # array of sweep definitions
            entity_name = param['entity-name']
            param_path = param['parameter-path']
            data_filename = param['value']

            data_path = _experiment.datafile(data_filename)
            _compute_node.set_parameter_db(_experiment.entity_with_prefix(entity_name), param_path, data_path)


def launch_compute_aws_ecs(task_name):
    """
    Launch Compute on AWS ECS (elastic container service).
    Assumes that ECS is setup to have the necessary task, and container instances running.
    Hang till Compute is up and running. Return task arn.

    :param task_name:
    :return:
    """

    print "launching Compute on AWS-ECS"

    if task_name is None:
        print "ERROR: you must specify a Task Name to run on aws-ecs"
        exit()

    task_arn = _cloud.run_task_ecs(task_name)
    _compute_node.wait_up()
    return task_arn


def launch_compute_remote_docker():
    """
    Launch Compute Node on AWS. Assumes there is a running ec2 instance running Docker
    Hang till Compute is up and running.
    """

    print "launching Compute on AWS (on ec2 using run-in-docker.sh)"
    _cloud.launch_compute_docker(_compute_node.host, remote_keypath)
    _compute_node.wait_up()


def launch_compute_local(main_class=""):
    """ Launch Compute locally. Hang till Compute is up and running.

    If main_class is specified, then use run-demo.sh,
    which builds entity graph and data from the relevant Demo project defined by the Main Class.
    WARNING: In this case, the properties file used is hardcoded to node.properties
    WARNING: and the prefix used is the global variable PREFIX

    :param main_class:
    :return:
    """

    print "launching Compute locally"
    cmd = "../node_coordinator/run.sh "
    if main_class is not "":
        cmd = "../node_coordinator/run-demo.sh node.properties " + main_class + " " + TEMPLATE_PREFIX

    if log:
        print "Running: " + cmd

    cmd += " > run_stdout.log 2> run_stderr.log "

    # we can't hold on to the stdout and stderr streams for logging, because it will hang on this line
    # instead, log to a file
    subprocess.Popen(cmd,
                     shell=True,
                     executable="/bin/bash")

    _compute_node.wait_up()


def launch_compute(use_ecs=False):
    """ Launch Compute locally or on AWS. Return task arn if on AWS """

    print "....... Launch Compute"

    task_arn = None

    if is_aws:
        if use_ecs:
            task_arn = launch_compute_aws_ecs(args.task_name)
        else:
            launch_compute_remote_docker()
    else:
        launch_compute_local()

    version = _compute_node.version()
    print "Running Compute version: " + version

    return task_arn


def shutdown_compute(task_arn):
    """ Close compute: terminate and then if running on AWS, stop the task. """

    print "....... Shutdown System"

    _compute_node.terminate()

    # note that task may be set up to terminate once compute has been terminated
    if is_aws and (task_arn is not None):
        _cloud.stop_task_ecs(task_arn)


def generate_input_files_locally():
    entity_file_path = _experiment.inputfile("entity.json")
    data_file_path = _experiment.inputfile("data.json")

    root = _experiment.entity_with_prefix("experiment")
    _compute_node.export_experiment(root, entity_file_path, data_file_path)


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
                        help='Run AWS instances to run Compute. Then InstanceId and Task need to be specified.')
    parser.add_argument('--step_exps', dest='exps_file', required=False,
                        help='Run experiments, defined in the file that is set with this parameter.'
                             'Filename is within AGI_RUN_HOME that defines the '
                             'experiments to run (with parameter sweeps) in json format (default=%(default)s).')
    parser.add_argument('--step_sync', dest='sync', action='store_true',
                        help='Sync the code and run folder (relevant for --step_aws).'
                             'Requires setting key path with --ec2_keypath')
    parser.add_argument('--step_compute', dest='launch_compute', action='store_true',
                        help='Launch the Compute node.')
    parser.add_argument('--step_shutdown', dest='shutdown', action='store_true',
                        help='Shutdown instances and Compute after other stages.')
    parser.add_argument('--step_export', dest='export', action='store_true',
                        help='Export entity tree and data at the end of each experiment.')
    parser.add_argument('--step_upload', dest='upload', action='store_true',
                        help='Upload exported entity tree and data at the end of each experiment.')

    # how to reach the Compute
    parser.add_argument('--host', dest='host', required=False,
                        help='Host where the Compute node will be running (default=%(default)s). '
                             'THIS IS IGNORED IF RUNNING ON AWS (in which case the IP of the instance '
                             'specified by the Instance ID is used)')
    parser.add_argument('--port', dest='port', required=False,
                        help='Port where the Compute node will be running (default=%(default)s).')

    # launch mode
    parser.add_argument('--launch_per_session', dest='launch_per_session', action='store_true',
                        help='Compute node is launched once at the start (and shutdown at the end if you use '
                             '--step_shutdown. Otherwise, it is launched and shut per experiment.')

    # aws details
    parser.add_argument('--instanceid', dest='instanceid', required=False,
                        help='Instance ID of the ec2 container instance - to start an ec2 instance, use this OR ami id,'
                             ' not both.')
    parser.add_argument('--amiid', dest='amiid', required=False,
                        help='AMI ID for new ec2 instances - to start an ec2 instance, use this OR instance id, not'
                             ' both.')
    parser.add_argument('--ami_ram', dest='ami_ram', required=False,
                        help='If launching ec2 via AMI, use this to specify how much minimum RAM you want '
                             '(default=%(default)s).')
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
    parser.set_defaults(pg_instance="localhost")
    parser.set_defaults(task_name="mnist-spatial-task:10")
    parser.set_defaults(ec2_keypath=utils.filepath_from_env_variable(".ssh/ecs-key.pem", "HOME"))
    parser.set_defaults(ami_ram='6')

    return parser.parse_args()


class LaunchMode(Enum):
    per_experiment = 1
    per_session = 2


if __name__ == '__main__':

    print "------------------------------------------"
    print "----          run-framework           ----"
    print "------------------------------------------"

    TEMPLATE_PREFIX = "SPAGHETTI"
    PREFIX_DELIMITER = "--"

    args = setup_arg_parsing()
    log = args.logging
    if log:
        print "LOG: Arguments: ", args

    is_aws = args.aws
    is_export = args.export
    is_upload = args.upload
    remote_keypath = args.ec2_keypath

    if is_upload and not is_export:
        print "WARNING: Uploading experiment to S3 is enabled, but 'export experiment' is not, so the most " \
              "important files (output entity.json and data.json) will be missing"

    if args.launch_per_session:
        launch_mode = LaunchMode.per_session
    else:
        launch_mode = LaunchMode.per_experiment

    if args.amiid and args.instanceid:
        print "ERROR: Both the AMI ID and EC2 Instance ID have been specified. Use just one to specify how to get " \
              "a running ec2 instance"
        if args.aws:
            print "--- in any case, aws has not been set, so they have no effect"
        exit()

    _experiment = experiment.Experiment(log, TEMPLATE_PREFIX, PREFIX_DELIMITER)
    _compute_node = compute.Compute(log)
    _cloud = cloud.Cloud(log)

    # 1) Generate input files
    if args.main_class:
        _compute_node.base_url = utils.getbaseurl(args.host, args.port)
        launch_compute_local(args.main_class)
        generate_input_files_locally()
        _compute_node.terminate()
        exit()

    # 2) Setup infrastructure (on AWS or nothing to do locally)
    ips = {'ip_public': args.host, 'ip_private': None}
    ips_pg = {'ip_public': None, 'ip_private': None}

    if args.pg_instance:
        is_pg_ec2 = (args.pg_instance[:2] == 'i-')
    if is_aws:

        # start Compute ec2 either from instanceid or amiid
        if args.instanceid:
            ips = _cloud.run_ec2(args.instanceid)
        else:
            ips = _cloud.launch_from_ami_ec2('run-fwk auto', args.amiid, int(args.ami_ram))

        # start DB ec2, from instanceid
        if args.pg_instance and is_pg_ec2:
            ips_pg = _cloud.run_ec2(args.pg_instance)
        else:
            ips_pg = {'ip_private': args.pg_instance}

    elif args.pg_instance:
        if is_pg_ec2:
            print "ERROR: the pg instance is set to an ec2 instance id, but you are not running AWS."
            exit()

        ips_pg = {'ip_public': args.pg_instance, 'ip_private': args.pg_instance}

    _compute_node.host = ips['ip_public']
    _compute_node.port = args.port

    # TEMPORARY HACK
    # Set the DB_HOST environment variable
    if args.pg_instance:
        os.putenv("DB_HOST", ips_pg['ip_private'])

    # if we just started an ec2 instance, and there are any further steps, wait 30 seconds
    if args.aws and (args.instanceid or is_pg_ec2 or args.amiid) and (
            args.sync or args.exp_file or args.launch_compute):
        wait_for_ec2_delay = 90  # seconds
        # TODO: better solution would be to try to connect for a number of times and catch the exceptions
        print "WAIT " + str(wait_for_ec2_delay) + " seconds to ensure enough time for services such as SSH to start."
        time.sleep(wait_for_ec2_delay)

    # 3) Sync code and run-home
    if args.sync:
        if not args.aws:
            print "ERROR: Syncing is meaningless unless you're running aws (use param --step_aws)"
            exit()
        _cloud.sync_experiment(_compute_node.host, remote_keypath)

    # 4) Launch Compute (on AWS or locally) - *** IF Mode == 'Per Session' ***
    if (launch_mode is LaunchMode.per_session) and args.launch_compute:
        launch_compute()

    # 5) Run experiments
    if args.exps_file:
        _experiment.experiments_def_filename = args.exps_file
        if not args.launch_compute:
            print "WARNING: Running experiment is meaningless unless you're already running the Compute node" \
                  "(use param --step_compute)"

        run_sweeps()

    # 6) Shutdown framework
    if args.shutdown:

        if launch_mode is LaunchMode.per_session:
            _compute_node.terminate()

        # Shutdown infrastructure
        if is_aws:
            _cloud.stop_ec2(args.instanceid)

            if is_pg_ec2:
                _cloud.stop_ec2(args.pg_instance)
