output "ec2_public_ip" {
  description = "Public IP address of EC2 instance"
  value       = module.compute.ec2_public_ip
}
