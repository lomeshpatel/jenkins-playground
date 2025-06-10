// sample-app/pactConfig.groovy
// Example configuration for Pact publishing.
// Values here can be overridden by direct parameters passed to the publishPactContracts function
// or by other dynamic fallbacks (like Jenkins environment variables).

config = [
  // --- Connection to Pact Broker ---
  // It's often better to configure these directly in the Jenkinsfile or Jenkins job configuration
  // for clarity and security, especially the brokerTokenCredentialId.
  // brokerBaseUrl: 'https://your-company.pactflow.io', // Example Pactflow or self-hosted Pact Broker URL
  // brokerTokenCredentialId: 'pact-broker-credential-id-from-jenkins', // ID of the Jenkins secret text credential for the broker token

  // --- Application Identification ---
  // Defines the name of the consumer application.
  applicationName: 'SampleAppFromConfig', // Overrides dynamic determination (e.g., from JOB_NAME)

  // Defines the version of the consumer application.
  // version: '1.0.0-config', // Overrides dynamic determination (e.g., from BUILD_NUMBER)

  // --- Pact Files Location ---
  // Specifies the directory where Pact contract files (.json) are located.
  // This path should be relative to the root of the Jenkins workspace.
  pactFilesDir: 'pacts', // Default in publishPactContracts.groovy is also 'pacts' if not specified here or as a parameter.

  // --- Tagging ---
  // A list or comma-separated string of tags to apply to this version of the pacticipant in the Pact Broker.
  // Useful for identifying branches, environments, etc.
  tags: ['sample-app-tag', 'dev-config'] // Example: ['dev', 'feature-xyz'] or 'dev,feature-xyz'
]

return config
