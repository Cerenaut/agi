# Artificial General Intelligence Experimental Framework

This repository contains code that might help us develop an artificial general intelligence - one day!

## Introduction
For an introduction to the content and purpose of this repository, see the [Wiki](https://github.com/ProjectAGI/agi/wiki). Motivation, results, ideas and other natural language stuff is on our [website](https://agi.io) and in particular our [blog](https://blog.agi.io).

The remainder of this file contains technical information for setting up and using the code in this repository.

## This repository

This repository contains algorithm code and a framework to execute repeatable and fully logged / inspectable experiments. Every piece of data used in the algorithms can be retrospectively analyzed using graphical tools that can be written *after* you discover there's a bug...

The code includes a simple graphical UI, an interprocess layer for distributed coordination and communication, and base classes for the entities that you need for building an AGI experiment. 

We also include implementations of many algorithms from the AI and ML literature.

## Code structure

This repo consists of:

- Java core algorithmic components ```/code/core/src/io/agi/core```
- Java experimental framework componennts ```/code/core/src/io/agi/framework```
- Web based graphical UI ```/code/www```
- Associated scripts ```/bin```

Compute nodes have a RESTful API, so it is possible to implement components in other languages, or write alternative visualisations.
**The API is documented at ```/doc/API/http.swagger.yaml```**

# Important notes

* The framework supports a distributed graph of compute nodes

* Each compute node has any number of Entity nodes (Entity class), which can own Data (Data class)

* Entities have zero or one parents and zero or more child entities. An update to an Entith causes its children to be updated also. 

* An Experiment is a root entity without a parent, and with its descendants is therefore a self-contained subtree.

* This framework allows parallel and consecutive dependencies between Entities to be described (siblings are parallel, children are sequential).

* We currently use Java for compute nodes, but this isn't essential.

* We use JSON format for remote serialization, which is slow but web friendly for debugging / understanding.

* We use JDBC, JSON file, and in-memory implementations of a persistence layer.

* Algorithms are updated iteratively. Between iterations, all data is persisted, therefore components are otherwise stateless and reproducable, repeatable, visualisable

* We provide a HTML user interface to explore and visualize the state of the algorithms.

* The code can be executed on a single local computer in an IDE, or on remote cloud instances.

# Getting Started

The repository contains a bunch of scripts to help with installation, setup and running. 

NOTE: There is a ```run-in-docker.sh``` script that allows you to build and run compute in a docker container, which means you won't need to do any environment configuration on your own computer, save for installation of Docker.

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
* The folder that you are running from must contain the file `node.properties` and a log4j configuration file. A working template is given in `/resources/run-empty`.
* `node.properties` allows you to set the db mode between 'jdbc' and 'node'. The former is postgres, the latter is 'in-memory'.
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
