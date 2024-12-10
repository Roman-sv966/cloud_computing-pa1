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

# Setup

1. Log in to the AWS EC2 Dashboard.
2. Launch two EC2 instances with the following configuration:
   - AMI: Amazon Linux 2
   - Instance Type: t2.micro (free-tier eligible)
   - Security Groups:
     - Allow SSH (port 22) from your IP address.
     - HTTP (port 80)
     - HTTPS (port 443)
3. Note the public DNS of each instance for file transfer and SSH access:
   - Instance A: Runs ImageCarDetector.
   - Instance B: Runs TextDetector.
4. SQS Queue:
   - Navigate to the **SQS Dashboard**.
   - Create a **FIFO queue** and note the queue URL for use in the applications.


#Copy Files to Ec2

# To Instance A (ImageCarDetector)
scp -i labsuser.pem aws-dependencies.jar ImageCarDetector.java ec2-user@<instance-a-public-dns>:/home/ec2-user/

# To Instance B (TextDetector)
scp -i labsuser.pem aws-dependencies.jar TextDetector.java ec2-user@<instance-b-public-dns>:/home/ec2-user/



# SSH into each instance:
ssh -i labsuser.pem ec2-user@<instance-public-dns>

# Install Java 11:
sudo yum install -y java-11-amazon-corretto-devel




# Create a credentials file and Add your AWS credentials:
mkdir ~/.aws
nano ~/.aws/credentials

Copy from AWS Educate Account
[default]
aws_access_key_id = YOUR_ACCESS_KEY
aws_secret_access_key = YOUR_SECRET_KEY
aws_session_token = YOUR_SESSION_TOKEN




# SSH into Instance A
ssh -i labsuser.pem ec2-user@<instance-a-public-dns>

# Compile and run the ImageCarDetector application:
javac -cp .:aws-dependencies.jar ImageCarDetector.java
java -cp .:aws-dependencies.jar ImageCarDetector


This application will:
- Detect cars in the S3 images using Rekognition.
- Send image identifiers of detected cars to the SQS queue.




# SSH into Instance B
ssh -i labsuser.pem ec2-user@<instance-b-public-dns>

# Compile and run the TextDetector application:
javac -cp .:aws-dependencies.jar TextDetector.java
java -cp .:aws-dependencies.jar TextDetector


This application will:
- Read image identifiers from the SQS queue.
- Perform text recognition on the images.
- Save the results to /home/ec2-user/text-detection-results.txt.




# common Issues
1. Access Denied:
   Ensure the .pem key file has the correct permissions:
   chmod 400 labsuser.pem

2. AWS Credentials:
   Double-check the ~/.aws/credentials file for typos or expired session tokens.

3. Dependency Errors:
   Ensure aws-dependencies.jar is uploaded and included in the classpath.

