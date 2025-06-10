// vars/standardPipeline.groovy
import com.example.BuildToolType
import com.example.BuildToolDetector

def call(Map config = [:]) {
  // Define default workspace directory if not provided in config
  // This path is relative to the Jenkins job's workspace root.
  // It's the directory where the application code (and its build files) reside.
  String appWorkspace = config.workspaceDir ?: '.'

  echo "[StandardPipeline] Starting standard pipeline configured with: ${config}"
  echo "[StandardPipeline] Application workspace (for build tool detection and execution): ${appWorkspace}"

  // Detect build tool
  BuildToolType buildTool = BuildToolDetector.detect(this, appWorkspace)
  echo "[StandardPipeline] Detected build tool: ${buildTool}"

  pipeline {
    agent any // Or could be configured via `config` map

    options {
      // Example: timestamps()
      // Could also take options from `config`
      timestamps()
    }

    environment {
      // Example: PASS_PACT_BROKER_TOKEN = credentials(config.pactBrokerTokenCredentialId ?: '')
      // This needs to be more dynamic based on whether pactBrokerTokenCredentialId is actually set.
    }

    stages {
      stage('Initialize') {
        steps {
          script {
            echo "[StandardPipeline] Initializing pipeline..."
            // Could add checkout step here if SCM is not handled by the job definition itself
            // Or if we need to checkout multiple repos. For now, assume main app repo is checked out.
            if (buildTool == BuildToolType.UNKNOWN) {
              error "[StandardPipeline] Aborting: Unknown build tool. Could not find build.gradle, pom.xml, or package.json in '${appWorkspace}'."
            }
          }
        }
      }

      stage('Build') {
        steps {
          script {
            // The tool-specific scripts will be executed within the appWorkspace context
            dir(appWorkspace) {
              switch (buildTool) {
                case BuildToolType.GRADLE:
                  buildGradle(config) // Pass along any relevant config
                  break
                case BuildToolType.MAVEN:
                  buildMaven(config)
                  break
                case BuildToolType.NPM:
                  buildNpm(config)
                  break
                default:
                  // This case should ideally be caught by the Initialize stage
                  error "[StandardPipeline] Build stage: Should not happen - Unknown build tool: ${buildTool}"
              }
            }
          }
        }
      }

      stage('Test') {
        steps {
          script {
            dir(appWorkspace) {
              switch (buildTool) {
                case BuildToolType.GRADLE:
                  testGradle(config) // Pass along any relevant config
                  break
                case BuildToolType.MAVEN:
                  testMaven(config)
                  break
                case BuildToolType.NPM:
                  testNpm(config)
                  break
                default:
                  error "[StandardPipeline] Test stage: Should not happen - Unknown build tool: ${buildTool}"
              }
            }
          }
        }
      }

      stage('Pact Publish') {
        when {
          // Only run if pactBrokerTokenCredentialId is provided in the config
          expression { return config.pactBrokerTokenCredentialId }
        }
        steps {
          script {
            echo "[StandardPipeline] Preparing for Pact Publishing..."
            // The `publishPactContracts` function expects its `workspaceDir` parameter
            // to point to the directory containing `pactConfig.groovy`.
            // `appWorkspace` (derived from `config.workspaceDir` in Jenkinsfile) is this directory.
            def pactParams = [
              brokerTokenCredentialId: config.pactBrokerTokenCredentialId,
              workspaceDir: appWorkspace // appWorkspace is where pactConfig.groovy is expected
            ]
            // Other parameters (applicationName, version, brokerBaseUrl, tags, pactFilesDir)
            // will be resolved by publishPactContracts from pactParams, pactConfig.groovy, or dynamic fallbacks.
            publishPactContracts(pactParams)
          }
        }
      }

      stage('Deploy') {
        // Example: Only deploy on main branch, if such a condition is passed or detectable
        // when { expression { return env.BRANCH_NAME == 'main' } }
        steps {
          script {
            echo "[StandardPipeline] Preparing for Deployment..."
            dir(appWorkspace) { // Ensure execution in the application's workspace
              switch (buildTool) {
                case BuildToolType.GRADLE:
                  deployGradle(config)
                  break
                case BuildToolType.MAVEN:
                  deployMaven(config)
                  break
                case BuildToolType.NPM:
                  deployNpm(config)
                  break
                default:
                  error "[StandardPipeline] Deploy stage: Unknown build tool: ${buildTool}"
              }
            }
          }
        }
      }
    }

    post {
      always {
        echo "[StandardPipeline] Pipeline finished."
        // Example: cleanWs()
      }
      // Other post conditions (success, failure, etc.) can be added here
    }
  }
}
