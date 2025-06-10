package com.example

class ConfigUtils {
  /**
   * Loads Pact configuration from pactConfig.groovy in the specified workspace path.
   * @param script The Jenkins pipeline script object, needed for file operations.
   * @param workspacePath The path to the workspace root. Defaults to '.'
   * @return A Map of the configuration, or an empty Map if not found or error.
   */
  static Map loadPactConfig(def script, String workspacePath = '.') {
    def configFile = "${workspacePath}/pactConfig.groovy"
    def config = [:]

    if (script.fileExists(configFile)) {
      try {
        // The loaded groovy script should return a Map
        def loadedConfig = script.load(configFile)
        if (loadedConfig instanceof Map) {
          config = loadedConfig
          script.echo "[ConfigUtils] Loaded Pact configuration from ${configFile}"
        } else {
          script.echo "[ConfigUtils] WARNING: ${configFile} did not return a Map. Ignoring."
        }
      } catch (Exception e) {
        script.echo "[ConfigUtils] WARNING: Could not load or parse ${configFile}. Error: ${e.getMessage()}"
      }
    } else {
      script.echo "[ConfigUtils] INFO: Configuration file ${configFile} not found. Using defaults/parameters."
    }
    return config
  }
}
