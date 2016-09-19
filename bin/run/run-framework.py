import json
import subprocess
import os

import agief
import experiment
import utils
import aws

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


# launch AGIEF on AWS
# hang till framework is up and running
def launch_framework_aws(task_name):
    print "....... launching framework on AWS"
    aws.run_task(task_name)
    agief.wait_up()


# launch AGIEF on locally
# hang till framework is up and running
def launch_framework_local(main_class=""):
    print "....... launching framework locally"
    cmd = "../node_coordinator/run.sh "
    if main_class is not "":
        cmd = "../node_coordinator/run-demo.sh " + main_class

    if log:
        print "Running: " + cmd

    subprocess.Popen(cmd,
                     shell=True,
                     stdout=subprocess.PIPE,
                     stderr=subprocess.STDOUT,
                     executable="/bin/bash")
    agief.wait_up()


# Perform parameter sweeps, and run experiment
def run_sweeps(exps_file, experiment):

    import ValIncrementer

    with open(exps_file) as data_exps_file:
        data = json.load(data_exps_file)

    for experiments in data['experiments']:
        import_files = experiments['import-files']  # import files dictionary

        if log:
            print "LOG: Import Files Dictionary = "
            print "LOG: ", import_files

        # get experiment filenames, and expand to full path
        entity_file = import_files['file-entities']
        data_file = import_files['file-data']

        entity_filepath = experiment.inputfile(entity_file)
        data_filepath = experiment.inputfile(data_file)

        if log:
            print "LOG: Entity file full path = " + entity_filepath

        if not os.path.isfile(entity_filepath):
            print "ERROR: The entity file " + entity_file + ", at path " + entity_filepath + ", does not exist.\nCANNOT CONTINUE."
            exit()

        if 'parameter-sweeps' not in experiments or len(experiments['parameter-sweeps']) == 0:
            if log:
                print "No parameters to sweep, just run once."

            print "....... Run Experiment"
            agief.import_experiment(entity_filepath, data_filepath)
            agief.run_experiment()

            output_entity_filepath = experiment.outputfile(entity_file)
            output_data_filepath = experiment.outputfile(data_file)
            agief.exp_export(experiment.entity_with_prefix("experiment"),
                             output_entity_filepath,
                             output_data_filepath)
        else:
            param_sweep_i = 0
            for param_sweep in experiments['parameter-sweeps']:         # array of sweep definitions

                if False:
                    print "LOG: Parameter sweep part: " + str(param_sweep_i)
                    print "LOG: ", json.dumps(param_sweep, indent=4)
                param_sweep_i += 1

                # Sweep Param Set
                # -------------------
                # For each 'param', get details and setup counter
                # Iterate through counters, incrementing each then running experiment
                # First counter to reset, exits loop

                counters = []
                param_i = 0
                for param in param_sweep['parameter-set']:          # set of params for one 'sweep'

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

                    entity_name = experiment.entity_with_prefix(entity_name)

                    incrementer = ValIncrementer(val_begin, val_end, val_inc)

                    counter = {'incrementer': incrementer, 'entity-name': entity_name, 'param-path': param_path}
                    counters.append(counter)

                sweeping = True
                while sweeping:
                    # inc all counters, and set parameter in entity file
                    short_descr = ""
                    for counter in counters:
                        incrementer = counter['incrementer']
                        is_counting = incrementer.increment()
                        if is_counting is False:
                            if log: print "LOG: Sweeping has concluded for this sweep-set, due to the parameter: " + counter['entity-name'] + '.' + counter['param-path']
                            sweeping = False
                            break

                        val = incrementer.value()
                        agief.set_parameter(entity_filepath, counter['entity-name'], counter['param-path'], val)

                        short_descr += "_" + counter['entity-name'] + "." + counter['param-path'] + "=" + str(val)

                    if sweeping is False:
                        break

                    if log: print "LOG: Parameter sweep of params/vals: " + short_descr

                    # run experiment
                    new_entity_file = utils.append_before_ext(entity_file, short_descr)
                    new_data_file = utils.append_before_ext(data_file, short_descr)

                    output_entity_filepath = experiment.outputfile(new_entity_file)
                    output_data_filepath = experiment.outputfile(new_data_file)

                    if log:
                        print "LOG: Sweep set output entity file: " + output_entity_filepath

                    print "....... Run Experiment"
                    agief.import_experiment(entity_filepath, data_filepath)
                    agief.run_experiment()

                    print "....... Export Experiment"
                    agief.export_experiment(experiment.entity_with_prefix("experiment"),
                                            output_entity_filepath,
                                            output_data_filepath)


def generate_input_files_locally():
    entity_filepath = experiment.inputfile("entity.json")
    data_filepath = experiment.inputfile("data.json")

    root = experiment.entity_with_prefix("experiment")
    agief.export_experiment(root, entity_filepath, data_filepath)


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


if __name__ == '__main__':

    args = setup_arg_parsing()

    log = args.logging
    global log

    if log:
        print "LOG: Arguments: ", args

    exp = experiment.Experiment()
    agief = agief.AGIEF(log, None)

    # 1) Generate input files
    if args.main_class:
        agief.base_url = utils.getbaseurl(args.host, args.port)
        launch_framework_local(args.main_class)
        generate_input_files_locally()
        agief.terminate()
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

        ips = aws.run_ec2(args.instanceid)

        if args.pg_instance[:2] is 'i-':
            ips_pg = aws.run_ec2(args.pg_instance)

    agief.base_url = utils.getbaseurl(ips['ip_public'], args.port)  # define base_url, with aws host if relevant


    # TEMPORARY HACK
    # set the DB_HOST environment variable
    os.putenv("DB_HOST", ips_pg["ip_private"])

    # 3) Sync code and run-home
    if args.sync:
        if not args.aws:
            print "ERROR: Syncing is meaningless unless you're running aws (use param --step_aws)"
            exit()
        aws.sync_experiment(ips["ip_public"], args.ec2_keypath)

    # 4) Launch framework (on AWS or locally)
    if args.launch_framework:
        if args.aws:
            launch_framework_aws(args.task_name)
        else:
            launch_framework_local()

    # 5) Run experiments
    if args.exps_file:
        if not args.launch_framework:
            print "WARNING: Running experiment is meaningless unless you're already running framework " \
                  "(use param --step_agief)"
        file_path = utils.filepath_from_env_variable(args.exps_file, "AGI_RUN_HOME")
        run_sweeps(file_path, exp)

    # 6) Shutdown framework
    if args.shutdown:
        agief.terminate()

        # Shutdown Infrastructure
        if args.aws:
            aws.close(args.instanceid)

            if args.pg_instance[:2] is 'i-':
                aws.close(args.pg_instance)
