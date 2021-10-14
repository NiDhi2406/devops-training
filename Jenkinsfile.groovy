pipeline {
    options {
        disableConcurrentBuilds()
        parallelsAlwaysFailFast()
        timestamps()
    }

    agent {
        label {
            label "${JENKINS_SLAVE_LABEL}"
        }
    }

    parameters {
        string(name: 'AWS_ACCESS_KEY', defaultValue: '', description: '', trim: true)
        string(name: 'AWS_SECRET_KEY', defaultValue: '', description: '', trim: true)
        choice(name: 'JENKINS_SLAVE_LABEL', choices: ['master'], description: '')
    }
    
    environment {
        //Do not change below environment variables
        DATE = '`date +"%F-Time-%H-%M"`'
        NEW_SSH_PUB_KEY = sh (script: "cat /var/lib/jenkins/.ssh/id_rsa.pub", returnStdout: true ).trim()
    }
    
    stages {    
        stage('Cleaning Workspace Before Build Creation') {
            steps {
                cleanWs()
            }
        }

        stage('Git Clone') {
            steps {
                sh """
                git clone git@github.com:NiDhi2406/devops-training.git
                cd devops-training
                git checkout main
                pwd
                ls -ltrh
                """
            }
        }

		stage('Setup AWS CLI') {
            steps {
                script {
                    sh """
                    echo "[default]
                    aws_access_key_id = ${AWS_ACCESS_KEY}
                    aws_secret_access_key = ${AWS_SECRET_KEY}" | tee /var/lib/jenkins/.aws/credentials
                    cat /var/lib/jenkins/.aws/credentials
                    aws s3 ls
                    """
                }
            }
        }/*
		stage('Terminating Existing AWS Instances if any') {
            steps {
                script {
	
                    sh """
                    #!/bin/bash

                    set -x
                    set +e
                    echo "Terminating region us-east-1..."
                    aws ec2 describe-instances --region us-east-1 | \
                        jq -r .Reservations[].Instances[].InstanceId | \
                        xargs -L 1 -I {} aws ec2 modify-instance-attribute \
                            --region us-east-1 \
                            --no-disable-api-termination \
                            --instance-id {}
                    aws --profile default --region us-east-1 ec2 delete-key-pair --key-name UK-Sandbox-Key-Pair
		    set -e
                    """
                }
            }
        }
		stage('Setup Terraform') {
            steps {
                script {
	
                    sh """
                    cd $WORKSPACE/devops-training/terraform
		    terraform init

                    sed -i 's|  public_key = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQDirdXxHIflCBYSrSUwLwtqYoXcLxLfjC9J+ScWZsnoFngTjk6FOFYrEzuaJ9VW9aWiUZTDSJ+7FNU7j1avpZPHj7c9DRVw4V1KkiuZrV24F/xGW17u5fPouQJ8MWtrQrs7erJqZN1bZNISs0TOXPR0+DOvltzzjzmjrNaw2gd5sDrCzBpyqyuUxuLUuIAFyqexe2YfCpVEbrWt+iPW2KOWZyC71eLiiiCNGsj0husabxwvqSN6Su/35hsR6InGoJGHcmqiDOVjIErK/7VSxbEJXjfun1+jvSnGQblEVqBYwo0vCGxwVtEIjbzi5KKRsB86H9jDznFiRx1IAFd0C5BLRCfggomi7UwIdg9HMGj+HscXqIuD9OWC+q3IuPZHNhQPNIACIccvbc3Ee4RtLhGwRM1ooLpsyoLOyGV0npKHhUoniElCWiD3p7opT2Z5gMR8lOYUW/JBvncMu4ZgkEiG9i21jLmM5NvoihOIWwtQbNWkBK1nmvhIJBzV7G2g5/s= jenkins@LAPTOP-PVIMTUA2"|  public_key = "$NEW_SSH_PUB_KEY"|g' main.tf

                    terraform plan
                    terraform plan -out=result
                    terraform apply result
		    
                    """
                }
            }
        }*/

		stage('Install Nginx Using Ansible') {
            steps {
                script {
		            APP_SERVER_PUBLIC_IP = sh (script: "aws ec2 --profile default --region us-east-1 describe-instances --filters \"Name=instance-state-name,Values=running\" \"Name=tag:Name,Values=UK-Sandbox-App-ASG\" --query 'Reservations[*].Instances[*].[PublicIpAddress]' --output text", returnStdout: true ).trim()
                    sh """
                    cd $WORKSPACE/devops-training/ansible
                    GET_SECURITY_GROUP=`aws --profile default --region us-east-1 ec2 describe-security-groups --group-names default --query 'SecurityGroups[*].[GroupId]' --output text`
		    aws --profile default --region us-east-1 ec2 authorize-security-group-ingress \
		    --group-id \$GET_SECURITY_GROUP \
		    --protocol tcp \
		    --port 22 \
		    --cidr 0.0.0.0/0 || true
		    
		    aws --profile default --region us-east-1 ec2 authorize-security-group-ingress \
		    --group-id \$GET_SECURITY_GROUP \
		    --protocol tcp \
		    --port 80 \
		    --cidr 0.0.0.0/0 || true
		    
		    aws --profile default --region us-east-1 ec2 authorize-security-group-egress \
		    --group-id \$GET_SECURITY_GROUP \
		    --protocol tcp \
		    --port 80 \
		    --cidr 0.0.0.0/0 || true
		    
		    aws --profile default --region us-east-1 ec2 authorize-security-group-egress \
		    --group-id \$GET_SECURITY_GROUP \
		    --protocol tcp \
		    --port 443 \
		    --cidr 0.0.0.0/0 || true
		    
                    sudo sed -i '/myserver/d' /etc/hosts
                    sudo cat /dev/null > /var/lib/jenkins/.ssh/known_hosts
                    echo "${APP_SERVER_PUBLIC_IP} myserver" | sudo tee -a /etc/hosts
                    ansible-playbook install-nginx.yml --extra-vars 'WORKSPACE=${WORKSPACE}'
                    """
                }
            }
        }
    }
    
	post {
        always {                         
            echo "Cleaning Workspace After Pipeline Execution"
            deleteDir() /* Cleaning up Workspace */
        }
    }
}
