# Jenkins Shared Library and Job DSL Example

## Overview

This project provides a basic framework for using a Jenkins Shared Library and Job DSL to define and manage Jenkins pipelines. It includes:

- A shared library with reusable functions for building, testing, deploying a sample application, and publishing Pact contracts. This library demonstrates loading external configuration from a Groovy script within the application's directory.
- Job DSL scripts to define Jenkins pipelines.
- A sample Gradle application that can be built, tested, and deployed by the pipelines, along with placeholder Pact contracts and an example `pactConfig.groovy` file.

## Directory Structure

```
.
├── README.md               # This file
├── jobs/                   # Contains Job DSL scripts
│   └── samplePipeline.groovy # Defines the sample pipeline
├── sample-app/             # A sample Gradle project
│   ├── build.gradle        # Gradle build script for the sample app
│   ├── pactConfig.groovy   # Example external config for Pact publishing
│   ├── pacts/              # Placeholder Pact contracts
│   │   └── myconsumer-myprovider.json
│   ├── src/
│   │   ├── main/java/com/example/App.java      # Sample Java application
│   │   └── test/java/com/example/AppTest.java  # Sample unit test
├── src/                    # For utility classes in the shared library
│   └── com/example/ConfigUtils.groovy # Utility to load .groovy config files
└── vars/                   # For global variables and functions (Groovy scripts) in the shared library
    ├── build.groovy        # Shared library function for building
    ├── deploy.groovy       # Shared library function for deploying
    ├── publishPactContracts.groovy # Shared library function for Pact contract publishing
    └── test.groovy         # Shared library function for testing
```

## Shared Library Functions

This project defines the following global functions in the `vars/` directory:

- **`build(Map config = [:])`**, **`test(Map config = [:])`**, **`deploy(Map config = [:])`**:
    - Basic placeholder functions that print messages. In a real scenario, these would execute Gradle or other build tool commands.

- **`publishPactContracts(Map params)`**:
    - Purpose: Publishes Pact contract files to a Pact Broker.
    - **Parameter Resolution Strategy**:
        The function resolves its operational parameters using the following order of precedence (highest to lowest):
        1.  **Direct Parameters**: Values passed directly in the `params` map when calling `publishPactContracts` in the Jenkinsfile.
        2.  **External Configuration (`pactConfig.groovy`)**: Values loaded from a `pactConfig.groovy` file located in the directory specified by the `workspaceDir` parameter (or its default).
        3.  **Dynamic Fallbacks**: Environment variables available in the Jenkins build context (e.g., `env.JOB_NAME` for `applicationName`, `env.BUILD_NUMBER` for `version`).
        4.  **Function Defaults**: Predefined defaults within the `publishPactContracts` function itself (e.g., `pactFilesDir` defaults to `'pacts'`).
    - **Key Parameters**:
        - `brokerTokenCredentialId` (String, **Required as a direct parameter**): The ID of the Jenkins "Secret text" credential storing your Pact Broker API token. This is considered sensitive and should always be passed directly from the Jenkinsfile or job configuration.
        - `workspaceDir` (String, Optional): The path, relative to the Jenkins job's workspace root, to the directory containing the `pactConfig.groovy` file and from which `pactFilesDir` (if relative) will be resolved. Defaults to `.` (current directory, usually the job workspace root). The `pact-broker` CLI command will also be executed from this directory.
        - `brokerBaseUrl` (String): Base URL of the Pact Broker.
        - `applicationName` (String): Name of the consumer application.
        - `version` (String): Version of the consumer application.
        - `pactFilesDir` (String): Directory containing Pact JSON files. If relative, it's resolved against `workspaceDir`.
        - `tags` (String or List): Comma-separated string or a List of tags for the Pacticipant version.
    - **External Configuration (`pactConfig.groovy`)**:
        - **Location**: Expected to be in the directory defined by the `workspaceDir` parameter (e.g., `sample-app/pactConfig.groovy` if `workspaceDir` is `sample-app`).
        - **Format**: A Groovy script that returns a `Map`. See `sample-app/pactConfig.groovy` for an example.
        - **Recommended Settings**: `applicationName`, `version`, `pactFilesDir`, `tags`, `brokerBaseUrl`.
        - **Security Note**: **Do NOT store `brokerTokenCredentialId` in `pactConfig.groovy`**. This ID should be passed directly to `publishPactContracts` from the Jenkins pipeline configuration.
    - **Prerequisites**: Requires the `pact-broker` CLI tool to be installed on the Jenkins agent (see "Prerequisites for Pact Publishing" below).

