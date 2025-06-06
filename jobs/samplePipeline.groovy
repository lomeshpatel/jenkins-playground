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
                        stage('Pact Publish') {
                            steps {
                                script {
                                    // NOTE: Ensure 'pact-broker-token' Jenkins credential exists
                                    // and 'brokerBaseUrl' points to your Pact Broker instance.
                                    publishPactContracts(
                                        brokerBaseUrl: 'https://your-pact-broker.example.com', // TODO: Configure this URL
                                        brokerTokenCredentialId: 'pact-broker-token', // TODO: Configure this Jenkins credential ID
                                        applicationName: 'sample-app', // Example application name
                                        version: env.BUILD_NUMBER ?: '0.0.0-SNAPSHOT', // Uses Jenkins build number or a default
                                        pactFilesDir: 'sample-app/pacts', // Directory containing pact files
                                        tags: 'dev, main' // Example tags, adjust as needed
                                    )
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
