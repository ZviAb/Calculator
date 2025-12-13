pipeline{
    agent any

    options {
        timeout(time: 10, unit: 'MINUTES')
        timestamps()
    }

    environment {
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

        stage('Build & Test') {
            steps {
                script { 
                    env.CURRENT_STAGE = 'Build & Test'
                    echo "Current stage: ${env.CURRENT_STAGE}"
                }
                sh 'mvn clean verify'
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
    sshagent(credentials: ['gitlabssh']) {
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
