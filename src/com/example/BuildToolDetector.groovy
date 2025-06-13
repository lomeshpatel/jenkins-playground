package com.example

class BuildToolDetector {
  /**
   * Determines the BuildToolType based on the presence of characteristic build files.
   * @param hasGradle True if Gradle build files (build.gradle or build.gradle.kts) exist.
   * @param hasMaven True if a pom.xml file exists.
   * @param hasNpm True if a package.json file exists.
   * @return BuildToolType enum value.
   */
  static BuildToolType determineBuildTool(boolean hasGradle, boolean hasMaven, boolean hasNpm) {
    // Using script.echo for logging in library classes is not typical if 'script' object isn't passed.
    // For a utility class like this, standard System.out.println might be used for local debugging,
    // but it won't show in Jenkins logs. Pipeline steps (like 'echo') are preferred for Jenkins output.
    // If logging from here is needed in Jenkins, the 'script' object would have to be passed.
    // For now, this method is pure logic based on boolean inputs.
    // Example for local debugging (won't show in Jenkins):
    // System.out.println("[BuildToolDetector] Determining tool: Gradle=${hasGradle}, Maven=${hasMaven}, NPM=${hasNpm}")

    if (hasGradle) {
      return BuildToolType.GRADLE
    } else if (hasMaven) {
      return BuildToolType.MAVEN
    } else if (hasNpm) {
      return BuildToolType.NPM
    } else {
      return BuildToolType.UNKNOWN
    }
  }
}
