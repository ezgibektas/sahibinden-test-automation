pipeline {
    agent any

    environment {
        SELENOID_URL = 'http://selenoid:4444/wd/hub'
        MAVEN_HOME = tool 'Maven 3.8.4'
        JAVA_HOME = tool 'JDK 17'
        PATH = "${MAVEN_HOME}/bin:${JAVA_HOME}/bin:${env.PATH}"
    }

    tools {
        maven 'Maven 3.8.6'
        jdk 'JDK 17'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test -Dspring.profiles.active=test,responsive-test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Generate Report') {
            steps {
                allure([
                    includeProperties: false,
                    jdk: '',
                    properties: [],
                    reportBuildPolicy: 'ALWAYS',
                    results: [[path: 'target/allure-results']]
                ])
            }
        }

        stage('Archive Results') {
            steps {
                archiveArtifacts artifacts: 'target/allure-results/**', fingerprint: true
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
} 
pipeline {
    agent any
    
    environment {
        SELENOID_URL = 'http://selenoid:4444/wd/hub'
        MAVEN_HOME = tool 'Maven 3.8.4'
        JAVA_HOME = tool 'JDK 17'
        PATH = "${MAVEN_HOME}/bin:${JAVA_HOME}/bin:${env.PATH}"
    }
    
    tools {
        maven 'Maven 3.8.6'
        jdk 'JDK 17'
    }
    
    stages {
        stage('Build') {
            steps {
                echo 'Building..'
                sh 'mvn clean install -DskipTests'
            }
        }
        stage('Test') {
            steps {
                echo 'Testing..'
                sh 'mvn test'
            }
        }
    }
}
