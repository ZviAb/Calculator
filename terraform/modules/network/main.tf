resource "aws_vpc" "Calculator_Vpc" {
  cidr_block = var.vpc_cidr_block

  tags = merge(
    var.resource_tags,
    {
      Name = "${var.project_prefix}_Vpc"
    }
  )
}

resource "aws_subnet" "Calculator_public_Subnet" {
  vpc_id            = aws_vpc.Calculator_Vpc.id
  cidr_block        = var.subnet_cidr_block
  availability_zone = var.availability_zone

  tags = merge(
    var.resource_tags,
    {
      Name = "${var.project_prefix}_public_Subnet"
    }
  )
}

resource "aws_internet_gateway" "Calculator_igw" {
  vpc_id = aws_vpc.Calculator_Vpc.id

  tags = merge(
    var.resource_tags,
    {
      Name = "${var.project_prefix}_igw"
    }
  )
}

resource "aws_route_table" "route_table_Calculator_public" {
  vpc_id = aws_vpc.Calculator_Vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.Calculator_igw.id
  }

  tags = merge(
    var.resource_tags,
    {
      Name = "${var.project_prefix}_route_table_public"
    }
  )
}

resource "aws_route_table_association" "route_table_association_public" {
  subnet_id      = aws_subnet.Calculator_public_Subnet.id
  route_table_id = aws_route_table.route_table_Calculator_public.id
}