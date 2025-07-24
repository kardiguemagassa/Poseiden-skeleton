node {
    stage('Checkout') {
        git url: 'https://github.com/kardiguemagassa/Poseiden-skeleton.git', branch: 'master'
    }

    stage('Build & Test') {
        steps {
            // Définition des outils Maven et Java via 'tool'
            MAVEN_HOME = tool name: 'M3', type: 'Maven'
            JDK_HOME = tool name: 'JDK-21', type: 'JDK'
            
            // Vérification des outils
            sh 'echo "JAVA_HOME: $JAVA_HOME"'
            sh 'echo "MAVEN_HOME: $MAVEN_HOME"'
            
            // Build avec Maven
            sh "${MAVEN_HOME}/bin/mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install"
        }
    }

    stage('Coverage Report') {
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

    stage('SonarQube Analysis') {
        withSonarQubeEnv('SonarQube') {
            withCredentials([string(credentialsId: 'sonartoken', variable: 'SONAR_TOKEN')]) {
                sh """
                    mvn sonar:sonar \
                      -Dsonar.projectKey=Poseidon-skeleton \
                      -Dsonar.host.url=$SONAR_HOST_URL \
                      -Dsonar.token=${SONAR_TOKEN} \
                      -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                """
            }
        }
    }

    stage('Quality Gate') {
        timeout(time: 2, unit: 'MINUTES') {
            waitForQualityGate abortPipeline: true
        }
    }

    stage('Résultat') {
        script {
            if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
                echo '✔️ Build, couverture et qualité OK !'
            } else {
                echo '❌ Échec, vérifier les logs et SonarQube.'
            }
        }
    }
}