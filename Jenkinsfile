def EMAIL_RECIPIENTS = "magassakara@gmail.com"

def BRANCH_NAME
def BUILD_NUMBER
def CONTAINER_NAME = "poseidon-app"
def DOCKER_REGISTRY = "docker.io"
def mavenHome
def jdkHome
def dockerHome = '/usr/local/bin'
def HTTP_PORT
def ENV_NAME
def CONTAINER_TAG

node {
    try {
        stage('Initialisation') {
            script {
                BRANCH_NAME = env.BRANCH_NAME ?: sh(
                    script: 'git rev-parse --abbrev-ref HEAD || echo "unknown"',
                    returnStdout: true
                ).trim()

                BUILD_NUMBER = env.BUILD_NUMBER ?: currentBuild.number ?: "0"

                mavenHome = tool name: 'M3', type: 'maven'
                jdkHome = tool name: 'JDK-21', type: 'jdk'

                env.DOCKER_AVAILABLE = sh(
                    script: 'which docker && docker --version >/dev/null 2>&1 && echo "true" || echo "false"',
                    returnStdout: true
                ).trim() == "true" ? "true" : "false"

                env.JAVA_HOME = jdkHome
                env.MAVEN_HOME = mavenHome
                env.PATH = "${dockerHome}:${mavenHome}/bin:${jdkHome}/bin:${env.PATH}"
                env.DOCKER_BUILDKIT = "1"

                HTTP_PORT = getHTTPPort(BRANCH_NAME)
                ENV_NAME = getEnvName(BRANCH_NAME)
                CONTAINER_TAG = getTag(BUILD_NUMBER.toString(), BRANCH_NAME)
            }
        }

        stage('Checkout') {
            checkout scm
            script {
                BRANCH_NAME = sh(script: 'git rev-parse --abbrev-ref HEAD || echo "unknown"', returnStdout: true).trim()
                echo "Nom de branche confirmé: ${BRANCH_NAME}"
            }
        }

        stage('Environment Setup') {
            script {
                echo """
                [Configuration Environnement]
                • Build #: ${BUILD_NUMBER}
                • Branch: ${BRANCH_NAME}
                • Java: ${jdkHome}
                • Maven: ${mavenHome}
                • Docker: ${env.DOCKER_AVAILABLE}
                • Environnement: ${ENV_NAME}
                • Port: ${HTTP_PORT}
                • Tag: ${CONTAINER_TAG}
                """

                sh 'mvn --version'
                sh 'java -version'

                if (env.DOCKER_AVAILABLE == "true") {
                    sh 'docker --version'
                    sh 'docker info'
                }
            }
        }

        stage('Build & Test') {
            sh """
                mvn clean verify \
                    org.jacoco:jacoco-maven-plugin:prepare-agent \
                    -DskipTests=false \
                    -Dmaven.test.failure.ignore=false
            """
        }

        stage('Code Analysis') {
            withSonarQubeEnv('SonarQube') {
                withCredentials([string(credentialsId: 'sonartoken', variable: 'SONAR_TOKEN')]) {
                    sh """
                        mvn sonar:sonar \
                          -Dsonar.projectKey=Poseidon-skeleton \
                          -Dsonar.host.url=\$SONAR_HOST_URL \
                          -Dsonar.token=\${SONAR_TOKEN} \
                          -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                          -Dsonar.java.binaries=target/classes
                    """
                }
            }
        }

        stage('Quality Gate') {
            timeout(time: 5, unit: 'MINUTES') {
                waitForQualityGate abortPipeline: true
            }
        }

        stage('Docker Operations') {
            script {
                if (!fileExists('Dockerfile')) {
                    error "Fichier Dockerfile introuvable à la racine du projet."
                }

                def jarFiles = findFiles(glob: 'target/*.jar').findAll { it.name.endsWith('.jar') }
                if (jarFiles.length == 0) {
                    error "Aucun fichier JAR trouvé dans target/"
                }

                def jarFile = jarFiles[0].path

                if (env.DOCKER_AVAILABLE == "true") {
                    try {
                        sh """
                            docker build \
                                --pull \
                                --no-cache \
                                --build-arg JAR_FILE=${jarFile} \
                                -t ${CONTAINER_NAME}:${CONTAINER_TAG} \
                                .
                        """

                        withCredentials([usernamePassword(
                            credentialsId: 'dockerhub-credentials',
                            usernameVariable: 'DOCKER_USER',
                            passwordVariable: 'DOCKER_PASSWORD'
                        )]) {
                            sh """
                                echo "\${DOCKER_PASSWORD}" | docker login -u "\${DOCKER_USER}" --password-stdin ${DOCKER_REGISTRY}
                                docker tag ${CONTAINER_NAME}:${CONTAINER_TAG} \${DOCKER_USER}/${CONTAINER_NAME}:${CONTAINER_TAG}
                                docker push \${DOCKER_USER}/${CONTAINER_NAME}:${CONTAINER_TAG}
                            """

                            if (BRANCH_NAME == 'master') {
                                sh """
                                    docker tag ${CONTAINER_NAME}:${CONTAINER_TAG} \${DOCKER_USER}/${CONTAINER_NAME}:latest
                                    docker push \${DOCKER_USER}/${CONTAINER_NAME}:latest
                                """
                            }

                            sh "docker logout ${DOCKER_REGISTRY}"
                        }
                    } catch (Exception e) {
                        error "Erreur Docker: ${e.getMessage()}"
                    }
                } else {
                    echo "Docker non disponible - étapes Docker ignorées"
                    currentBuild.result = 'UNSTABLE'
                }
            }
        }

        stage('Deploy') {
            script {
                if (env.DOCKER_AVAILABLE == "true" && (BRANCH_NAME == 'master' || BRANCH_NAME == 'develop')) {
                    try {
                        withCredentials([usernamePassword(
                            credentialsId: 'dockerhub-credentials',
                            usernameVariable: 'DOCKER_USER',
                            passwordVariable: 'DOCKER_PASSWORD'
                        )]) {
                            sh "docker stop ${CONTAINER_NAME} || true"
                            sh "docker rm ${CONTAINER_NAME} || true"

                            sh """
                                docker run -d \
                                    --name ${CONTAINER_NAME} \
                                    -p ${HTTP_PORT}:${HTTP_PORT} \
                                    -e SPRING_PROFILES_ACTIVE=${ENV_NAME} \
                                    \${DOCKER_USER}/${CONTAINER_NAME}:${CONTAINER_TAG}
                            """

                            sleep(time: 10, unit: 'SECONDS')

                            def status = sh(
                                script: "docker inspect -f '{{.State.Status}}' ${CONTAINER_NAME}",
                                returnStdout: true
                            ).trim()

                            if (status != "running") {
                                error "Le conteneur n'est pas 'running' (statut: ${status})"
                            }
                        }
                    } catch (Exception e) {
                        error "Échec du déploiement: ${e.getMessage()}"
                    }
                } else {
                    echo "Conditions de déploiement non remplies. Docker ou branche non autorisée."
                }
            }
        }

    } catch (Exception e) {
        currentBuild.result = 'FAILURE'
        echo "ERREUR CRITIQUE: ${e.getMessage()}"
        error "Échec du pipeline"
    } finally {
        try {
            deleteDir()
        } catch (Exception e) {
            echo "Erreur lors du nettoyage: ${e.getMessage()}"
        }
        sendEmail(EMAIL_RECIPIENTS)
    }
}

