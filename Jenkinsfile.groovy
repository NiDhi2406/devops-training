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
        APP_SERVER_PUBLIC_IP = sh (script: "aws ec2 --profile default --region us-east-1 describe-instances --filters \"Name=instance-state-name,Values=running\" \"Name=tag:Name,Values=UK-Sandbox-App-ASG\" --query 'Reservations[*].Instances[*].[PublicIpAddress]' --output text", returnStdout: true ).trim()
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
        }

		stage('Setup Terraform') {
            steps {
                script {
                    sh """
                    cd $WORKSPACE/devops-training/terraform
		    terraform init
		    terraform destroy
                    

                    sed -i 's|  public_key = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQDirdXxHIflCBYSrSUwLwtqYoXcLxLfjC9J+ScWZsnoFngTjk6FOFYrEzuaJ9VW9aWiUZTDSJ+7FNU7j1avpZPHj7c9DRVw4V1KkiuZrV24F/xGW17u5fPouQJ8MWtrQrs7erJqZN1bZNISs0TOXPR0+DOvltzzjzmjrNaw2gd5sDrCzBpyqyuUxuLUuIAFyqexe2YfCpVEbrWt+iPW2KOWZyC71eLiiiCNGsj0husabxwvqSN6Su/35hsR6InGoJGHcmqiDOVjIErK/7VSxbEJXjfun1+jvSnGQblEVqBYwo0vCGxwVtEIjbzi5KKRsB86H9jDznFiRx1IAFd0C5BLRCfggomi7UwIdg9HMGj+HscXqIuD9OWC+q3IuPZHNhQPNIACIccvbc3Ee4RtLhGwRM1ooLpsyoLOyGV0npKHhUoniElCWiD3p7opT2Z5gMR8lOYUW/JBvncMu4ZgkEiG9i21jLmM5NvoihOIWwtQbNWkBK1nmvhIJBzV7G2g5/s= jenkins@LAPTOP-PVIMTUA2"|  public_key = "$NEW_SSH_PUB_KEY"|g' main.tf

                    terraform plan
                    terraform plan -out=result
                    terraform apply result
		    
                    """
                }
            }
        }

		stage('Install Nginx Using Ansible') {
            steps {
                script {
                    sh """
                    cd $WORKSPACE/devops-training/ansible
                    echo "${APP_SERVER_PUBLIC_IP} myserver" | sudo tee -a /etc/hosts
                    ansible-playbook install-nginx.yml
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
