pipeline{
    agent any

    options {
        timeout(time: 10, unit: 'MINUTES')
        timestamps()
    }

    tools {
        maven 'maven-3.6.2'
        jdk 'OpenJDK-8'
    }

    stages {
        stage('Checkout') {
            steps {
                script { 
                    env.CURRENT_STAGE = 'Checkout'
                    echo "Current stage: ${env.CURRENT_STAGE}"
                }
                checkout scm
            }
        }

        stage('Version calculation') {
            steps {
                script {
                    env.CURRENT_STAGE = 'Version calculation'
                    echo "Current stage: ${env.CURRENT_STAGE}"
                    versionCalculation()
                    sh "mvn versions:set -DnewVersion=${env.CALCULATED_VERSION}"
                }
            }
        }

        stage('Build & Test') {
            steps {
                script { 
                    env.CURRENT_STAGE = 'Build & Test'
                    echo "Current stage: ${env.CURRENT_STAGE}"
                }
                sh 'mvn clean verify'
            }
        }

        stage('Build a Docker image') {
            steps {
                script {
                    env.CURRENT_STAGE = 'Build a Docker image'
                    echo "Current stage: ${env.CURRENT_STAGE}"
                }
                sh """
                    docker buildx build --platform linux/amd64 -t calculator-app:${env.CALCULATED_VERSION} .
                """
            }
        }

        stage('Push the Docker image') {
            steps {
                script {
                    env.CURRENT_STAGE = 'Push the Docker image'
                    echo "Current stage: ${env.CURRENT_STAGE}"
                }
            withCredentials([usernamePassword(credentialsId: 'docker-hub',usernameVariable: 'DOCKER_USER',passwordVariable: 'DOCKER_PASS')])
            {
            sh """
                docker tag calculator-app:${env.CALCULATED_VERSION} \$DOCKER_USER/calculator-app:${env.CALCULATED_VERSION}
                echo "\$DOCKER_PASS" | docker login -u "\$DOCKER_USER" --password-stdin
                docker push \$DOCKER_USER/calculator-app:${env.CALCULATED_VERSION}
            """
                }
            }
        }

        stage('Git tagging') {
            steps {
                script {
                    env.CURRENT_STAGE = 'Git tagging'
                    echo "Current stage: ${env.CURRENT_STAGE}"
                    sshagent(credentials: ['github']) {
                        sh """
                            git config user.name "jenkins"
                            git config user.email "jenkins@example.com"
                            git tag -a "${env.CALCULATED_VERSION}" -m "Release ${env.CALCULATED_VERSION}"
                            git push origin "${env.CALCULATED_VERSION}"
                        """
                    }
                }
            }
        }

        stage('Terraform Plan') {
            steps {
                script {
                    env.CURRENT_STAGE = 'Terraform Plan'
                    echo "Current stage: ${env.CURRENT_STAGE}"
                }
                withCredentials([
                    usernamePassword(credentialsId: 'aws-credentials', usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY'),
                    usernamePassword(credentialsId: 'docker-hub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')
                ])
                {
                    sh """
                        cd terraform
                        terraform init
                        terraform plan \
                            -var="docker_image=\$DOCKER_USER/calculator-app" \
                            -var="app_version=${env.CALCULATED_VERSION}" \
                            -out=tfplan
                    """
                }
            }
        }

        // ========================================
        // MANUAL APPROVAL (Optional)
        // Uncomment the stage below to add manual approval before Terraform Apply.
        // Pipeline will pause and wait for user to click:
        //   - "Apply" button -> Continue to next stage
        //   - "Abort" button -> Fail the pipeline and stop
        // ========================================
        // stage('Approve Terraform Apply') {
        //     steps {
        //         input message: 'Review the Terraform plan. Proceed with apply?', ok: 'Apply'
        //     }
        // }

        stage('Terraform Deploy') {
            steps {
                script {
                    env.CURRENT_STAGE = 'Terraform Deploy'
                    echo "Current stage: ${env.CURRENT_STAGE}"
                }
                withCredentials([
                    usernamePassword(credentialsId: 'aws-credentials', usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY')
                ])
                {
                    sh """
                        cd terraform
                        terraform apply -auto-approve tfplan
                    """
                }
            }
        }

        stage('Health Check') {
            steps {
                script {
                    env.CURRENT_STAGE = 'Health Check'
                    echo "Current stage: ${env.CURRENT_STAGE}"
                }
                withCredentials([
                    usernamePassword(credentialsId: 'aws-credentials', usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY')
                ])
                {
                    script {
                        def ec2Ip = sh(
                            script: 'cd terraform && terraform output -raw ec2_public_ip',
                            returnStdout: true
                        ).trim()

                        echo "Testing application on http://${ec2Ip}:8080"

                        try {
                            retry(10) {
                                sleep(time: 30, unit: 'SECONDS')
                                sh "curl -f -s -o /dev/null http://${ec2Ip}:8080"
                            }
                            echo "✅ Application is UP and running on http://${ec2Ip}:8080"
                        } catch (Exception e) {
                            error("❌ Application failed to start after 10 attempts")
                        }
                    }
                }
            }
        }

    }

    post {
        success {
            echo "✅ Build and deploy completed successfully."
        }
        failure {
            echo "❌ Build failed in stage: ${env.CURRENT_STAGE}"
        }
    }
}

def versionCalculation() {
    sshagent(credentials: ['github']) {
        sh "git remote set-url origin ${env.GIT_URL}"
        sh '''git fetch --tags'''
        def latestTag = sh(script: '''git tag | sort -V | tail -n1''', returnStdout: true).trim()
        echo "Latest tag is: ${latestTag}"

        def versionPattern = ~/^v?(\d+)\.(\d+)\.(\d+)$/
        def match = versionPattern.matcher(latestTag)

        if (latestTag && match.matches()) {
            echo "Incrementing patch version"
            def major = match.group(1).toInteger()
            def minor = match.group(2).toInteger()
            def patch = match.group(3).toInteger() + 1
            env.CALCULATED_VERSION = "v${major}.${minor}.${patch}"
        } else {
            echo "No existing tags or unrecognized format. Setting version to v1.0.0"
            env.CALCULATED_VERSION = "v1.0.0"
        }

        echo "Calculated version: ${env.CALCULATED_VERSION}"
    }
}
