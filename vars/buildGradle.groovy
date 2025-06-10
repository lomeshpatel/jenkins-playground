// vars/buildGradle.groovy
def call(Map args = [:]) {
  echo "[BuildGradle] Building with Gradle. Args: ${args}"
  // Assuming Gradle wrapper is in the root of the workspaceDir passed to standardPipeline
  sh "./gradlew build"
}
