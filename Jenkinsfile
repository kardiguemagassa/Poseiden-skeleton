node {
    try {
        // Définir les outils au début du pipeline
        def mavenHome = tool name: 'M3', type: 'maven'
        def jdkHome = tool name: 'JDK-21', type: 'jdk'
        def HTTP_PORT = getHTTPPort(env.BRANCH_NAME)
        def ENV_NAME = getEnvName(env.BRANCH_NAME)
        def CONTAINER_NAME = "poseidon-app"
        def CONTAINER_TAG = getTag(env.BUILD_NUMBER, env.BRANCH_NAME)
        def EMAIL_RECIPIENTS = "magassakara@gmail.com"

        // Définir les variables d'environnement
        env.JAVA_HOME = jdkHome
        env.PATH = "${mavenHome}/bin:${jdkHome}/bin:${env.PATH}"
        env.MAVEN_HOME = mavenHome

        stage('Checkout') {
            // Vérification de la branche et du commit
            checkout scm
        }

        stage('Check Maven') {
            script {
                // Affiche l'emplacement de Maven
                echo "MAVEN_HOME is: ${mavenHome}"
                echo "JAVA_HOME is: ${jdkHome}"
                echo "Branch: ${env.BRANCH_NAME}"
                echo "Environment: ${ENV_NAME}"
                echo "HTTP Port: ${HTTP_PORT}"
            }
        }

        stage('Build & Test') {
            // Affichage des variables d'environnement pour le diagnostic
            sh 'echo "JAVA_HOME: $JAVA_HOME"'
            sh 'echo "MAVEN_HOME: $MAVEN_HOME"'
            sh 'echo "PATH: $PATH"'
            sh 'mvn --version'
            // Exécution du build avec Maven
            sh "mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install"
        }

        stage('Coverage Report') {
            // Enregistrement du rapport de couverture avec JaCoCo
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
            // Exécution de l'analyse SonarQube
            withSonarQubeEnv('SonarQube') {
                withCredentials([string(credentialsId: 'sonartoken', variable: 'SONAR_TOKEN')]) {
                    sh '''
                        mvn sonar:sonar \
                          -Dsonar.projectKey=Poseidon-skeleton \
                          -Dsonar.host.url=$SONAR_HOST_URL \
                          -Dsonar.token=${SONAR_TOKEN} \
                          -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    '''
                }
            }
        }

        stage('Quality Gate') {
            // Vérification du quality gate
            timeout(time: 2, unit: 'MINUTES') {
                waitForQualityGate abortPipeline: true
            }
        }

        stage('Run App') {
            withCredentials([usernamePassword(credentialsId: 'dockerhubcredentials', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                runApp(CONTAINER_NAME, CONTAINER_TAG, USERNAME, HTTP_PORT, ENV_NAME)
            }
        }

        stage('Résultat') {
            // Affichage du résultat du build
            script {
                if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
                    echo 'Build, couverture et qualité OK !'
                } else {
                    echo 'Échec, vérifier les logs et SonarQube.'
                }
            }
        }

    } catch (Exception e) {
        currentBuild.result = 'FAILURE'
        echo "Pipeline failed: ${e.getMessage()}"
        throw e
    } finally {
        deleteDir()
        sendEmail(EMAIL_RECIPIENTS)
    }
}

def sendEmail(recipients) {
    mail(
        to: recipients,
        subject: "Build ${env.BUILD_NUMBER} - ${currentBuild.currentResult} - (${currentBuild.fullDisplayName})",
        body: "Check console output at: ${env.BUILD_URL}/console" + "\n"
    )
}

String getEnvName(String branchName) {
    if (branchName == 'master') {
        return 'prod'
    }
    return (branchName == 'develop') ? 'uat' : 'dev'
}

String getHTTPPort(String branchName) {
    if (branchName == 'master') {
        return '9003'
    }
    return (branchName == 'develop') ? '9002' : '9001'
}

String getTag(String buildNumber, String branchName) {
    if (branchName == 'master') {
        return buildNumber + '-stable'
    }
    return buildNumber + '-unstable'
}