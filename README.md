*Note: The framework is currently in proof of concept phase. Not all the modules described below are implemented, which is indicated in the text. The functionality described is implemented.*

# Artificial General Intelligence Experimental Framework

AGIEF makes it easy to set up repeatable and logged experiments. It consists of a simple graphical UI, base Agents, a World and a Coordinator that communicate with each other via a client/server RESTful architecture. The system architecture is shown [here](https://github.com/ProjectAGI/agi/blob/master/resources/AGIHighLevelDesignSystemArchitecture.png). You can implement your own Agents or Worlds easily, and run suites of experiments.

The graphical UI is web based, and the Coordinator, base Agent and World modules are written in Java. The most common use case is to inherit the base Agent and implement with custom AI algorithms, and test on one of the available Worlds. Similarly, it is possible to inherit the base World and implement other environments.

Also, since each module communicates in a well documented RESTful API, it's is possible to re-implement any of these modules in any language.

If you'd like to modify or add to the functionality of the framework, this is also straightforward. The system is built around the API spec. Libraries for clients, servers and documentation are then automatically generated with Swagger. The modules are then built on these libraries.


Each scenario is elaborated below.

## Prerequisites
You must set an environment variable for the AGI project dir. In bash:
````
export $AGI_PROJECT_DIR=path/to/folder
````

Installation of the following popular tools is required and some background knowledge recommended:
- [Maven](https://maven.apache.org/) 


## Getting Started
This section explains how to install the system and get the modules talking to each other. You can think of it as a "Hello World".

* Pull the repository
* Install the Client and Server libs. This installs them to the local Maven repository, which makes them available to dependent projects, in this case the Agent, World and Coordinator modules.
	*	**Client:**
	* 	_cd lib/CoordinatorClientLib_
	*	_mvn install_
	*	**Server**
	* 	_cd lib/CoordinatorServerLib_
	*	_mvn install_
* Build the modules, listed below. They can all be built using the Maven command **mvn package**. Additionally, each is an IntelliJ project:
	*	Agent (located at **/Agent**)
	*	World (located at **/World**)
	* 	Coordinator (located at **/Coordinator**)
* Run the Coordinator. It is an embedded Jetty web server, so will launch a server that will be listening on a port (hard coded to 9999 now).
	*	You can test it out by going to **http://localhost:9999** and trying any of the calls documented in the API Documentation, which can be viewed by opening **/APIDocumentation/index.html**.
* Run the Agent, it will communicate with the Coordinator by making one simple call **/control/run**, and will log the output to the console. Verify that it has done so, it should be an array of integers (timestamps).
* Next, open the Web UI (needs a name, and not implemented yet).
* Select from the options and observe that the system has received the command by verifying the responses via visual feedback in the UI.


## Scenario 1: Testing your own Agents and Worlds
* Open the relevant project, and inherit the Agent or World classes ....



## Scenario 2: Modify the framework

Installation of the following popular tool(s) is required and some background knowledge recommended:
- [Swagger](http://swagger.io/)

If you want to get further into the implementation details, knowledge of the following libraries is recommended:
- [Jetty](http://www.eclipse.org/jetty/)
- [Jersey](https://jersey.java.net/)

* Modify the spec (**/ApiSpec/coordinator.yaml**) as desired according to Swagger spec. The easiest way to do this is to go to the online [Swagger Editor](http://editor.swagger.io/#/), paste the yaml contents in, and see the documentation change dynamically.
* Re-generate client and server using Swagger, in particular the [swagger-codegen](https://github.com/swagger-api/swagger-codegen) project. You will need to use the modified template 'Mustache' file(s) with Swagger. Copy them from **/resources** to the appropriate folder in the swagger-codegen source code project before building Swagger again (with the command **mvn package**)):
	* **/bin/genClient.sh**
	* **/bin/genServer.sh**
* *NOTE: You need to set the paths correctly in the scripts, which will be improved in later versions*
* These scripts will generate code, adjust folder structure where necessary, build the libs and install to local Maven repository.
* Then modify the server and client projects accordingly (see below).


Modify Coordinator Server:

* The folder **/lib/CoordinatorServerLib/tmp** contains empty concrete API Implementation classes
* Drag these to the Server project and modify as you wish


Modify Coordinator Client:

* the lib **/lib/CoordinatorClientLib** contains a series of **[Name]Api.java** files
* these contain the available requests that you can use in your Client project
