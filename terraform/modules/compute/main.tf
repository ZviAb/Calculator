resource "aws_security_group" "Calculator_sg" {
  vpc_id = var.vpc_id

  ingress {
    from_port   = var.app_port
    to_port     = var.app_port
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(
    var.resource_tags,
    {
      Name = "${var.project_prefix}_sg"
    }
  )
}

resource "aws_instance" "ec2" {
  ami                         = var.ami_id
  instance_type               = var.instance_type
  subnet_id                   = var.subnet_id
  vpc_security_group_ids      = [aws_security_group.Calculator_sg.id]
  user_data                   = templatefile("${path.root}/user-data.sh", {
    docker_image = var.docker_image
    app_version  = var.app_version
  })
  associate_public_ip_address = true

  tags = merge(
    var.resource_tags,
    {
      Name = "${var.project_prefix}_Ec2"
    }
  )
}