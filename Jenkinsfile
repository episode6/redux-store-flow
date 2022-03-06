#! groovy

node {
  stage('checkout') {
    checkout scm
  }

  def gradleRunner
  stage('pipeline') {
    gradleRunner = fileLoader.fromGit(
        'gradle/GradleRunner',
        'git@github.com:episode6/jenkins-pipelines.git',
        'v0.0.10',
        null,
        '')
  }

  gradleRunner.buildAndTest()

  stage('docgen') {
    gradleRunner.runGradle("docgen", "dokkaHtml", false)
  }

  gradleRunner.deploy()
}
