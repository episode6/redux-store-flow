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

  
  stage('clean') {
    gradleRunner.runGradle("clean", "clean", false)
  }

  stage('build') {
    gradleRunner.runGradle("build", "assemble", false)
  }

  stage('test') {
    gradleRunner.runGradle("test", "check", false) // KMP doesnt output xmls
  }

  stage('docgen') {
    gradleRunner.runGradle("docgen", "dokkaHtml", false)
  }

  gradleRunner.deploy()
}
