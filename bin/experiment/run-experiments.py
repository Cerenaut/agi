import json
import boto3

def setup_aws():
    print "     start ec2"
    instanceId = "i-06d6a791"                                 # <----- global constant
    ec2 = boto3.resource('ec2')
    instance = ec2.Instance(instanceId)
    response = instance.start()

    print "Start respones: ", response

    instance.wait_until_running()


def close_aws():
    instanceId = "i-06d6a791"                                 # <----- global constant  
    ec2 = boto3.resource('ec2')
    instance = ec2.Instance(instanceId)
    response = instance.stop()


def run_experiment():
    print "Run Experiment"

    # sync files to S3 and from S3 to the ec2 instance
        # run the shell script: "ecs-sync-experiment.sh"

    # run task on ecs
    client = boto3.client('ecs')
    response = client.run_task(
        cluster='default',
        taskDefinition='mnist-spatial-task:3',               # <----- global constant
        count=1,
        startedBy='pyScript'
    )


    # wait for the task to finish

    # do this by polling an API or one of the config params             <-------------  ********* To Be Implemented on the Java side **********


def modify_param(file_entities, param_path, val):
    print "Modify Param: ", file_entities, param_path, val

    # open the json
    
    # modify the param
    
    # write to json



if __name__ == '__main__':
    import argparse
    parser = argparse.ArgumentParser(
	description='Run an AGIEF experiment, potentially with parameter sweep.')
    parser.add_argument('--inputFile', dest='expFile', required=True,
            help='filename, in json format, that contains a list of params to sweep, with a range.')
    args = parser.parse_args()

    global log 
    log = False
    if log : print "Arguments: ", args
    
    if args.expFile:


        setup_aws()

        with open(args.expFile) as data_file:    
            data = json.load(data_file)

        for experiments in data["experiments"]:
            import_files = experiments["import-files"]                         # import files dictionary 

            if log:
                print "Import Files Dictionary = "
                print import_files

            file_entities = import_files["file-entities"]

            for param_sweep in experiments["parameter-sweeps"]:
                param_path = param_sweep["parameter-path"]
                exp_type   = param_sweep["val-type"]
                val_begin  = param_sweep["val-begin"]
                val_end    = param_sweep["val-end"]
                val_inc    = param_sweep["val-inc"]

                if log:
                    print "Parameter Sweep Dictionary"
                    print param_sweep

                for val in xrange(val_begin, val_end, val_inc):
                    modify_param(file_entities, param_path, val)
                    run_experiment()
                    break
                break
            break

        close_aws()