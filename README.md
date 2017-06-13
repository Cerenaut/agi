# Artificial General Intelligence Experimental Framework

AGIEF makes it easy to set up repeatable and logged experiments. It consists of a simple graphical UI, an Interprocess layer for distributed coordination and communication, and base classes for the entities that you need for building an AGI experiment. The system architecture is shown [here](https://github.com/ProjectAGI/agi/blob/master/doc/AGIEF%20Experimental%20System.pdf). 


This repo consists of:
- core algorithmic components ```/code/core/src/io/agi/core```
- compute node previously referred to coordinator_node (written in Java) including interprocess communication, persistence layer a set of entities and a set of defined experiments ```/code/core/src/io/agi/framework```
- web based graphical UI ```/code/www```
- associated scripts ```/bin```

Compute has a RESTful API, so it is possible to implement components in other languages, or write alternative visualisations.
**The API is documented at ```/doc/API/http.swagger.yaml```**

* TODO: Some info about architecture, the fact that there is a db (currently in-memory), core, and GUI
* The db is a big json dictionary.
* All data persisted, therefore stateless and reproducable, repeatable, visualisable
* GUI gives tools to interact and visualise

* Entity is the basic structure, you create them, they get added to tree
* System update is on an entity, and it propagates
* parallel execution (siblings), and consecutive (children)
* standard setup is to have Experiment at the top


# Getting Started

The repository contains a bunch of scripts to help with installation, setup and running. 

NOTE: There is a ```run-in-docker.sh``` script that allows you to build and run compute in a docker container, which means you won't need to do any setup on your own computer.

All scripts utilise environmental variables defined in a 'variables' file. Every script begins by sourcing this file. ```/resources/variables-template.sh``` is an example with explanations of each variable. You can modify that file, or create your own instead. 
**IMPORTANT:** Then set the ENV variable ```VARIABLES_FILE``` to it using the full path.

That is necessary even if you are using the ```run-in-docker.sh``` script.

## Installation
Installation of the following tools is required and some background knowledge recommended:
* [Maven](https://maven.apache.org/) 

Then:
* Pull the repository
* Set variables. Duplicate ```/resources/variables-template.sh```, and overwrite with values suitable for your environment. Copy it to a convenient location and set an environmental variable VARIABLES_FILE to point to it using the full path. We recommend you set that up in .bashrc so that it is always definted correctly.
* The favoured (and our current) approach is to use 'in memory' persistence, specified in ```node.properties``` in the working folder. However, postgres is an option. If using postgres, setup and run the db. Run ```/bin/db/setup.sh```


## Running Basic
* The folder that you are running from must contain the file ```node.properties``` and a log4j configuration file. A working template is given in ```/resources/run-empty```.
* node.properties allows you to set the db mode between 'jdbc' and 'node'. The former is postgres, the latter is 'in-memory'.
* You can build and run the compute node using the scripts in `/bin/node_coordinator`. There is also the option of doing this in a docker container using `/bin/run-in-docker.sh`, read the help to see how to use it.
	* `run.sh` will run the generic main, whereas `run-demo.sh` is used to run one of the specific demos, each one has it's own main(). The latter is done to export the entities and data to be imported for running the experiment.
* You can use command line parameters to set node properties, and the initial state of system (entities, data)
* Run GUI by running the web server `/bin/www/python_server.sh` and going to [http://localhost:8000](http://localhost:8000)
	* alternatively, open any of the web pages in `/code/wwww`
* Choose the `main()` method to run to determine how you want to run the framework. 
	* Pre-defined experiment: There are experiments defined in code. By convention they are named `[demo-name]Demo`. They are contained in the package `io.agi.framework.demo`. 
	* Compute Node: The `Main` class in `io.agi.framework` launches the framework as a server, or 'Compute Node'. You can then load and export experiments via the HTTP API.


## Running an experiment on Compute node.
There are multiple options, and the repository `run-framework` is a python project to do a lot of the heavy lifting for you. See repo README. 
Also there is an `experiment-definitions` repo with assets required for past and current experiments.

The basic steps are to:
* Run the compute node (there is a template for the run-folder in `/resourses/run-empty` and a template variables file `/resources/variables-template.sh`, with the necessary assets to run the system)
* Ensure that there is an experiment loaded - entities and data (see below for details)
* Start the root entity (via web GUI or directly via http API `/update` call)

You run the compute node by either:
* Running generic main in `io.agi.framework.Main`, which does not load any entities or data, then load entities and data
* Running the main of an experiment, see `io.agi.framework.demo` package for examples. 
* Or use the scripts `/bin/node_coordinator`

You load data and entities by either:
* passing entities and data json files as command line parameters
* via www GUI
* directly via http API

In summary, pull the repo, build it with `mvn package`, pull `experiment-definitions`, set up your environment variables by copying one of the variables.sh files in experiment-definitions and setting it as `$VARIABLES_FILE` on your machine, and then run it all through `run-framework.py` .



## Resources
Have a look in the ```/resources``` folder for useful .... resources!
There is a code formatting style file, log4j configuration file template, an empty run-folder with necessary assets for the working directory and a template for the variables.sh file.