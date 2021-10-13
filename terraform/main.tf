terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 3.27"
    }
  }

  required_version = ">= 0.14.9"
}

provider "aws" {
  profile = "default"
  region  = "us-east-1"
}

resource "aws_instance" "NFS_SERVER" {
  ami           = "ami-0747bdcabd34c712a"
  instance_type = "t2.micro"
  key_name      = aws_key_pair.SSH_KEY_PAIR.id

  # root disk
  root_block_device {
    encrypted             = true
    delete_on_termination = true
  }

  tags = {
    Name = "UK-Sandbox-NFS"
  }
}

resource "aws_instance" "SYMANTEC_SERVER" {
  ami           = "ami-0747bdcabd34c712a"
  instance_type = "t2.micro"
  key_name      = aws_key_pair.SSH_KEY_PAIR.id

  # root disk
  root_block_device {
    encrypted             = true
    delete_on_termination = true
  }

  tags = {
    Name = "UK-Sandbox-Symantec"
  }
}

resource "aws_instance" "APP_SERVER" {
  ami           = "ami-0747bdcabd34c712a"
  instance_type = "t2.small"
  key_name      = aws_key_pair.SSH_KEY_PAIR.id

  # root disk
  root_block_device {
    encrypted             = true
    delete_on_termination = true
  }

  tags = {
    Name = "UK-Sandbox-App-ASG"
  }
}

resource "aws_key_pair" "SSH_KEY_PAIR" {
  key_name   = "UK-Sandbox-Key-Pair"
  public_key = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQDaPv/t+FUXv1vZ+EEqE/aeW22aTtqj+Gpulp1+/tLIdQhKw5tX4/47n5vsVpBnpZCNzeS96pDXL11CZjco6Hc6DAGh5gxVQtpOI6eC6aT7CToVqn7wirx8ubZq7Y38jrYnoEEAIWEZdeyoVkpNl6ah8xNI774ncp+SE9IF4QcIddhPnOsmRnXiee/Xz1nDtoJp1fKYxJyoNHVOsx9fYYT4iYsbyIshg5cFNtWBfoSsYVOgkxKIvq+9z7cS3MbQ0YPIn6QJPWocPOUEMsdfhqimgxGxg50DI56TJzo2aeVA8fj3x/alBhG3vCfTOxSiLoSj6Fwg+RgDb/+chky5mKoKCekna55aEvG5cVrGpbZJgUt4G9XWgsVtpAWraR7wD2g9MCOx92vbDWWRoqJg9VV+r4r8ZL/ATqY5I2RW/oAQuv+GQuA4aFuae/XblAiCB7UQS1VNwyngEErlCWCnZoUdlDatIJj3uJqiR0uZIDkDvkijaR8yLeBdzUzJ2fWdWJk= root@LAPTOP-PVIMTUA2"
}
