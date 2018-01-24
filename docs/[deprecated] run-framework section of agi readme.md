
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

