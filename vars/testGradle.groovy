// vars/testGradle.groovy
def call(Map args = [:]) {
  echo "[TestGradle] Testing with Gradle. Args: ${args}"
  sh "./gradlew test"
}