## Prerequisites for Pact Publishing

- **`pact-broker` CLI**: The `pact-broker` command-line tool must be installed and available in the `PATH` on any Jenkins agent that will execute the `publishPactContracts` step.
    - Installation instructions: [Pact Broker Client CLI Installation](https://github.com/pact-foundation/pact_broker-client#installation)
- **Pact Broker Token**: An API token for your Pact Broker.
    - Store this as a **Secret text** credential in Jenkins (Manage Jenkins -> Credentials).
    - Use the ID of this credential for the `brokerTokenCredentialId` parameter.
    - Scope credentials appropriately.

## Jenkins Configuration

(Sections for Shared Library, Job DSL Plugin, and Seed Job remain largely the same but ensure `my-shared-library` is used as the library name example).

### 1. Configure Shared Library
   (Ensure name is `my-shared-library` and repository URL points to this project)

### 2. Configure Job DSL Plugin
   (Standard instructions)

### 3. Create a Seed Job
   (Standard instructions, ensuring DSL scripts path is `jobs/**/*.groovy`)

## Running the Sample Pipeline

1.  **Run the Seed Job**: This processes `jobs/samplePipeline.groovy` and creates/updates the `sample-pipeline` Jenkins job.
2.  **Find the Sample Pipeline**: The `sample-pipeline` job will appear on your Jenkins dashboard.
3.  **Run the Sample Pipeline**: Click **Build Now**. It will execute:
    *   **Build, Test, Deploy Stages**: Print messages.
    *   **Pact Publish Stage**: Calls `publishPactContracts`.
        - It is configured to use `workspaceDir: 'sample-app'`, so it will look for `sample-app/pactConfig.groovy`.
        - `brokerTokenCredentialId` is passed directly (e.g., `'pact-broker-token'`). You **must** create a Jenkins credential with this ID.
        - Other parameters like `brokerBaseUrl`, `applicationName`, `version`, `pactFilesDir`, and `tags` will be sourced from `sample-app/pactConfig.groovy`, or dynamic fallbacks if not present there.
        - The `sample-app/pactConfig.groovy` has placeholder values. You'll need to set `brokerBaseUrl` there (or override it directly in the pipeline) and ensure the `pact-broker` CLI is installed for this stage to succeed.

**Example `publishPactContracts` call in `jobs/samplePipeline.groovy`:**
```groovy
publishPactContracts(
    brokerTokenCredentialId: 'pact-broker-token', // REQUIRED
    workspaceDir: 'sample-app' // Specifies location of pactConfig.groovy
    // Other params can be added here to override pactConfig.groovy or dynamic values
)
```

To make the pipeline fully functional:
- Modify `vars/*.groovy` (build, test, deploy) to execute actual Gradle commands (e.g., `sh "./gradlew build -p sample-app"`).
- Ensure your tests in `sample-app` generate Pact contracts into the `sample-app/pacts` directory (or update `pactFilesDir` in `sample-app/pactConfig.groovy`).
- Update `sample-app/pactConfig.groovy` with your actual Pact Broker URL and any other desired settings.
- Create the 'pact-broker-token' (or your chosen ID) secret text credential in Jenkins.
- Install `pact-broker` CLI on your Jenkins agents.
