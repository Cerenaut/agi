# Artificial General Intelligence Experimental Framework

This repository contains code for the development of artificial general intelligence. It contains algorithm code and a framework to execute repeatable and fully logged / inspectable experiments. Every piece of data used in the algorithms can be retrospectively analysed using graphical tools that can be written *after* you discover there's a bug.

The code includes a simple graphical UI, an interprocess layer for distributed coordination and communication, and base classes for the entities that you need for building an AGI experiment. We also include implementations of many algorithms from the AI and ML literature.

For an introduction to the content and purpose of this repository, see the [Wiki](https://github.com/ProjectAGI/agi/wiki). Motivation, results, ideas and other natural language stuff is on our [website](https://agi.io). Additional technical documentation and tips can also be found in the [docs](./docs) directory.

The remainder of this file contains technical information for setting up and using the code in this repository.

## Table of Contents

* [Code Structure](#code-structure)
* [Requirements](#requirements)
   * [Basic](#basic)
   * [Advanced](#advanced)
* [Getting Started](#getting-started)
   * [Setup Instructions](#setup-instructions)
   * [Running Basic](#running-basic)
   * [Run a Demo](#run-a-demo)
   * [Run Generic Experiments](#run-generic-experiments)
   * [Run Advanced Experiments](#run-advanced-experiments)
   * [Running the GUI](#running-the-gui)
* [Development Environment](#development-environment)
   * [Building](#building)
   * [Testing](#testing)
* [Resources](#resources)

## Requirements
These are the basic requirements necessary for running experiments with AGIEF.

- Linux or macOS
   - We aim to support Microsoft Windows in the future. However, it requires a custom build of the database HTTP API.
- [Docker](https://www.docker.com/)

## Getting Started
To start running experiments, the `/bin/node_coordinator/run-in-docker.sh` script allows you to build and run compute in a Docker container, which means you won't need to do any environment configuration on your own computer, save for installation of Docker.

All scripts utilise environmental variables defined in a 'variables' file. Every script begins by sourcing this file. `/resources/variables-template.sh` is an example with explanations of each variable. You can modify that file, or create your own instead.
**IMPORTANT:** Then set the ENV variable `VARIABLES_FILE` to it using the full path.

That is necessary even if you are using the `run-in-docker.sh` script.

### Setup Instructions
1. Clone the repository using `git clone https://github.com/ProjectAGI/agi.git`
2. Set variables
    - Duplicate `/resources/variables-template.sh` and overwrite with values suitable for your environment
    - Copy it to a convenient location and set an environmental variable `VARIABLES_FILE` to point to it using the full path
    - Note: We recommend you set that up in `.bashrc` so that it is always defined correctly.

**Note:** The favoured (and our current) approach is to use 'in memory' persistence, specified in `node.properties` in the working folder. However, `postgres` is an option. If using PostgreSQL, setup and run the db by executing `/bin/db/setup.sh`

### Running Basic
* The folder that you are running from must contain the file `node.properties` and a log4j configuration file. A working template is given in `/resources/run-empty`.
* `node.properties` allows you to set the db mode between 'jdbc' and 'node'. The former is PostgreSQL, the latter is 'in-memory'.
* You can build and run the Compute Node using the scripts in `/bin/node_coordinator`. There is also the option of doing this in a docker container using `/bin/run-in-docker.sh`, read the help to see how to use it.
* There scripts for running the system, they take parameters such as the node properties and the initial state of system (entities, data)
* Once a Compute Node is run as a Demo or Generic Experiment (see below), it will be running as a server. You can then load and export experiments via the HTTP API. The GUI utilises the API to make it easy to do that and to visualise all data structures, entity tree and entity configurations.

### Run a Demo
The simplest way to run an experiment is to choose a Demo. There are a bunch of examples in the package `io.agi.framework.demo`.

* Choose a demo. By convention they are named `[demo-name]Demo`.
* Launch a Compute Node by running the appropriate `main()` method for that Demo. 
* They can be run within the IDE or using `run-demo.sh`. 
* Send an `update` signal to the root `Experiment` node to start the experiment. You can do this using the RESTful API, or with the GUI, where you can also see what's going on. 
* The Demo is an experiment that has been defined in code, alternatively, you can run an experiment defined in JSON input files.

### Run Generic Experiments
This describes how to run an experiment defined in JSON input files. If you don't already have them ready to go, you can run a given demo and export the input files (using GUI or RESTful API).

* Launch the framework with the generic `main()` method
* Use `run.sh` or IDE
* You will need to import the input files that define your experiment
* It could be specified as input parameter when running, or after launched you can use the RESTful API or GUI
* As above, get the experiment started by sending an `update` to the root `Experiment` node

### Run Advanced Experiments
We have a tool called [run-framework](https://github.com/ProjectAGI/run-framework) which makes it easy to run predefined experiments, locally or remotely on physical or AWS infrastructure, conduct parameter sweeps, export and upload the results and more.

There is also a set of experiment folders already defined and ready to go at [experiment-definitions](https://github.com/ProjectAGI/experiment-definitions).

### Running the GUI
* Run GUI by running the web server `/bin/www/python_server.sh` and going to [http://localhost:8000](http://localhost:8000)
* Alternatively, open any of the web pages in `/code/www`
* Start with `index.html`

## Resources
There are additional useful resources available in the [/resources](./resources) directory. This includes a code formatting style file, log4j configuration file template, an empty run-folder with necessary assets for the working directory and a template for the `variables.sh` file

## Contributing
The purpose of this repository is to continue to improve AGIEF, making it better, faster and easier to use for the research community. The development happens in the open on GitHub, and we are grateful to the community for contributing bug fixes and improvements. Read below to learn how you can start contributing.

### [Code of Conduct](CODE_OF_CONDUCT.md)
We have adopted a Code of Conduct that we expect project participants to adhere to. Please read the [full text](CODE_OF_CONDUCT.md) so that you can understand what actions will and will not be tolerated.

### [Contributing Guide](CONTRIBUTING.md)
Read our contributing guide to learn about the development process, how to setup a development environment, how to build and test the framework and how to propose improvements and bug fixes.

### License
The code is licensed under the [GNU General Public License v3.0](LICENSE).
