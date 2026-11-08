pipeline {
    agent any
    tools {
        jdk 'jdk-lab'
    }
    options {
        skipDefaultCheckout(true)
        timeout(time: 20, unit: 'MINUTES')
    }
    environment {
        SONAR_PROJECT_KEY = 'orders-gradle-lab'
    }
    stages {
        stage('Checkout') {
            steps { checkout scm }
        }
        stage('Build, tests, coverage') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'chmod +x gradlew && ./gradlew clean test jacocoTestReport --console=plain'
                    } else {
                        bat 'gradlew.bat clean test jacocoTestReport --console=plain'
                    }
                }
            }
            post {
                always { junit allowEmptyResults: true, testResults: 'build/test-results/test/*.xml' }
            }
        }
        stage('Submit Sonar analysis') {
            steps {
                withSonarQubeEnv('Sonar Lab') {
                    withCredentials([string(credentialsId: 'sonar-lab-token', variable: 'SONAR_TOKEN')]) {
                        script {
                            if (isUnix()) {
                                sh './gradlew sonar --console=plain'
                            } else {
                                bat 'gradlew.bat sonar --console=plain'
                            }
                        }
                    }
                }
            }
        }
        stage('Wait for quality gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        stage('Ready to release') {
            steps { echo 'Quality gate passed. A real deployment could follow here.' }
        }
    }
}
