import com.example.ConfigUtils

def call(Map params) {
    echo "[publishPactContracts] Starting Pact contract publishing process."

    // Define workspace directory, default to current directory if not provided
    String workspaceDir = params.workspaceDir ?: '.'

    // Load external configuration from pactConfig.groovy if it exists
    // 'this' refers to the current script object (Jenkins pipeline context)
    Map pactFileConfig = ConfigUtils.loadPactConfig(this, workspaceDir)

    // Resolve parameters: Direct params > pactFileConfig > Dynamic fallbacks
    String appName = params.applicationName ?: pactFileConfig.applicationName ?: env.JOB_NAME
    String appVersion = params.version ?: pactFileConfig.version ?: env.BUILD_NUMBER ?: "0.0.0-SNAPSHOT"
    // pactFilesDir from config should be relative to workspaceDir, or an absolute path.
    // If pactFilesDir is relative, it will be resolved correctly by sh step assuming workspaceDir is current dir.
    String pactDir = params.pactFilesDir ?: pactFileConfig.pactFilesDir ?: 'pacts'
    String brokerUrl = params.brokerBaseUrl ?: pactFileConfig.brokerBaseUrl
    String tokenCredId = params.brokerTokenCredentialId ?: pactFileConfig.brokerTokenCredentialId // Allow token ID from config file

    def rawTags = params.tags ?: pactFileConfig.tags
    String tagsString = ""
    if (rawTags) {
        if (rawTags instanceof List) {
            tagsString = rawTags.join(',')
        } else {
            tagsString = rawTags.toString()
        }
    }

    // --- Mandatory Parameter Validation ---
    if (!tokenCredId) {
        error "[publishPactContracts] ERROR: brokerTokenCredentialId parameter is required (either direct or in pactConfig.groovy)."
    }
    if (!brokerUrl) {
        error "[publishPactContracts] ERROR: Pact Broker URL (brokerBaseUrl) must be provided (either direct or in pactConfig.groovy)."
    }
    if (!appName) {
        // Should ideally not happen if JOB_NAME is always available as a fallback
        error "[publishPactContracts] ERROR: Application name (applicationName) must be provided or derivable."
    }
     if (!pactDir) {
        // Should not happen due to default 'pacts'
        error "[publishPactContracts] ERROR: Pact files directory (pactFilesDir) must be provided or have a default."
    }


    echo "[publishPactContracts] Effective settings: AppName='${appName}', Version='${appVersion}', PactDir='${pactDir}', BrokerURL='${brokerUrl}', Tags='${tagsString ?: 'none'}'"

    withCredentials([string(credentialsId: tokenCredId, variable: 'PACT_BROKER_TOKEN')]) {
        // Construct the pact-broker publish command using resolved variables
        // Ensure pactDir is treated as relative to the workspace by the sh step
        def command = "pact-broker publish \"${pactDir}\"" // Quote pactDir in case it contains spaces
        command += " --consumer-app-version \"${appVersion}\"" // Quote version
        command += " --broker-base-url \"${brokerUrl}\"" // Quote URL
        command += " --broker-token \$PACT_BROKER_TOKEN"

        if (tagsString && !tagsString.isEmpty()) {
            command += " --tag \"${tagsString}\"" // Quote tags
        }

        // Log the command without the token for security (already done by echo above for effective settings)
        echo "[publishPactContracts] Executing command..." // Token will not be printed by sh step by default

        try {
            // It's good practice to ensure the CLI is installed.
            // A user might add: sh "command -v pact-broker >/dev/null 2>&1 || { echo 'ERROR: pact-broker CLI not found.'; exit 1; }"

            sh command
            echo "[publishPactContracts] Pact contract publishing completed successfully for ${appName} version ${appVersion}."
        } catch (Exception e) {
            error "[publishPactContracts] ERROR: Pact contract publishing failed for ${appName} version ${appVersion}. Details: ${e.getMessage()}"
        }
    }
}
