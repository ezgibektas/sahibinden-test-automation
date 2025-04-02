pipeline {
    agent any
    
    tools {
        jdk 'JDK 17'
        maven 'Maven 3.9.9'
    }
    
    stages {
        stage('Build') {
            steps {
                echo 'Building..'
                sh 'java -version'
                sh 'mvn -version'
            }
        }
        stage('Test') {
            steps {
                echo 'Testing..'
                sh 'mvn clean test'
            }
        }
    }
}
