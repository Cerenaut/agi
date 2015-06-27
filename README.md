# Artificial General Intelligence Experimental Framework #

AGIEF makes it easy to set up repeatable and logged experiments. It consists of shells of Agents, a World and a Coordinator that communicate with each other via a client/server RESTful architecture. The system architecture is shown [here](https://github.com/ProjectAGI/agi/blob/master/resources/AGIHighLevelDesignSystemArchitecture.png). You can implement your own Agents or Worlds easily.

If you'd like to modify the framework itself, you start with the spec (/ApiSpec).
The shell clients and servers are then automatically generated with swagger.
_Instructions below._

To get started with the current spec:
* install libs to local maven repository
	*	**Client**
	* 	cd lib/CoordinatorClientLib
	*	mvn install
	*	**Server**
	* 	cd lib/CoordinatorServerLib
	*	mvn install
* then modify the server and client projects accordingly (see below)


Modify spec and get started:
* change the spec (/ApiSpec/coordinator.yaml) as desired according to swagger spec
* re-generate client and server
	* /bin/genClient.sh
	* /bin/genServer.sh
* these scripts will generate code, adjust folder structure where necessary, build the libs and install to local maven repository
* then modify the server and client projects accordingly (see below)


Modify Coordinator Server:
* the folder /lib/CoordinatorServerLib/tmp contains empty concrete Api Implementation classes
* drag these to the Server project and modify as you wish


Modify Coordinator Client:
* the lib /lib/CoordinatorClientLib contains a series of [Name]Api.java files
* these contain the available requests that you can use in your Client project
