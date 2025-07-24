node {
    def mvnHome = tool name: 'M3', type: 'hudson.tasks.Maven$MavenInstallation'
    def jdkHome = tool name: 'JDK-21', type: 'hudson.model.JDK'

    withCredentials([string(credentialsId: 'sonartoken', variable: 'SONAR_TOKEN')]) {
        env.PATH = "${jdkHome}/bin:${env.PATH}"

        stage('Checkout') {
            git url: 'https://github.com/kardiguemagassa/Poseiden-skeleton.git', branch: 'master'
        }

        stage('Build & Test') {
            sh "${mvnHome}/bin/mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install"
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
                sh 'echo "Sonar Host URL: $SONAR_HOST_URL"'
                sh """
                    ${mvnHome}/bin/mvn sonar:sonar \
                    -Dsonar.projectKey=Poseidon-skeleton \
                    -Dsonar.host.url=$SONAR_HOST_URL \
                    -Dsonar.token=$SONAR_TOKEN \
                    -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                """
            }
        }

        stage('Quality Gate') {
            timeout(time: 2, unit: 'MINUTES') {
                waitForQualityGate abortPipeline: true
            }
        }

        stage('Archive Reports') {
            archiveArtifacts artifacts: 'target/site/jacoco/**/*', allowEmptyArchive: true
        }
    }
}

currentBuild.result == null ? echo '✔️ Build, couverture et qualité OK !' : echo '❌ Échec, vérifier les logs et SonarQube.'
