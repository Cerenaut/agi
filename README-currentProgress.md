In order to run, you'll have to do the following.

- pull repo
- go to lib/CoordinatorClientLib and run 'mvn clean install'
- go to lib/CoordinatorServerLib and run 'mvn clean install'
- go to Coordinator project in IntelliJ, and run
- go to Agent project in IntelliJ, and run
- go to browser, and step the Coordinator ( http://localhost:8080/coordinator/control/step )
- observe that the Agent steps in the console of the Agent project

Note: CoordinatorClient/ServerLib terminology above should be replaced with 'core AGIEF server/client'

What has happened?

- The Coordinator launched a core AGIEF server
- The Agent launched a core AGIEF server
- The Agent requested the Coordinator to connect to it, passing a path to use
- The Coordinator connected
- You manually told the Coordinator to step, and it told all the connected Agents and Worlds


NOW: 
- to modify the spec to the actual control and state commands that we want, and make it do actual stuff
- fix bugs and continue developing for production version (see Pivotal)

