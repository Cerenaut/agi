
* Modify variables.sh to set the environment variables for your system
* Source variables.sh first
* Change directory to the node type tha tyou wish to run
* Run setup.sh
* Run run.sh (if it exists)

NOTE:
If you want to run the system in docker containers, you must launch a Postgres container, and then an dev/agi container, linked to teh postgres container. The commands are contained at /bin/docker/README-docker-commands.md


The system has been set up so that
* It is possible to setup and run db and dbapi on a separate machine


To Do
* Use MAVEN_HOME and POSTGRESQL_HOME throughout scripts, instead of assuming system version is available
* Modify run.sh so that it passes the coordinator port variable as an integer (not a string)
