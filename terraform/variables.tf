variable "aws_region" {
  description = "AWS region for resources"
  type        = string
  default     = "ap-south-1"
}

variable "vpc_cidr_block" {
  description = "CIDR block for VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "subnet_cidr_block" {
  description = "CIDR block for public subnet"
  type        = string
  default     = "10.0.1.0/24"
}

variable "project_prefix" {
  description = "Prefix for all resource names"
  type        = string
  default     = "Calculator"
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.micro"
}

variable "ami_id" {
  description = "AMI ID for EC2 instance"
  type        = string
  default     = "ami-087d1c9a513324697"
}

variable "app_port" {
  description = "Application port"
  type        = number
  default     = 8080
}

variable "resource_tags" {
  description = "Common tags for all resources"
  type        = map(string)
  default = {
    Project     = "Calculator"
    ManagedBy   = "Terraform"
  }
}

variable "docker_image" {
  description = "Docker image name (username/repository)"
  type        = string
}

variable "app_version" {
  description = "Application version tag"
  type        = string
}
