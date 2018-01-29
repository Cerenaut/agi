# AGIEF in Jenkins
We utilise Jenkins to run and manage experiments across multiple machines, including AWS. The instructions below only apply to our current infrastructure but may be useful for those interested in setting up a similar infrastructure using Jenkins.

## Requirements
- [Jenkins](http://jenkins-ci.org/) 1.6+

## Setup Instructions
1. Install `github-authorisation` plugin in Jenkins, so that GitHub user identities can be used for authentication and access privilige

2. SSH into the Jenkins instance and and install the [AWS Command Line Interface](https://aws.amazon.com/cli/)

3. Once the job has run and you get the repository, you can navigate to the workspace with the Python dependencies and install the dependencies.

4. Setup an SSH key to enable `rsync` over ssh with the EC2 instance
  1. Add the private key in `/home/tomcat/.ssh` folder
  2. You will need to do `sudo su tomcat`

5. Configure `aws-cli`
  1. Login in as tomcat using `sudo su tomcat`
  2. Execute `aws configure` to configure
  3. Use your AWS credentials and set the appropriate region

6. Through the Jenkins configuration, add credentials for GitHub user `agi-technology`, for access to repositories
