
# Artificial General Intelligence Experimental Framework

AGIEF makes it easy to set up repeatable and logged experiments. It consists of a simple graphical UI, an Interprocess layer for distributed coordination and communication, and a base classes for the entities that you need for building an AGI experiment. The system architecture is shown [here]("https://github.com/ProjectAGI/agi/blob/master/resources/docs/Project AGI_ AGI Experimental Framework_ A platform for AGI R&D.pdf"). You can implement your own Agents, Worlds, Sensors and Actuators easily, and run suites of experiments.

The graphical UI is web based, and the core modules are written in Java. The most common use case is to inherit the base Agent and implement with custom AI algorithms, and test on one of the available Worlds. Similarly, it is possible to inherit the base World and implement other environments.

Also, since each module communicates in a well documented RESTful API, it's is possible to re-implement any of these modules in any language.

If you'd like to modify or add to the functionality of the framework, this is also straightforward. The system is built around the API spec. Libraries for clients, servers and documentation are then automatically generated with Swagger. The modules are then built on these libraries.


Each scenario is elaborated below after the installation instructions.


## Installation

This section explains how to install the system and get the modules talking to each other. It's an AGIEF "Hello World".


Installation of the following popular tools is required and some background knowledge recommended:
- [Maven](https://maven.apache.org/) 

Following steps to get started:

* Pull the repository
* Open **variables.sh** and set the values to be correct for your environment
* Run **setup.sh** . This needs to be run once in a given shell, until variables or system settings change. It will setup the database, necessary environment variables, install the dependent libs and compile the core modules.


## Launching a node

* Run ```run.sh``` to run the coordinator node (to explain passing different param for non coordinator node)


## Scenario 1: Writing your own Agents and/or Worlds

* Create a project that includes the experimental framework Experiment and Interprocess packages as dependencies
* Inherit the Agent or World classes ....

See the demo HelloWorld for a template that you can add to and modify.



## Scenario 2: Modify the framework

Installation of the following popular tool(s) is required and some background knowledge recommended:
- [Swagger](http://swagger.io/)

If you want to get further into the implementation details, knowledge of the following libraries is recommended:
- [Jetty](http://www.eclipse.org/jetty/)
- [Jersey](https://jersey.java.net/)

* Modify the spec (**/ApiSpec/spec.yaml**) as desired according to Swagger spec. The easiest way to do this is to go to the online [Swagger Editor](http://editor.swagger.io/#/), paste the yaml contents in, and see the documentation change dynamically. _More information in README-Swagger.md_.
* Re-generate the relevant library using the provided scripts in **bin/db/codegen** :
	* **genClient.sh**
	* **genServer.sh**
	* **genPersistence.sh**

* They use the [swagger-codegen](https://github.com/swagger-api/swagger-codegen) project. You will need to use the modified template 'Mustache' file(s) with Swagger. Copy them from **/resources/swagger-mustache** to the appropriate folder in the swagger-codegen source code project before building Swagger again. 
	* Use the script **copyMustache.sh** provided in **/bin/codegen**.

GOTCHA: You must copy the modified mustache files as described above, BEFORE you build the swagger-codgen project. If you make any further changes to any mustache files, you must re-compile the project before the changes take effect.

Modify Coordinator Server:

* The folder **/lib/CoordinatorServerLib/tmp** contains empty concrete API Implementation classes
* Drag these to the Server project and modify as you wish


Modify Coordinator Client:

* the lib **/lib/CoordinatorClientLib** contains a series of **[Name]Api.java** files
* these contain the available requests that you can use in your Client project