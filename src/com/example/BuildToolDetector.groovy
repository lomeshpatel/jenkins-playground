package com.example

class BuildToolDetector {
  /**
   * Detects the build tool used in the specified workspace path.
   * @param script The Jenkins pipeline script object, needed for file operations.
   * @param workspacePath The path within the Jenkins workspace to check for build files.
   * @return BuildToolType enum value.
   */
  static BuildToolType detect(def script, String workspacePath = '.') {
    script.echo "[BuildToolDetector] Checking for build tool in workspace path: ${workspacePath}"
    // Ensure workspacePath is handled correctly if it's not just "."
    // fileExists expects paths relative to the overall workspace root if not prepended by workspacePath
    // However, 'script.dir(workspacePath)' changes context for fileExists.

    def buildTool = BuildToolType.UNKNOWN
    script.dir(workspacePath) { // Change context to the specified workspace path
      if (script.fileExists('build.gradle') || script.fileExists('build.gradle.kts')) {
        script.echo "[BuildToolDetector] Found Gradle build file."
        buildTool = BuildToolType.GRADLE
      } else if (script.fileExists('pom.xml')) {
        script.echo "[BuildToolDetector] Found Maven build file."
        buildTool = BuildToolType.MAVEN
      } else if (script.fileExists('package.json')) {
        script.echo "[BuildToolDetector] Found NPM build file (package.json)."
        buildTool = BuildToolType.NPM
      } else {
        script.echo "[BuildToolDetector] No recognized build tool detected."
      }
    }
    return buildTool
  }
}
