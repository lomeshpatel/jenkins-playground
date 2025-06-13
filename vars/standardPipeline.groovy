// vars/standardPipeline.groovy
import com.example.BuildToolType
import com.example.BuildToolDetector // Still need this for the class name

def call(Map config = [:]) {
  String appWorkspace = config.workspaceDir ?: '.'
  BuildToolType buildTool

  echo "[StandardPipeline] Starting standard pipeline configured with: ${config}"
  echo "[StandardPipeline] Application workspace (for build tool detection and execution): ${appWorkspace}"

  pipeline {
    agent any
    options { timestamps() }
    // environment { /* ... */ } // Keep environment if needed

    stages {
      stage('Initialize') {
        steps {
          script {
            echo "[StandardPipeline] Initializing pipeline and detecting build tool in '${appWorkspace}'..."
            dir(appWorkspace) { // Set directory context to appWorkspace
              boolean gradleExists = fileExists('build.gradle') || fileExists('build.gradle.kts')
              boolean mavenExists = fileExists('pom.xml')
              boolean npmExists = fileExists('package.json')

              echo "[StandardPipeline] File checks: build.gradle(.kts)?: ${gradleExists}, pom.xml?: ${mavenExists}, package.json?: ${npmExists}"

              // Call the static method from BuildToolDetector class
              buildTool = com.example.BuildToolDetector.determineBuildTool(gradleExists, mavenExists, npmExists)
            } // End dir block

            echo "[StandardPipeline] Detected build tool: ${buildTool}"
            if (buildTool == BuildToolType.UNKNOWN) {
              error "[StandardPipeline] Aborting: Unknown build tool. Could not find build.gradle, pom.xml, or package.json in '${appWorkspace}'."
            }
          }
        }
      }

      stage('Build') {
        steps {
          script {
            echo "[StandardPipeline] Executing Build stage for ${buildTool} in '${appWorkspace}'..."
            dir(appWorkspace) {
              switch (buildTool) {
                case BuildToolType.GRADLE:
                  buildGradle(config)
                  break
                case BuildToolType.MAVEN:
                  buildMaven(config)
                  break
                case BuildToolType.NPM:
                  buildNpm(config)
                  break
                default:
                  error "[StandardPipeline] Build stage: Should not happen - Unknown build tool: ${buildTool}"
              }
            }
          }
        }
      }

      stage('Test') {
        steps {
          script {
            echo "[StandardPipeline] Executing Test stage for ${buildTool} in '${appWorkspace}'..."
            dir(appWorkspace) {
              switch (buildTool) {
                case BuildToolType.GRADLE:
                  testGradle(config)
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
          expression { return config.pactBrokerTokenCredentialId }
        }
        steps {
          script {
            echo "[StandardPipeline] Preparing for Pact Publishing from '${appWorkspace}'..."
            def pactParams = [
              brokerTokenCredentialId: config.pactBrokerTokenCredentialId,
              workspaceDir: appWorkspace
            ]
            publishPactContracts(pactParams)
          }
        }
      }

      stage('Deploy') {
        steps {
          script {
            echo "[StandardPipeline] Preparing for Deployment of ${buildTool} from '${appWorkspace}'..."
            dir(appWorkspace) {
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
    } // End stages

    post {
      always {
        echo "[StandardPipeline] Pipeline finished."
      }
    }
  } // End pipeline
} // End call
