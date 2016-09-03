# End to end 
NOTE: all scripts utilise variables.sh 
You can modify that file, or create your own and set the env variable VARIABLES_FILE to it to user yours instead.

Most steps are carried out with ```/bin/run/run-framework.py```
Installation instructions below.

## Setup
- get the latest [code](https://github.com/ProjectAGI/agi) and [experiment definitions](https://github.com/ProjectAGI/experiment-definitions) from github
- setup python script (instructions in the next section)
- build code (```/bin/node_coordinator/build.sh```)
- ensure values in your ```VARIABLES_FILE``` are correct, in particular the location ofcode ```$AGI_HOME`` and the particular experiment ```$AGI_RUN_HOME```
Note: ```$AGI_HOME``` should refer to the location of you repo 'agi'
Note: ```$AGI_RUN_HOME``` should refer to the location of you repo 'experiment-definitions' and then the specific experiment folder within it (e.g. /classifier)

## Generate Input Files
```run-framework.py --step_gen_input NAME_OF_MAIN_CLASS```

This will place the input files in your experiment definitions folder, referred to by ```$AGI_RUN_HOME```

## Run the framework
Use ```run-framework.py```. All of the steps can be run separately, or all together, specified with command line switches. The experiments can be run locally or on AWS. 

The steps are:

- [aws] run the ec2 instances (ecs and postgres)
- launch framework
- import input files from run-folder
- [aws] sync code folder (compiled), run-folder to ecs (run-folder has node.properties, log4j xml etc.), and dataset
- run experiment
- optionally export artefacts
- change parameters and repeat run experiment
- shutdown framework
- [aws] shutdown ec2 instances

## Installation of run-framework.py
- Install Python and Pip
- Install dependencies
```pip install -r REQUIREMENTS.txt```
- Install and configure AWS-CLI (used by python script) [guide](http://docs.aws.amazon.com/cli/latest/userguide/installing.html)

# Examples
### aws esc and aws postgres 
```sh
python ~/Development/ProjectAGI/AGIEF/agi/bin/run/run-framework.py --logging --step_aws --step_exps experiments.json --step_sync --step_agief --step_shutdown --instanceid i-06d6a791 --port 8491 --pg_instance i-b1d1bd33 --task_name mnist-spatial-task:8 --ec2_keypath /Users/gideon/.ssh/nextpair.pem
```

### local agief and local postgres
```sh
python ~/Development/ProjectAGI/AGIEF/agi/bin/run/run-framework.py --logging --step_exps experiments.json --step_agief --step_shutdown --host localhost --pg_instance localhost --port 8491
```

### generate input files
```sh
python ~/Development/ProjectAGI/AGIEF/agi/bin/run/run-framework.py --step_gen_input io.agi.framework.demo.classifier.ClassifierDemo
```