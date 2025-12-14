output "vpc_id" {
  description = "ID of the VPC"
  value       = aws_vpc.Calculator_Vpc.id
}

output "subnet_id" {
  description = "ID of the public subnet"
  value       = aws_subnet.Calculator_public_Subnet.id
}

output "internet_gateway_id" {
  description = "ID of the Internet Gateway"
  value       = aws_internet_gateway.Calculator_igw.id
}
