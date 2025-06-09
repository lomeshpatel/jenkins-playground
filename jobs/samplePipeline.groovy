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
                                    echo "[Pipeline] Pact Broker URL, App Name, Version, Pact Dir, and Tags will primarily be sourced from pactConfig.groovy (in sample-app directory), direct parameters to publishPactContracts, or dynamic fallbacks."
                                    echo "[Pipeline] The 'workspaceDir' parameter for publishPactContracts defaults to '.', so pactConfig.groovy should be in the root of the 'sample-app' directory if you follow this example structure and call from pipeline root."

                                    // The publishPactContracts function will look for 'sample-app/pactConfig.groovy'
                                    // if workspaceDir is not specified and the pipeline runs from the repo root.
                                    // Or, explicitly set workspaceDir: params.workspaceDir = 'sample-app' if needed.
                                    publishPactContracts(
                                        brokerTokenCredentialId: 'pact-broker-token', // REQUIRED: Jenkins credential ID for Pact Broker token. This is best set directly.
                                        workspaceDir: 'sample-app', // Explicitly set workspace for clarity, assuming pactConfig.groovy is in sample-app
                                        // brokerBaseUrl: 'https://your-pact-broker.example.com', // Explicitly set here to override pactConfig.groovy or if not in config
                                        // applicationName: 'sample-app-override', // Override value from pactConfig.groovy
                                        // version: '1.2.3-pipeline', // Override value from pactConfig.groovy or env.BUILD_NUMBER
                                        // pactFilesDir: 'sample-app/custom-pacts', // Override value from pactConfig.groovy
                                        // tags: 'pipeline-tag' // Override value from pactConfig.groovy
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
