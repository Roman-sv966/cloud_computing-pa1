# cloud_computing-pa1
# AWS Image Recognition Pipeline

This repository contains two Java applications that implement an AWS-based image recognition pipeline. The pipeline uses Amazon Rekognition, S3, and SQS to detect cars and text in images stored in an S3 bucket.

- **ImageCarDetector**: Identifies images containing cars and sends their names to an SQS queue.
- **TextDetector**: Reads image names from the SQS queue, performs text recognition, and logs the results to a file.

---

## Setup

### Prerequisites

```plaintext
- AWS Educate Account: Retrieve AWS credentials and `.pem` key file for SSH access.
- Java 11: Ensure the Java Development Kit is installed on your EC2 instances.



1. Log in to the AWS EC2 Dashboard.
2. Launch two EC2 instances with the following configuration:
   - AMI: Amazon Linux 2
   - Instance Type: t2.micro (free-tier eligible)
   - Security Groups:
     - Allow SSH (port 22) from your IP address.
3. Note the public DNS of each instance for file transfer and SSH access:
   - Instance A: Runs ImageCarDetector.
   - Instance B: Runs TextDetector.


# To Instance A (ImageCarDetector)
scp -i <your-key.pem> aws-dependencies.jar ImageCarDetector.java ec2-user@<instance-a-public-dns>:/home/ec2-user/

# To Instance B (TextDetector)
scp -i <your-key.pem> aws-dependencies.jar TextDetector.java ec2-user@<instance-b-public-dns>:/home/ec2-user/



# SSH into each instance:
ssh -i <your-key.pem> ec2-user@<instance-public-dns>

# Install Java 11:
sudo yum install -y java-11-amazon-corretto-devel

# Verify the installation:
java -version



# Create a credentials file:
mkdir ~/.aws
nano ~/.aws/credentials

# Add your AWS credentials:

[default]
aws_access_key_id = YOUR_ACCESS_KEY
aws_secret_access_key = YOUR_SECRET_KEY
aws_session_token = YOUR_SESSION_TOKEN

