... note: this must be consolidated with the material on the wiki ..... 


# Artificial General Intelligence Experimental Framework

AGIEF makes it easy to set up repeatable and logged experiments. It consists of a simple graphical UI, an Interprocess layer for distributed coordination and communication, and a base classes for the entities that you need for building an AGI experiment. The system architecture is shown [here]("https://github.com/ProjectAGI/agi/blob/master/resources/docs/Project AGI_ AGI Experimental Framework_ A platform for AGI R&D.pdf"). You can implement your own Agents, Worlds, Sensors and Actuators easily, and run suites of experiments.


This repo consists of:
- compute node (written in Java)
- web based graphical UI
- associated scripts

Compute has a RESTful API, so it is possible to implement components in other languages, or write alternative visualisations.

* Some info about architecture, the fact that there is a db, core, and GUI
* The db is a big dictionary.
* GUI gives tools to interact and visualise

* Entity is the basic structure, you create them, they get added to tree
* System update is on an entity, and it propagates
* parallel execution (siblings), and consecutive (children)
* standard setup is to have Experiment at the top
* use 'High Level' entities, for Agents, World (each have multiple entities as their children)


# Getting Started

The repository contains a bunch of scripts to help with installation, setup and running. 

All scripts utilise environmental variables defined in a 'variables' file. Every script begins by sourcing this file. ```/resources/variables-template.sh``` is an example with explanations of each variable. You can modify that file, or create your own instead. 
*IMPORTANT:* Then set the ENV variable ```VARIABLES_FILE``` to it using the full path.

## Installation
Installation of the following tools is required and some background knowledge recommended:
* [Maven](https://maven.apache.org/) 
* One or both of [Postgres](http://www.postgresql.org/) and [Couchbase](http://www.couchbase.com/) for the persistence layer

Then:
* Pull the repository
* Set variables. Duplicate ```/resources/variables-template.sh```, and overwrite with values suitable for your environment. Copy it to a convenient location and set an environmental variable VARIABLES_FILE to point to it using the full path. We recommend you set that up in .bashrc so that it is always definted correctly.
* If using postgres, setup and run the db. Run ```/bin/db/setup.sh```


## Running
* The folder that you are running from must contain the file ```node.properties``` and a log4j configuration file. A working template is given in ```/resources/run-empty```.
* node.properties allows you to set the db mode between 'jdbc' and 'node'. The former is postgres, the latter is 'in-memory'.
* You can build and run the compute node using the scripts in ```/bin/node_coordinator```. There is also the option of doing this in a docker container using ```/bin/run-in-docker.sh```, read the help to see how to use it.
	* ```run.sh``` will run the generic main, whereas ```run-demo.sh``` is used to run one of the specific demos, each one has it's own main(). The latter is done to export the entities and data to be imported for running the experiment.
* You can use command line parameters to set node properties, and initial state of system (entities, data)
* Run GUI by running the web server ```/bin/www/python_server.sh``` and going to http://localhost:8000
	* alternatively, open any of the web pages in ```/code/wwww```

