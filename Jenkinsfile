pipeline {
    agent any

    stages {
        stage('build') {
            steps {
                echo 'building the app'
            }
        }
        stage ('Test') {
            steps{
                echo 'Running tests'
            }
        }
        stage('Depoly'){
            steps{
               echo 'Deploying app'
          }
        }
    }
}
