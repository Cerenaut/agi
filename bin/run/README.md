## End to end 

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



## run-framework.py

### Installation
```sh
pip install -r REQUIREMENTS.txt
```


### Examples
