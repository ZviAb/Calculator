terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# Get available availability zones dynamically
data "aws_availability_zones" "available" {
  state = "available"
}

# Network Module
module "network" {
  source             = "./modules/network"
  vpc_cidr_block     = var.vpc_cidr_block
  subnet_cidr_block  = var.subnet_cidr_block
  availability_zone  = data.aws_availability_zones.available.names[0]
  project_prefix     = var.project_prefix
  resource_tags      = var.resource_tags
}

# Compute Module
module "compute" {
  source         = "./modules/compute"
  project_prefix = var.project_prefix
  vpc_id         = module.network.vpc_id
  subnet_id      = module.network.subnet_id
  instance_type  = var.instance_type
  ami_id         = var.ami_id
  app_port       = var.app_port
  resource_tags  = var.resource_tags
  docker_image   = var.docker_image
  app_version    = var.app_version
}
