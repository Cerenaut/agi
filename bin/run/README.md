############################################################
### End to end 
############################################################

- pull code (git pull)
- build code (mvn package)
- [aws] run the ec2 instances (ecs and postgres)
- launch framework
- import input files from run-folder
- [aws] sync code folder (compiled), run-folder to ecs (run-folder has node.properties, log4j xml etc.), and dataset
- run experiment
- optionally export artefacts
- change parameters and repeat run experiment
- shutdown framework
- [aws] shutdown ec2 instances

Steps after 'build' can be performed with run-framework.py
Each step can be performed individually.



############################################################
### Installation of run-framework.py
############################################################

## run-framework.py

### Installation
```sh
pip install -r REQUIREMENTS.txt
```


############################################################
### Examples
############################################################

### aws esc and aws postgres 

python ~/Development/ProjectAGI/AGIEF/agi/bin/run/run-framework.py --logging --step_aws --step_exps experiments.json --step_sync --step_agief --step_shutdown --instanceid i-06d6a791 --port 8491 --pg_instance i-b1d1bd33 --task_name mnist-spatial-task:8 --ec2_keypath /Users/gideon/.ssh/nextpair.pem


### local agief and local postgres

python ~/Development/ProjectAGI/AGIEF/agi/bin/run/run-framework.py --logging --step_exps experiments.json --step_agief --step_shutdown --host localhost --pg_instance localhost --port 8491


### generate input files

--step_gen_input io.agi.framework.demo.classifier.ClassifierDemo