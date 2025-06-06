# Jenkins Shared Library and Job DSL Example

## Overview

This project provides a basic framework for using a Jenkins Shared Library and Job DSL to define and manage Jenkins pipelines. It includes:

- A shared library with reusable functions for building, testing, and deploying a sample application.
- Job DSL scripts to define Jenkins pipelines.
- A sample Gradle application that can be built, tested, and deployed by the pipelines.

## Directory Structure

```
.
├── README.md               # This file
├── jobs/                   # Contains Job DSL scripts
│   └── samplePipeline.groovy # Defines the sample pipeline
├── resources/              # For non-Groovy files in the shared library (e.g., templates)
├── sample-app/             # A sample Gradle project
│   ├── build.gradle        # Gradle build script for the sample app
│   ├── src/
│   │   ├── main/java/com/example/App.java      # Sample Java application
│   │   └── test/java/com/example/AppTest.java  # Sample unit test
├── src/                    # For utility classes in the shared library
└── vars/                   # For global variables and functions (Groovy scripts) in the shared library
    ├── build.groovy        # Shared library function for building
    ├── deploy.groovy       # Shared library function for deploying
    └── test.groovy         # Shared library function for testing
```

## Jenkins Configuration

### 1. Configure Shared Library

1.  **Go to Jenkins -> Manage Jenkins -> Configure System.**
2.  Scroll down to the **Global Pipeline Libraries** section.
3.  Click **Add**.
4.  **Name**: Give your library a name (e.g., `my-shared-library`). This name will be used in your Jenkinsfiles.
5.  **Default version**: Specify the branch or tag to use (e.g., `main`, `master`, or a specific release tag).
6.  **Retrieval Method**: Choose your SCM (e.g., **Modern SCM**).
7.  **Source Code Management**:
    *   Select **Git**.
    *   **Project Repository**: Enter the URL of this Git repository.
    *   **Credentials**: Add credentials if your repository is private.
8.  **Load implicitly**: If checked, this library will be available to all pipelines without explicit import. For more control, leave it unchecked.

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
    *   **DSL Scripts**: Enter the path to your Job DSL scripts (e.g., `jobs/**/*.groovy`). This will look for all `.groovy` files in the `jobs` directory and its subdirectories.
    *   **Action for removed jobs**: Choose an appropriate action (e.g., `Delete`).
    *   **Action for removed views**: Choose an appropriate action (e.g., `Delete`).
6.  Click **Save**.

## Running the Sample Pipeline

1.  **Run the Seed Job**: Manually trigger the `seed-job` you created in the previous step. This will process the `jobs/samplePipeline.groovy` script and create a new pipeline named `sample-pipeline` (or whatever name is defined in your DSL script, if you modify it).
2.  **Find the Sample Pipeline**: The newly created pipeline (e.g., `sample-pipeline`) should now appear on your Jenkins dashboard.
3.  **Run the Sample Pipeline**: Click on the pipeline and then **Build Now** to execute it. It will go through the 'Build', 'Test', and 'Deploy' stages, calling the shared library functions which currently print messages to the console.

To see actual Gradle execution, you would modify the `vars/*.groovy` scripts to execute Gradle commands within the `sample-app` directory.
