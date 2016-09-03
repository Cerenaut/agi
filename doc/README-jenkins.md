
- through jenkins, install github-authorisation plugin, so that we can use github user identities to log in and for access priviliges

- ssh in, and install aws-cli (see ecs-instance-setup.sh for instructions)

- once the job has run and you get the repository, you can navigate to the workspace with the python dependencies and install dependencies. It's all in the README.md, /bin/run

- setup ssh key to enable rsync over ssh with the ecs ec2 instance
put a private key in 'tomcat' user's ~/.ssh folder
you will need to do `sudo su tomcat`

- configure aws-cli
log in as tomcat (`sudo su tomcat`)
run `aws configure`
use your credentials and set the appropriate region

- through Jenkins configuration, add credentials for github user agi-technology, for access to repositories

