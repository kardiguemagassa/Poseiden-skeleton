node {
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
          sh 'echo "Sonar Host URL: $SONAR_HOST_URL"'
          sh """
            mvn sonar:sonar \
              -Dsonar.projectKey=Poseidon-skeleton \
              -Dsonar.host.url=$SONAR_HOST_URL \
              -Dsonar.token=$SONAR_TOKEN \
              -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
          """
        }
      }
    }

    stage('Quality Gate') {
      steps {
        timeout(time: 2, unit: 'MINUTES') {
          waitForQualityGate abortPipeline: true
        }
      }
    }
  }

  post {
    always {
      script {
        if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
          echo 'Build, couverture et qualité OK !'
        } else {
          echo 'Échec, vérifier les logs et SonarQube.'
        }
      }
    }
  }
}
