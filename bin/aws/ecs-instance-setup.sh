
# install aws cli

sudo yum install unzip

sudo curl "https://s3.amazonaws.com/aws-cli/awscli-bundle.zip" -o "awscli-bundle.zip"
sudo unzip awscli-bundle.zip
sudo ./awscli-bundle/install -i /usr/local/aws -b /usr/local/bin/aws


sudo yum install rsync

# configure aws aws-cli (although the IAM role allows S3 read, so this is not mandatory)
aws configure