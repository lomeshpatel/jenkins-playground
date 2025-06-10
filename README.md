# Jenkins Shared Library and Standardized Pipeline Framework

## Overview

This project provides a robust framework for standardizing Jenkins CI/CD pipelines using a combination of a **Job DSL seed job**, a **Shared Library**, and a convention of placing a minimal `Jenkinsfile` in each application repository.

The goal is to enable rapid onboarding of new applications with pre-defined, best-practice pipeline stages, while still allowing for customization.

## Core Architecture

The framework operates on the following principles:

1.  **Job DSL for Pipeline Generation**:
    *   A seed job in Jenkins (configured via `jobs/samplePipeline.groovy` in this repository) is responsible for discovering application repositories or being manually configured to create pipeline jobs in Jenkins.
    *   Instead of defining the full pipeline logic in Job DSL, it configures the generated Jenkins job to fetch its pipeline definition from an SCM (Source Code Management) source, specifically a `Jenkinsfile` within the application's repository.

2.  **Application Repository `Jenkinsfile`**:
    *   Each application repository that needs a CI/CD pipeline will contain a very minimal `Jenkinsfile`.
    *   This `Jenkinsfile` primarily does two things:
        1.  Imports this shared library (e.g., `@Library('my-shared-library@main') _`).
        2.  Calls the main entry point function from the shared library, `standardPipeline(pipelineConfig)`, passing a configuration map.

3.  **Shared Library (`standardPipeline`)**:
    *   The `vars/standardPipeline.groovy` script is the heart of the pipeline logic.
    *   It receives the `pipelineConfig` map from the application's `Jenkinsfile`.
    *   It dynamically detects the build tool (Gradle, Maven, NPM) used by the application.
    *   It orchestrates a sequence of standard stages: Initialize, Build, Test, Pact Publish (optional), and Deploy.
    *   It calls other tool-specific scripts (e.g., `buildGradle.groovy`, `testMaven.groovy`) from the `vars/` directory to execute actual commands.

4.  **Tool-Specific Logic**:
    *   Scripts in `vars/` like `buildGradle.groovy`, `testGradle.groovy`, `deployGradle.groovy`, etc., encapsulate the commands for specific build tools. These can be customized and extended.

5.  **Configuration (`pipelineConfig` and `pactConfig.groovy`)**:
    *   Global pipeline behavior is controlled by `pipelineConfig` in the application's `Jenkinsfile`.
    *   Pact publishing specific settings can be further customized via a `pactConfig.groovy` file within the application's directory.

## Directory Structure

```
.
├── README.md               # This file
├── jobs/                   # Contains Job DSL scripts
│   └── samplePipeline.groovy # Defines how Jenkins jobs are created (points to app Jenkinsfile)
├── sample-app/             # A sample Gradle project demonstrating usage
│   ├── Jenkinsfile         # Minimal Jenkinsfile calling standardPipeline
│   ├── build.gradle        # Gradle build script for the sample app
│   ├── pactConfig.groovy   # Example external config for Pact publishing
│   ├── pacts/              # Placeholder Pact contracts
│   │   └── myconsumer-myprovider.json
│   └── src/                # Sample Java application source
├── src/                    # Utility classes for the shared library
│   └── com/example/
│       ├── BuildToolDetector.groovy # Detects project's build tool
│       ├── BuildToolType.groovy     # Enum for build tool types
│       └── ConfigUtils.groovy       # Utility to load .groovy config files
└── vars/                   # Global functions and pipeline definitions for the shared library
    ├── buildGradle.groovy, testGradle.groovy, deployGradle.groovy # Gradle steps
    ├── buildMaven.groovy, testMaven.groovy, deployMaven.groovy   # Maven placeholder steps
    ├── buildNpm.groovy, testNpm.groovy, deployNpm.groovy       # NPM placeholder steps
    ├── publishPactContracts.groovy # Function for Pact contract publishing
    └── standardPipeline.groovy     # Main pipeline orchestration logic
```

## Shared Library Components

### 1. `vars/standardPipeline.groovy`
This is the main entry point for pipelines defined in application `Jenkinsfile`s.
-   **Signature**: `def call(Map config = [:])`
-   **`config` Map Parameters**:
    -   `workspaceDir` (String, Required): Path relative to the Jenkins job's workspace root that points to the application's root directory (where build files like `build.gradle`, `pom.xml`, or `package.json` are located). For example, if a Jenkins job checks out a Git repository named `my-app` and this repository contains the `Jenkinsfile` at its root, `workspaceDir` should be set to `'.'`. If the job checks out a larger repository (like this one) and `my-app` is a subdirectory (e.g., `sample-app`), then `workspaceDir` should be `'sample-app'`. This directory is also where `pactConfig.groovy` is expected.
    -   `pactBrokerTokenCredentialId` (String, Optional but Required for Pact Publish): Jenkins credential ID for the Pact Broker token. If provided, the 'Pact Publish' stage will be active.
    -   Other custom parameters can be added and utilized within `standardPipeline` or passed to tool-specific scripts.
