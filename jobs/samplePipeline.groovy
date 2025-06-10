// jobs/samplePipeline.groovy
// This Job DSL script defines a pipeline job that sources its Jenkinsfile from SCM.

pipelineJob('sample-app-pipeline-from-scm') { // Renamed for clarity
  displayName('Sample Application Pipeline (SCM-driven from sample-app/Jenkinsfile)')

  // Optional: Define parameters that can be used in the SCM configuration (e.g., branch)
  // parameters {
  //   stringParam('SCM_BRANCH', 'main', 'Branch to check out for the Jenkinsfile and application code')
  // }

  // Optional: Triggers for the job (e.g., SCM polling, upstream job, cron)
  // triggers {
  //   scm('H/15 * * * *') // Poll SCM every 15 minutes for changes
  // }

  definition {
    cpsScm {
      // SCM configuration:
      // This block defines where to find the Jenkinsfile.
      // In this example, we assume the Jenkins job executing this JobDSL script
      // has already checked out this 'jenkins-pipelines' repository, or has access to it.
      // The SCM definition below tells the *generated job* ('sample-app-pipeline-from-scm')
      // where to get its Jenkinsfile. For self-contained testing where the generated job
      // uses the same repo as the JobDSL script, this configuration is a bit meta.
      scm {
        git {
          // For a real setup, this URL would point to the specific application repository
          // that contains the Jenkinsfile at the specified scriptPath.
          // For this example, it points to a placeholder for THIS repository.
          remote {
            // Replace with the actual Git URL of this 'jenkins-pipelines' repository
            // or the application repository you intend to build.
            url('https://github.com/your-org/your-jenkins-pipelines-repo.git')
            // credentialsId('your-git-credentials-id') // Uncomment and set if the repository is private
          }
          // branch('*/main') // Example: build any branch named 'main'
          // Or use a parameter: branch('${SCM_BRANCH}')
          // For simplicity, we'll default to discovering all branches and letting Jenkins pick.
          // Or, specify a concrete branch like 'main' if not parameterizing.
          branches('*/main') // Monitor the main branch. Adjust as needed.
        }
      }
      // scriptPath: Path to the Jenkinsfile within the SCM repository defined above.
      // This path is relative to the root of the checked-out repository.
      // We are assuming the Jenkinsfile we created (Jenkinsfile.template) will be
      // placed at 'sample-app/Jenkinsfile' in this repository.
      scriptPath('sample-app/Jenkinsfile')

      // lightweight(true) // Optional: Use lightweight checkout. Good for speed if full history not needed.
    }
  }

  // Optional: Other job properties
  // properties {
  //   // Example: Discard old builds
  //   buildDiscarder(logRotator(numToKeepStr: '10'))
  // }
}
