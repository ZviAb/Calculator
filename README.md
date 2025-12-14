# Calculator Application - CI/CD Pipeline

A Spring Boot calculator application with a complete CI/CD pipeline using Jenkins, Docker, and Terraform for AWS deployment.

## Table of Contents
- [Prerequisites](#prerequisites)
- [Jenkins Setup](#jenkins-setup)
- [Required Credentials](#required-credentials)
- [Pipeline Stages](#pipeline-stages)
- [Manual Deployment Options](#manual-deployment-options)
- [Project Structure](#project-structure)

## Prerequisites

### Required Software
- **Jenkins** (2.x or higher)
  - Maven plugin
  - Docker plugin
  - Git plugin
- **Maven** 3.6.2 or higher
- **JDK** OpenJDK 8 or higher
- **Docker** (for building images)
- **Terraform** (latest version)
- **AWS CLI** (for manual deployments)
- **Git**

### AWS Account
- Active AWS account with permissions to create:
  - EC2 instances
  - VPC and networking resources
  - S3 buckets (for Terraform state)

## Jenkins Setup

### 1. Install Required Plugins
Install the following Jenkins plugins:
- Pipeline
- Git
- Docker Pipeline
- Maven Integration
- Credentials Binding

### 2. Configure Tools in Jenkins
Navigate to `Manage Jenkins` → `Global Tool Configuration`:

**Maven Configuration:**
- Name: `maven-3.6.2`
- Version: 3.6.2 or higher

**JDK Configuration:**
- Name: `OpenJDK-8`
- Version: OpenJDK 8 or higher

## Required Credentials

Configure the following credentials in Jenkins (`Manage Jenkins` → `Credentials`):

### 1. Docker Hub Credentials
- **Type:** Username with password
- **ID:** `docker-hub`
- **Username:** Your Docker Hub username
- **Password:** Your Docker Hub password or access token
- **Description:** Docker Hub credentials for pushing images

### 2. AWS Credentials
- **Type:** Username with password
- **ID:** `aws-credentials`
- **Username:** Your AWS Access Key ID
- **Password:** Your AWS Secret Access Key
- **Description:** AWS credentials for Terraform

### 3. GitHub Credentials (SSH)
- **Type:** SSH Username with private key
- **ID:** `github`
- **Username:** git
- **Private Key:** Your SSH private key for GitHub
- **Description:** GitHub SSH credentials for git operations

## Pipeline Stages

The Jenkins pipeline executes the following stages:

### 1. Checkout
Clones the repository from Git.

### 2. Version Calculation
Automatically calculates the next semantic version based on Git tags.
- Fetches existing tags
- Increments patch version
- Sets version to `v1.0.0` if no tags exist

### 3. Build & Test
Runs Maven build and tests:
```bash
mvn clean verify
```

### 4. Build Docker Image
Builds a Docker image for linux/amd64 platform:
```bash
docker buildx build --platform linux/amd64 -t calculator-app:${VERSION} .
```

### 5. Push Docker Image
Tags and pushes the image to Docker Hub:
```bash
docker push ${DOCKER_USER}/calculator-app:${VERSION}
```

### 6. Git Tagging
Creates and pushes a Git tag with the calculated version.

### 7. Terraform Plan
Initializes Terraform and creates an execution plan:
```bash
terraform init
terraform plan -var="docker_image=${DOCKER_USER}/calculator-app" -var="app_version=${VERSION}" -out=tfplan
```

### 8. Terraform Deploy
Applies the Terraform plan to deploy infrastructure on AWS.

### 9. Health Check
Verifies the application is running by making HTTP requests to the deployed EC2 instance.
- Retries: 10 attempts
- Interval: 30 seconds

## Manual Deployment Options

### Option 1: Build and Run Locally

#### Build with Maven
```bash
mvn clean package
```

#### Run the JAR
```bash
java -jar target/calculator-1.0-SNAPSHOT.jar
```

Access the application at: `http://localhost:8080`

### Option 2: Run with Docker Only

You can use the pre-built Docker image available at: [Docker Hub - zviabramovich/calculator-app](https://hub.docker.com/repository/docker/zviabramovich/calculator-app/tags)

#### Pull the Docker Image
```bash
docker pull zviabramovich/calculator-app:latest
```

#### Run the Container
```bash
docker run -d -p 8080:8080 --name calculator zviabramovich/calculator-app:latest
```

Access the application at: `http://localhost:8080`

#### Run a Specific Version
```bash
# Pull a specific version
docker pull zviabramovich/calculator-app:v1.0.1

# Run the specific version
docker run -d -p 8080:8080 --name calculator zviabramovich/calculator-app:v1.0.1
```

### Option 3: Deploy Infrastructure with Terraform Only

#### Prerequisites
1. Configure AWS credentials:
```bash
export AWS_ACCESS_KEY_ID="your-access-key"
export AWS_SECRET_ACCESS_KEY="your-secret-key"
export AWS_DEFAULT_REGION="ap-south-1"
```

2. Update Terraform variables in `terraform/variables.tf` or create a `terraform.tfvars` file:
```hcl
docker_image = "zviabramovich/calculator-app"
app_version  = "v1.0.1"  # Use any available version from Docker Hub
```

**Available Docker images:** [zviabramovich/calculator-app tags](https://hub.docker.com/repository/docker/zviabramovich/calculator-app/tags)

#### Deploy
```bash
cd terraform

# Initialize Terraform
terraform init

# Plan the deployment
terraform plan

# Apply the changes
terraform apply
```

#### Get the EC2 Public IP
```bash
terraform output ec2_public_ip
```

Access the application at: `http://<ec2-public-ip>:8080`

#### Destroy Infrastructure
```bash
terraform destroy
```


## Project Structure

```
Calculator/
├── src/                          # Java source code
│   ├── main/
│   │   ├── java/                 # Application code
│   │   └── resources/            # Configuration files
│   └── test/                     # Test files
├── terraform/                    # Infrastructure as Code
│   ├── modules/
│   │   ├── network/             # VPC, subnet configurations
│   │   └── compute/             # EC2, security groups
│   ├── main.tf                  # Main Terraform configuration
│   ├── variables.tf             # Variable definitions
│   ├── outputs.tf               # Output definitions
│   └── user-data.sh             # EC2 initialization script
├── Dockerfile                    # Docker image definition
├── Jenkinsfile                  # CI/CD pipeline definition
├── pom.xml                      # Maven configuration
└── README.md                    # This file
```

## Terraform Backend Configuration

The project uses S3 for remote state storage with native locking.

### Setup S3 Backend (Optional)

1. Create an S3 bucket:
```bash
aws s3 mb s3://your-terraform-state-bucket
aws s3api put-bucket-versioning --bucket your-terraform-state-bucket --versioning-configuration Status=Enabled
```

2. Uncomment the backend configuration in `terraform/main.tf` and update the bucket name.

3. Initialize with state migration:
```bash
terraform init -migrate-state
```

## Manual Approval for Terraform Apply

To enable manual approval before Terraform applies changes:

1. Open `Jenkinsfile`
2. Uncomment the "Approve Terraform Apply" stage (lines 122-126)
3. The pipeline will pause and wait for user confirmation before applying changes

## Environment Variables

The pipeline uses the following environment variables:

- `CALCULATED_VERSION`: Automatically calculated semantic version
- `DOCKER_USER`: Docker Hub username (from credentials)
- `AWS_ACCESS_KEY_ID`: AWS access key (from credentials)
- `AWS_SECRET_ACCESS_KEY`: AWS secret key (from credentials)

## Troubleshooting

### Pipeline fails at "Version calculation"
- Ensure GitHub SSH credentials are configured correctly
- Verify Git repository has at least one commit

### Docker build fails
- Check Docker daemon is running
- Verify Dockerfile syntax

### Terraform fails with credentials error
- Verify AWS credentials are configured in Jenkins
- Check IAM permissions for the AWS user

### Health check fails
- Verify security group allows traffic on port 8080
- Check EC2 instance is running
- Verify Docker container started successfully on EC2

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

