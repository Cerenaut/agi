These readme files can also be found on the Wiki @ https://github.com/ProjectAGI/agi/wiki


# Artificial General Intelligence Experimental Framework

... note: this must be consolidated with the material on the wiki ..... 

AGIEF makes it easy to set up repeatable and logged experiments. It consists of a simple graphical UI, an Interprocess layer for distributed coordination and communication, and a base classes for the entities that you need for building an AGI experiment. The system architecture is shown [here]("https://github.com/ProjectAGI/agi/blob/master/resources/docs/Project AGI_ AGI Experimental Framework_ A platform for AGI R&D.pdf"). You can implement your own Agents, Worlds, Sensors and Actuators easily, and run suites of experiments.

The graphical UI is web based, and the core modules are written in Java. The most common use case is to inherit the base Agent and implement with custom AI algorithms, and test on one of the available Worlds. Similarly, it is possible to inherit the base World and implement other environments.

Also, since each module communicates in a well documented RESTful API, it's possible to re-implement any of these modules in any language. We encourage anyone to modify or add to the functionality of the framework.


	* Some info about architecture, the fact that there is a db, core, and GUI
	* The db is a big dictionary.
	* All models mirror this exactly
	* Access to persistence is always via Core with RESTful API
	* GUI gives tools to interact and visualise

	* Entity is the basic structure, you create them, they get added to tree
	* System update is on an entity, and it propagates
	* parallel execution (siblings), and consecutive (children)
	* standard setup is to have Experiment at the top
	* use 'High Level' entities, for Agents, World (each have multiple entities as their children)


# Getting Started

The repository contains a bunch of scripts to help with installation, setup and running. A common file contains all the environment variables for the framework, they define parameter values and relevant path. By default, this file is */bin/variables.sh*. You can override this by specifying the path to an alternative file in an environment variable *VARIABLES_FILE*. If using the defaulat, *variables.sh*, modify it to set the environment variables for your system. Every script begins by sourcing this file.

## Installation

Installation of the following tools is required and some background knowledge recommended:
* [Maven](https://maven.apache.org/) 
* One or both of [Postgres](http://www.postgresql.org/) and [Couchbase](http://www.couchbase.com/) for the persistence layer

Then:
* Pull the repository
* Set variables. Open ```variables.sh``` (or alternative variables file) and overwrite with values suitable for your environment.
* Setup and run the db. Run ```/bin/db/setup.sh```
* Setup and run coordinator. Run **setup.sh**. This compiles and installs the dependent libs, and compiles the coordinator node.


## Running
* Run ```run.sh``` to run the coordinator node (to explain passing different param for non coordinator node)
* Can specify following parameters to set node, and initial state of system (entities, data)
	... to expand
* Run GUI by opening any of the web pages in ```/code/web-ui```
* Primary page is ```update.html```, more information on available GUI components [here](blah)

# Using the framework
You may be here to develop the framework, or to use it to run experiments.
More information for these scenarios is given in the sections below.

# Scenario 1: Running experiments with AGIEF

Use a demo as an example
Inherit Experiment, Agent and World
Add with json input file


# Scenario 2: Developing AGIEF

to write




