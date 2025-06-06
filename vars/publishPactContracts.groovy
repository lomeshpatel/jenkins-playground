def call(Map params) {
    echo "Starting Pact contract publishing for app: ${params.applicationName}, version: ${params.version}"

    // Validate required parameters
    if (!params.brokerBaseUrl || !params.brokerTokenCredentialId || !params.applicationName || !params.version || !params.pactFilesDir) {
        error "ERROR: Missing required parameters for publishPactContracts. Must include: brokerBaseUrl, brokerTokenCredentialId, applicationName, version, pactFilesDir"
    }

    withCredentials([string(credentialsId: params.brokerTokenCredentialId, variable: 'PACT_BROKER_TOKEN')]) {
        // Construct the pact-broker publish command
        def command = "pact-broker publish ${params.pactFilesDir}"
        command += " --consumer-app-version ${params.version}"
        command += " --broker-base-url ${params.brokerBaseUrl}"
        command += " --broker-token \$PACT_BROKER_TOKEN" // Use the environment variable

        // Add tags if provided
        if (params.tags && !params.tags.isEmpty()) {
            command += " --tag '${params.tags}'" // Enclose tags in single quotes if they might contain spaces or special chars handled by shell
        }

        echo "Publishing Pact contracts with command: ${command}" // Log the command without the token for security

        try {
            // Check if pact-broker CLI is available (optional, but good practice)
            // sh "command -v pact-broker >/dev/null 2>&1 || { echo 'ERROR: pact-broker CLI not found or not in PATH.'; exit 1; }"

            sh command
            echo "Pact contract publishing completed successfully for ${params.applicationName} version ${params.version}."
        } catch (Exception e) {
            error "ERROR: Pact contract publishing failed for ${params.applicationName} version ${params.version}. Details: ${e.getMessage()}"
        }
    }
}