-   **Standard Stages Executed**:
    1.  `Initialize`: Detects the build tool. Fails if no known build tool is found in `workspaceDir`.
    2.  `Build`: Executes the build using the detected tool (e.g., `buildGradle`).
    3.  `Test`: Executes tests using the detected tool (e.g., `testGradle`).
    4.  `Pact Publish`: (Conditional) Publishes Pact contracts if `pactBrokerTokenCredentialId` is provided. Uses `publishPactContracts.groovy`.
    5.  `Deploy`: Executes deployment using the detected tool (e.g., `deployGradle`).
-   **Operation**:
    -   Uses `BuildToolDetector.detect(this, appWorkspace)` to determine the build system.
    -   Executes build, test, and deploy commands within the context of `appWorkspace` (e.g., `dir(appWorkspace) { buildGradle() }`).

### 2. `vars/publishPactContracts.groovy`
Handles publishing of Pact contracts to a Pact Broker.
-   **Parameter Resolution**: See detailed "Pact Publishing Configuration" section below.
-   Key direct parameter: `brokerTokenCredentialId`.
-   Uses `ConfigUtils.loadPactConfig(this, workspaceDir)` to load `pactConfig.groovy`.

### 3. Tool-Specific Scripts (`vars/*Gradle.groovy`, `*Maven.groovy`, `*Npm.groovy`)
These scripts contain the actual commands for building, testing, and deploying for each supported tool. They are currently placeholders for Maven and NPM but functional for Gradle's `./gradlew` commands. They can be extended with more sophisticated logic (e.g., handling custom arguments from `config`).

### 4. `src/com/example/BuildToolType.groovy`
An `enum` defining the supported build tools: `GRADLE`, `MAVEN`, `NPM`, `UNKNOWN`.