// 🔧 Fonctions utilitaires

def sendEmail(recipients) {
    try {
        def cause = currentBuild.getBuildCauses()?.collect { it.shortDescription }?.join(', ') ?: "Non spécifiée"
        def subject = "[Jenkins] ${env.JOB_NAME} - Build #${env.BUILD_NUMBER} - ${currentBuild.currentResult}"
        def body = """
        Résultat: ${currentBuild.currentResult}
        Projet: ${env.JOB_NAME}
        Build: #${env.BUILD_NUMBER}
        Branche: ${env.BRANCH_NAME ?: 'N/A'}
        Durée: ${currentBuild.durationString.replace(' and counting', '')}
        Détails: ${env.BUILD_URL}console
        Docker: ${env.DOCKER_AVAILABLE == "true" ? "Disponible" : "Indisponible"}
        Cause: ${cause}
        """
        mail(to: recipients, subject: subject, body: body)
    } catch (Exception e) {
        echo "Échec de l'envoi d'email: ${e.getMessage()}"
    }
}

String getEnvName(String branchName) {
    switch (branchName?.toLowerCase()) {
        case 'master': return 'prod'
        case 'develop': return 'uat'
        default: return 'dev'
    }
}

String getHTTPPort(String branchName) {
    switch (branchName?.toLowerCase()) {
        case 'master': return '9003'
        case 'develop': return '9002'
        default: return '9001'
    }
}

String getTag(String buildNumber, String branchName) {
    def safeBranch = (branchName ?: "unknown")
        .replaceAll('[^a-zA-Z0-9-]', '-')
        .toLowerCase()
    return (safeBranch == 'master') ?
        "${buildNumber}-stable" :
        "${buildNumber}-${safeBranch}-snapshot"
}
