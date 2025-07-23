pipeline {
	agent any

  tools {
		maven 'M3'
    jdk 'JDK-21'
  }

  environment {
		SONAR_TOKEN = credentials('sonartoken')
  }

  stages {
		stage('Checkout') {
			steps {
				git url: 'https://github.com/kardiguemagassa/Poseiden-skeleton.git', branch: 'master'
      }
    }

    stage('Build & Test') {
			steps {
				sh 'mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install'
      }
    }

    stage('Coverage Report') {
			steps {
				recordCoverage(
          tools: [[parser: 'JACOCO']],
          id: 'jacoco',
          sourceCodeRetention: 'EVERY_BUILD',
          qualityGates: [
            [threshold: 60.0, metric: 'LINE', baseline: 'PROJECT', unstable: true],
            [threshold: 40.0, metric: 'BRANCH', baseline: 'PROJECT', unstable: true]
          ]
        )
      }
    }

    stage('SonarQube Analysis') {
			steps {
				withSonarQubeEnv('SonarQube') {
					sh """
            mvn sonar:sonar \
              -Dsonar.projectKey=Poseidon-skeleton \
              -Dsonar.host.url=${env.SONAR_HOST_URL} \
              -Dsonar.login=${SONAR_TOKEN} \
              -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
          """
        }
      }
    }

    stage('Quality Gate') {
			steps {
				timeout(time: 5, unit: 'MINUTES') {
					waitForQualityGate abortPipeline: true
        }
      }
    }
  }
// squ_555f098b3f5662a543b27bfe23cf825606c92329
  post {
		success { echo '✅ Build, couverture et qualité OK !' }
    failure { echo '❌ Échec, vérifier les logs et SonarQube.' }
  }
}
