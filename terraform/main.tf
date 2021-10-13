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
    delete_on_termination = true
  }

  # data disk
  ebs_block_device {
    device_name           = "/dev/sdf"
    volume_size           = "90"
    volume_type           = "gp3"
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
    delete_on_termination = true
  }

  tags = {
    Name = "UK-Sandbox-App-ASG"
  }
}

resource "aws_key_pair" "SSH_KEY_PAIR" {
  key_name   = "UK-Sandbox-Key-Pair"
  public_key = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQDirdXxHIflCBYSrSUwLwtqYoXcLxLfjC9J+ScWZsnoFngTjk6FOFYrEzuaJ9VW9aWiUZTDSJ+7FNU7j1avpZPHj7c9DRVw4V1KkiuZrV24F/xGW17u5fPouQJ8MWtrQrs7erJqZN1bZNISs0TOXPR0+DOvltzzjzmjrNaw2gd5sDrCzBpyqyuUxuLUuIAFyqexe2YfCpVEbrWt+iPW2KOWZyC71eLiiiCNGsj0husabxwvqSN6Su/35hsR6InGoJGHcmqiDOVjIErK/7VSxbEJXjfun1+jvSnGQblEVqBYwo0vCGxwVtEIjbzi5KKRsB86H9jDznFiRx1IAFd0C5BLRCfggomi7UwIdg9HMGj+HscXqIuD9OWC+q3IuPZHNhQPNIACIccvbc3Ee4RtLhGwRM1ooLpsyoLOyGV0npKHhUoniElCWiD3p7opT2Z5gMR8lOYUW/JBvncMu4ZgkEiG9i21jLmM5NvoihOIWwtQbNWkBK1nmvhIJBzV7G2g5/s= jenkins@LAPTOP-PVIMTUA2"
}
