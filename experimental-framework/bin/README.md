
* Modify variables.sh to set the environment variables for your system
* Source variables.sh first
* Change directory to the node type tha tyou wish to run
* Run setup.sh
* Run run.sh (if it exists)

NOTE:
If you want to run the system in docker containers, you must launch a Postgres container, and then an dev/agi container, linked to teh postgres container. The commands are contained at /bin/docker/README-docker-commands.md


The system has been set up so that
* It is possible to setup and run db and dbapi on a separate machine