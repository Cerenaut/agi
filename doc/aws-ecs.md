- create an s3 bucket with the code
- create ec2 instance from the standard ecs AMI 
- synch the s3 folder onto the ec2 instance
	- log into the ec2 instance
	- install aws cli tools (http://docs.aws.amazon.com/cli/latest/userguide/installing.html)
	- synch the code from s3 (http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/AmazonS3.html)



Every time the code is updated, we'll need to go into the ec2 instance and re-synch.
This needs to be scripted, or a better way to access a persistent volume from a task - but I haven't found it.
--> It looks like EFS will do this job, but not available yet.