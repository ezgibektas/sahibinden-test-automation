pipeline {
    agent {
        docker {
            image 'maven:3.9-amazoncorretto-17'
            args '-v $HOME/.m2:/root/.m2'
        }
    }
    
    parameters {
        choice(name: 'TEST_GROUP', choices: ['all', 'desktop', 'mobile'], description: 'Test grubunu seçin')
        string(name: 'THREAD_COUNT', defaultValue: '4', description: 'Paralel koşacak thread sayısı')
    }
    
    stages {
        stage('Hazırlık') {
            steps {
                echo 'Test ortamı hazırlanıyor...'
                sh 'java -version'
                sh 'mvn -version'
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn -B clean compile'
            }
        }
        
        stage('Test') {
            steps {
                script {
                    if (params.TEST_GROUP == 'all') {
                        sh "mvn -B test -Dselenium.use.grid=true -Dselenium.grid.url=http://selenium-hub:4444/wd/hub -Dmaven.test.failure.ignore=true -Dparallel=methods -DthreadCount=${params.THREAD_COUNT}"
                    } else {
                        sh "mvn -B test -Dgroups=${params.TEST_GROUP} -Dselenium.use.grid=true -Dselenium.grid.url=http://selenium-hub:4444/wd/hub -Dmaven.test.failure.ignore=true -Dparallel=methods -DthreadCount=${params.THREAD_COUNT}"
                    }
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'target/screenshots/**', allowEmptyArchive: true
                }
            }
        }
        
        stage('Rapor Oluştur') {
            steps {
                sh 'bash ./create-report.sh'
                archiveArtifacts artifacts: 'target/test-reports/**', allowEmptyArchive: true
            }
        }
    }
    
    post {
        always {
            echo 'Test tamamlandı'
        }
        success {
            echo 'Testler başarıyla tamamlandı!'
        }
        failure {
            echo 'Testlerde hatalar var, raporu kontrol edin!'
        }
    }
}
