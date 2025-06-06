pipelineJob('sample-pipeline') {
    definition {
        cps {
            script('''\
                @Library('my-shared-library') _

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
            '''.stripIndent())
            sandbox(true) // Run the pipeline script in a sandbox
        }
    }
}
