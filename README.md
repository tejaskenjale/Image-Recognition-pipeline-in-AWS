# Image-Recognition-pipeline-in-AWS
Image Recognition Pipeline in AWS using EC2 instances, S3, SQS and AWS Rekognition

## Setting up the Environment: - ##

---------------

- Go to AWS and search for "EC2" instances.

- Fill all the detais such as "Instance name". 

- Select Amazon Machine Image as "Amazon Linux 2 AMI (HVM) - Kernel 5.10, SSD Volume Type - ami-0cff7528ff583bf9a (64-bit x86) / ami-00bf5f1c358708486 (64-bit Arm)"
    
- Now, generate the key pair by selecting "Create a new key pair" in the drop down.
     
- Name it "EC2_A_CS643". and Click on "Download key pair."

- In network setting: - Under "Firewall" section- Click on "Create security group" check allow the HTTP, HTTPS, & traffic from "MY IP"
	
- Keep "Number of instances" as "1"

- Click on "Review & Launch" button.
	 
#### Note: - While creating the 2nd instance follow the same procedure but select the existing key pair same as created while creating 1st instance.


## Commands to be executed to set the permission for ".pem": - ##
---------------

- $ chmod 400 EC2_A_CS643.pem
- You can use the putty application to connection establishment.
- After you successful connection, run the following commands to update the java version of EC2: $ chmod 400 {Key-pair}.pem $ sudo yum install java-1.8.0-devel 

      $ sudo /usr/sbin/alternatives --config java

- Now select the corresponding number of java version:- 	

      "java-1.8.0-openjdk.x86_64 (/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.322.b06-2.el8_5.x86_64/jre/bin/java)""

- Repeat the stpe for both the instances.
	
  
## Setup the credentials for the "Access & Secret keys":- ##
---------------
	
- Establish the connection of both the instances and create the file for both:
	
		$mkdir .aws $touch .aws/credentials $vi .aws/credentials

## Copying the credentials in credential file:- ##
---------------


- copy the credentials from AWS details section and paste it into credentials file.

- The format of the credentials are: - 

		 [default] aws_access_key_id=THI5I5NOTTH3ACCE55K3Y
		 aws_secret_access_key= THI5I5NOTTH3S3CR3TACCE55K3Y
     
#### Note: -  This credentials expires after 4 hour so you will need to copy paste afte each session.


## Connecting to EC2 instances and running the jar file:- ##
---------------

- Connect 1st instance which is for car-Text-Recognition: 

		 $ java -jar *locate the jar file for text recognition*
     
 - This will begin running the code for text recognition. However, since it depends on items in the SQS queue to process, it will simply wait until we begin running the car recognition code. 

- To begin running the car recognition code. SSH into EC2-A and run the following command: 

		  $ java -jar **locate the jar file for car recognition 
      
 -  Now, both programs will be running simultaneously. The program on EC2-A is processing all images in the S3 bucket (njit-cs-643) and sending the indexes of images that include cars to EC2-B through SQS, which in turn is processing these images to find out which ones include text as well.





