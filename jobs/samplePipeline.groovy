pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                script {
                    build()
                }
            }
        }
        stage('Test') {
            steps {
                script {
                    test()
                }
            }
        }
        stage('Deploy') {
            steps {
                script {
                    deploy()
                }
            }
        }
    }
}
