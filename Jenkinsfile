pipeline {
    agent any
    // Triggers for calling builds
    triggers {
        githubPush()
    }
    // Additional credentials for gradle tasks
    environment {
        NEXUS = credentials("NachtRaben-Nexus")
    }
    // Options to configure workspace
    options {
        buildDiscarder(logRotator(numToKeepStr: '5', artifactNumToKeepStr: '5'))
        disableConcurrentBuilds()
    }
    // Tools to specify specific gradle/jdk/etc tools
    tools {
        gradle 'latest'
        jdk 'latest'
    }
    stages {
        // Test code can compile successfully
        stage ('Build') {
            steps {
                sh 'gradle clean shadowJar'
            }
        }
        // Save the build artifacts for automatic deployment
        stage ('Archive') {
            steps {
                echo "Grabbing artifacts..."
                archiveArtifacts artifacts: '**/build/libs/*.jar', onlyIfSuccessful: true
            }
        }
    }
}
