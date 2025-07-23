pipeline {
	agent any

    environment {
		MAVEN_HOME = '/Library/Java/Maven/apache-maven-3.9.10'
        PATH = "${MAVEN_HOME}/bin:${env.PATH}"
    }

    stages {
		stage('Checkout Code') {
			steps {
				git url: 'https://github.com/kardiguemagassa/Poseiden-skeleton.git', branch: 'master'
            }
        }

        stage('Build & Test') {
			steps {
				sh 'mvn clean install'
            }
        }
    }

    post {
		success {
			echo '✅ Build succeeded!'
        }
        failure {
			echo '❌ Build failed!'
        }
    }
}