### 5. `src/com/example/BuildToolDetector.groovy`
-   **Class**: `BuildToolDetector`
-   **Method**: `static BuildToolType detect(def script, String workspacePath = '.')`
-   **Functionality**: Checks for the presence of known build files (`build.gradle`, `build.gradle.kts` for Gradle; `pom.xml` for Maven; `package.json` for NPM) in the directory specified by `workspacePath` (relative to the Jenkins job's main workspace). It uses `script.fileExists()` within a `script.dir(workspacePath)` context.

### 6. `src/com/example/ConfigUtils.groovy`
-   **Class**: `ConfigUtils`
-   **Method**: `static Map loadPactConfig(def script, String workspacePath = '.')`
-   **Functionality**: Loads a Groovy script (expected to return a Map, e.g., `pactConfig.groovy`) from `${workspacePath}/pactConfig.groovy`.

## Application Repository Setup (`Jenkinsfile`)

Each application repository should contain a `Jenkinsfile` at its root with the following content:

```groovy
@Library('my-shared-library@main') _ // Adjust library name and version/branch as needed

// Configuration for the standardPipeline
def pipelineConfig = [
  /**
   * (Required) Application's root directory within the Jenkins job's workspace.
   * If this Jenkinsfile is at the root of your application code checkout, set this to '.'.
   */
  workspaceDir: '.',

  /**
   * (Required for Pact Publishing) Jenkins credential ID for the Pact Broker token.
   * Create a "Secret Text" credential in Jenkins and use its ID here.
   */
  pactBrokerTokenCredentialId: 'pact-broker-token', // REPLACE with your actual credential ID

  // --- Optional Parameters ---
  // Add any other parameters needed by your specific pipeline steps or shared library logic.
  // Example:
  // customBuildArgs: '--info --stacktrace',
  // deploymentEnvironment: 'staging'
]

// Execute the standard pipeline from the shared library
standardPipeline(pipelineConfig)
```

**Key `pipelineConfig` Explanations:**
-   `workspaceDir`: This tells `standardPipeline` where your application's main build files (like `build.gradle` or `pom.xml`) and your `pactConfig.groovy` are located, relative to the root of the SCM checkout for this `Jenkinsfile`. If this `Jenkinsfile` is at the root of your application code, `workspaceDir: '.'` is correct.
-   `pactBrokerTokenCredentialId`: Essential for enabling the 'Pact Publish' stage.

## Job DSL Configuration (`jobs/samplePipeline.groovy`)

The `jobs/samplePipeline.groovy` script in this repository provides an example of how to generate a Jenkins pipeline job that uses the SCM-driven approach:
-   It defines a `pipelineJob`.
-   It configures `cpsScm` to point to an application repository (for this example, it uses a placeholder for this repository itself).
-   It specifies `scriptPath('sample-app/Jenkinsfile')` (or just `Jenkinsfile` if the SCM points directly to an app repo root). This tells Jenkins where to find the `Jenkinsfile` within that repository.
-   **In a real-world scenario**: You would adapt this Job DSL script to:
    -   Discover multiple application repositories (e.g., from GitHub organizations, Bitbucket projects, or a static list).
    -   For each discovered repository, generate a `pipelineJob` configured to use the `Jenkinsfile` from that specific repository.

## Pact Publishing Configuration

The `publishPactContracts` function (called by `standardPipeline`) handles Pact contract publishing. Its configuration is resolved as follows:
1.  **Direct Parameters to `publishPactContracts`**:
    -   `brokerTokenCredentialId`: Passed from `pipelineConfig.pactBrokerTokenCredentialId`.
    -   `workspaceDir`: Passed as `appWorkspace` from `standardPipeline` (which is `pipelineConfig.workspaceDir` from the `Jenkinsfile`). This is the directory where `pactConfig.groovy` is located, and from which `pactFilesDir` (if relative in `pactConfig.groovy`) is resolved. The `pact-broker` CLI commands are also executed from this directory.
2.  **`pactConfig.groovy`**:
    -   **Location**: Must be in the directory specified by `pipelineConfig.workspaceDir` (e.g., `sample-app/pactConfig.groovy`).
    -   **Content**: A Groovy script returning a map. Can define:
        -   `brokerBaseUrl` (String): URL of the Pact Broker.
        -   `applicationName` (String): Consumer application name.
        -   `version` (String): Consumer application version.
        -   `pactFilesDir` (String): Directory containing pact files, relative to `pactConfig.groovy`'s location.
        -   `tags` (List or String): Tags for the pacticipant version.
        -   **DO NOT** store `brokerTokenCredentialId` here.
3.  **Dynamic Fallbacks**: If not found in direct params or `pactConfig.groovy`, `applicationName` can fall back to `env.JOB_NAME`, and `version` to `env.BUILD_NUMBER`.
4.  **Function Defaults**: `pactFilesDir` defaults to `'pacts'` if not specified anywhere else.

## Jenkins Setup Prerequisites

1.  **Shared Library Configuration**:
    -   In Jenkins: Manage Jenkins -> Configure System -> Global Pipeline Libraries.
    -   Add a new library.
    -   **Name**: `my-shared-library` (or your chosen name, ensure it matches `@Library` in `Jenkinsfile`).
    -   **Default version**: `main` (or your default branch/tag).
    -   **Retrieval method**: Modern SCM.
    -   **Source Code Management**: Git, with the URL of this `jenkins-pipelines` repository.
2.  **Job DSL Plugin**: Ensure the "Job DSL" plugin is installed in Jenkins.
3.  **Seed Job**: Create a Jenkins job (e.g., Freestyle or Pipeline) that uses the "Process Job DSLs" build step. Configure it to read `jobs/samplePipeline.groovy` from this repository's SCM. Running this seed job will generate the `sample-app-pipeline-from-scm` job.
4.  **Pact Broker CLI**: For Pact publishing, the `pact-broker` CLI must be installed and in the `PATH` on Jenkins agents that will run the 'Pact Publish' stage.
5.  **Credentials**:
    -   Create a "Secret text" credential in Jenkins for your Pact Broker token. Its ID must match what's provided in `pipelineConfig.pactBrokerTokenCredentialId`.
    -   If your Git repository for the shared library or application code is private, configure appropriate Git credentials in Jenkins.

## Using the Framework

1.  Set up Jenkins with the shared library and seed job as described above.
2.  Run the seed job to generate application pipeline jobs.
3.  For each application:
    -   Ensure it has a `Jenkinsfile` at its root, as per the template provided.
    -   Customize `pipelineConfig` in the `Jenkinsfile`, especially `workspaceDir` and `pactBrokerTokenCredentialId`.
    -   If using Pact, create a `pactConfig.groovy` in the `workspaceDir` with broker details, application name, version, etc.
    -   Ensure the build tool is supported (Gradle, Maven, NPM) and build files are present in `workspaceDir`.
4.  Trigger the generated application pipeline job in Jenkins.
```
