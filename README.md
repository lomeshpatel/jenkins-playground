# Jenkins Shared Library and Job DSL Example

## Overview

This project provides a basic framework for using a Jenkins Shared Library and Job DSL to define and manage Jenkins pipelines. It includes:

- A shared library with reusable functions for building, testing, deploying a sample application, and publishing Pact contracts.
- Job DSL scripts to define Jenkins pipelines.
- A sample Gradle application that can be built, tested, and deployed by the pipelines, along with placeholder Pact contracts.

## Directory Structure

```
.
├── README.md               # This file
├── jobs/                   # Contains Job DSL scripts
│   └── samplePipeline.groovy # Defines the sample pipeline
├── resources/              # For non-Groovy files in the shared library (e.g., templates)
├── sample-app/             # A sample Gradle project
│   ├── build.gradle        # Gradle build script for the sample app
│   ├── pacts/              # Placeholder Pact contracts
│   │   └── myconsumer-myprovider.json
│   ├── src/
│   │   ├── main/java/com/example/App.java      # Sample Java application
│   │   └── test/java/com/example/AppTest.java  # Sample unit test
├── src/                    # For utility classes in the shared library
└── vars/                   # For global variables and functions (Groovy scripts) in the shared library
    ├── build.groovy        # Shared library function for building
    ├── deploy.groovy       # Shared library function for deploying
    ├── publishPactContracts.groovy # Shared library function for Pact contract publishing
    └── test.groovy         # Shared library function for testing
```

## Shared Library Functions

This project defines the following global functions in the `vars/` directory:

- **`build(Map config = [:])`**:
    - Purpose: Executes a Gradle build.
    - Current Implementation: Prints 'Executing Gradle build'.
- **`test(Map config = [:])`**:
    - Purpose: Executes Gradle tests.
    - Current Implementation: Prints 'Executing Gradle tests'.
- **`deploy(Map config = [:])`**:
    - Purpose: Executes a Gradle deployment.
    - Current Implementation: Prints 'Executing Gradle deployment'.
- **`publishPactContracts(Map params)`**:
    - Purpose: Publishes Pact contract files to a Pact Broker.
    - Parameters:
        - `brokerBaseUrl` (String, Required): The base URL of your Pact Broker instance (e.g., `https://your-broker.example.com`).
        - `brokerTokenCredentialId` (String, Required): The ID of the Jenkins "Secret text" credential storing your Pact Broker API token.
        - `applicationName` (String, Required): The name of the consumer application publishing the contracts.
        - `version` (String, Required): The version of the consumer application. Typically, this would be the build number or a semantic version.
        - `pactFilesDir` (String, Required): The directory path (relative to the workspace root) containing the Pact JSON files to be published (e.g., `sample-app/pacts`).
        - `tags` (String, Optional): A comma-separated string of tags to apply to the published contracts in the Pact Broker (e.g., `dev,feat-new-feature`).
    - Prerequisites: Requires the `pact-broker` CLI tool to be installed on the Jenkins agent.

## Prerequisites for Pact Publishing

- **`pact-broker` CLI**: The `pact-broker` command-line tool must be installed and available in the `PATH` on any Jenkins agent that will execute the `publishPactContracts` step.
    - Installation instructions can be found here: [Pact Broker Client CLI Installation](https://github.com/pact-foundation/pact_broker-client#installation)
- **Pact Broker Token**: You need an API token for your Pact Broker to authenticate and authorize publishing.
    - This token should be stored as a **Secret text** credential in Jenkins.
    - Go to **Jenkins -> Manage Jenkins -> Credentials**.
    - Select the appropriate domain (or global) and click **Add Credentials**.
    - **Kind**: Select `Secret text`.
    - **Secret**: Paste your Pact Broker token.
    - **ID**: Enter a descriptive ID (e.g., `pact-broker-token`). This ID is what you will use for the `brokerTokenCredentialId` parameter in the `publishPactContracts` function.
    - **Description**: Add a meaningful description.
    - **Scope**: Limit the scope of the credential as much as possible (e.g., to specific jobs or folders) rather than making it globally available, if your Jenkins setup allows.

## Jenkins Configuration

### 1. Configure Shared Library

1.  **Go to Jenkins -> Manage Jenkins -> Configure System.**
2.  Scroll down to the **Global Pipeline Libraries** section.
3.  Click **Add**.
4.  **Name**: Give your library a name (e.g., `my-shared-library`). This name will be used in your Jenkinsfiles (`@Library('my-shared-library') _`).
5.  **Default version**: Specify the branch or tag to use (e.g., `main`, `master`).
6.  **Retrieval Method**: Choose your SCM (e.g., **Modern SCM**).
7.  **Source Code Management**:
    *   Select **Git**.
    *   **Project Repository**: Enter the URL of this Git repository.
    *   **Credentials**: Add credentials if your repository is private.

### 2. Configure Job DSL Plugin

1.  **Go to Jenkins -> Manage Jenkins -> Manage Plugins.**
2.  Go to the **Available** tab and search for `Job DSL`.
3.  Install the plugin and restart Jenkins if required.

### 3. Create a Seed Job

A seed job is a Jenkins job that runs Job DSL scripts to create or update other Jenkins jobs.

1.  **Go to Jenkins -> New Item.**
2.  Enter a name for your seed job (e.g., `seed-job`).
3.  Select **Freestyle project** and click **OK**.
4.  **Source Code Management**:
    *   Select **Git**.
    *   **Project Repository**: Enter the URL of this Git repository.
    *   **Credentials**: Add credentials if your repository is private.
    *   **Branch Specifier**: Specify the branch where your Job DSL scripts are located (e.g., `*/main`).
5.  **Build Steps**:
    *   Click **Add build step** and select **Process Job DSLs**.
    *   **Look on Filesystem**: Select this option.
    *   **DSL Scripts**: Enter the path to your Job DSL scripts (e.g., `jobs/**/*.groovy`).
    *   **Action for removed jobs/views**: Choose appropriate actions (e.g., `Delete`).
6.  Click **Save**.

## Running the Sample Pipeline

1.  **Run the Seed Job**: Manually trigger the `seed-job`. This processes `jobs/samplePipeline.groovy` and creates/updates the `sample-pipeline` Jenkins job.
2.  **Find the Sample Pipeline**: The `sample-pipeline` job will appear on your Jenkins dashboard.
3.  **Run the Sample Pipeline**: Click on the pipeline and then **Build Now**. It will execute the defined stages:
    *   **Build**: Calls `build()` (prints a message).
    *   **Test**: Calls `test()` (prints a message).
    *   **Deploy**: Calls `deploy()` (prints a message).
    *   **Pact Publish**: Calls `publishPactContracts()` with placeholder parameters. This stage will attempt to publish pacts from `sample-app/pacts`. **Note:** This stage will fail unless you have configured a valid Pact Broker URL, a corresponding `pact-broker-token` credential in Jenkins, and the `pact-broker` CLI on the agent.

To see actual Gradle execution and real Pact publishing, you would:
- Modify the `vars/*.groovy` scripts to execute actual Gradle commands (e.g., `./gradlew build test deploy -p sample-app`).
- Ensure your tests in `sample-app` generate Pact contracts into the `sample-app/pacts` directory.
- Configure the `publishPactContracts` parameters in `jobs/samplePipeline.groovy` with your actual Pact Broker details and Jenkins credential ID.
