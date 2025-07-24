def EMAIL_RECIPIENTS = "magassakara@gmail.com"

node {
    try {
        // 1. Initialisation des variables
        def BRANCH_NAME = env.BRANCH_NAME ?: sh(
            script: 'git rev-parse --abbrev-ref HEAD || echo "unknown"',
            returnStdout: true
        ).trim()

        def BUILD_NUMBER = env.BUILD_NUMBER ?: currentBuild.number ?: "0"

        // 2. Configuration des outils
        def mavenHome = tool name: 'M3', type: 'maven'
        def jdkHome = tool name: 'JDK-21', type: 'jdk'
        def dockerHome = '/usr/local/bin' // Chemin standard sur macOS

        // 3. Vérification de Docker
        env.DOCKER_AVAILABLE = sh(
            script: 'docker --version && echo "true" || echo "false"',
            returnStdout: true
        ).trim() == "true" ? "true" : "false"

        // 4. Setup environnement
        env.JAVA_HOME = jdkHome
        env.MAVEN_HOME = mavenHome
        env.PATH = "${dockerHome}:${mavenHome}/bin:${jdkHome}/bin:${env.PATH}"

        // 5. Variables dérivées
        def HTTP_PORT = getHTTPPort(BRANCH_NAME)
        def ENV_NAME = getEnvName(BRANCH_NAME)
        def CONTAINER_NAME = "poseidon-app"
        def CONTAINER_TAG = getTag(BUILD_NUMBER, BRANCH_NAME)

        stage('Checkout') {
            checkout scm
            echo "Branch détectée: ${BRANCH_NAME}"
        }

        stage('Environment Setup') {
            echo """
            Configuration Environnement:
            - Build: ${BUILD_NUMBER}
            - Branch: ${BRANCH_NAME}
            - Maven: ${mavenHome}
            - Java: ${jdkHome}
            - Docker: ${env.DOCKER_AVAILABLE}
            - Port: ${HTTP_PORT}
            - Environnement: ${ENV_NAME}
            - Tag: ${CONTAINER_TAG}
            """

            sh 'mvn --version'
            sh 'java -version'
            if (env.DOCKER_AVAILABLE == "true") {
                sh 'docker --version'
            }
        }

        stage('Build & Test') {
            sh "mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install"
        }

        stage('SonarQube Analysis') {
            withSonarQubeEnv('SonarQube') {
                withCredentials([string(credentialsId: 'sonartoken', variable: 'SONAR_TOKEN')]) {
                    sh """
                        mvn sonar:sonar \
                          -Dsonar.projectKey=Poseidon-skeleton \
                          -Dsonar.host.url=\$SONAR_HOST_URL \
                          -Dsonar.token=\${SONAR_TOKEN} \
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

        stage('Docker Build') {
            when {
                expression { env.DOCKER_AVAILABLE == "true" }
            }
            steps {
                script {
                    // Vérification que le JAR existe
                    def jarFile = findFiles(glob: 'target/*.jar')[0]?.path
                    if (!jarFile) {
                        error "Aucun fichier JAR trouvé dans target/"
                    }
                    try {
                        sh """
                            docker build \
                                --build-arg JAR_FILE=${jarFile} \
                                -t ${CONTAINER_NAME}:${CONTAINER_TAG} \
                                .
                        """
                    } catch (Exception e) {
                        error "Échec du build Docker: ${e.getMessage()}"
                    }
                }
            }
        }

        stage('Docker Push') {
            when {
                expression { env.DOCKER_AVAILABLE == "true" }
            }
            steps {
                script {
                    // 1. Vérification des prérequis
                    def dockerUser = credentials('dockerhub-credentials')?.username ?: error("Docker Hub username non trouvé")
                    def dockerPassword = credentials('dockerhub-credentials')?.password ?: error("Docker Hub password non trouvé")

                    // 2. Configuration du nom d'image
                    def imageName = "${dockerUser}/${CONTAINER_NAME}:${CONTAINER_TAG}"

                    // 3. Opérations Docker sécurisées
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_HUB_USER',
                        passwordVariable: 'DOCKER_HUB_PASSWORD'
                    )]) {
                        try {
                            // Version sécurisée du login (évite l'exposition du password dans l'historique)
                            sh """
                                echo ${dockerPassword} | docker login -u ${dockerUser} --password-stdin
                                docker tag ${CONTAINER_NAME}:${CONTAINER_TAG} ${imageName}
                                docker push ${imageName}
                            """

                            // Optionnel : Tag supplémentaire pour la branche master
                            if (env.BRANCH_NAME == 'master') {
                                sh """
                                    docker tag ${CONTAINER_NAME}:${CONTAINER_TAG} ${dockerUser}/${CONTAINER_NAME}:latest
                                    docker push ${dockerUser}/${CONTAINER_NAME}:latest
                                """
                            }
                        } catch (Exception e) {
                            error "Échec du push Docker: ${e.getMessage()}"
                        } finally {
                            // Nettoyage sécurisé
                            sh "docker logout || true"
                            sh "unset DOCKER_HUB_USER DOCKER_HUB_PASSWORD || true"
                        }
                    }
                }
            }
        }

    } catch (Exception e) {
        currentBuild.result = 'FAILURE'
        echo "Erreur: ${e.getMessage()}"
        error "Échec du pipeline"
    } finally {
        deleteDir()
        sendEmail(EMAIL_RECIPIENTS)
    }
}

// Fonctions utilitaires
def sendEmail(recipients) {
    mail(
        to: recipients,
        subject: "Build ${env.BUILD_NUMBER} - ${currentBuild.currentResult}",
        body: """
            Résultat: ${currentBuild.currentResult}
            Détails: ${env.BUILD_URL}/console
            Branch: ${env.BRANCH_NAME}
            Docker: ${env.DOCKER_AVAILABLE == "true" ? "Disponible" : "Indisponible"}
        """
    )
}

String getEnvName(String branchName) {
    if (!branchName) return 'dev'
    return (branchName == 'master') ? 'prod' :
           (branchName == 'develop') ? 'uat' : 'dev'
}

String getHTTPPort(String branchName) {
    if (!branchName) return '9001'
    return (branchName == 'master') ? '9003' :
           (branchName == 'develop') ? '9002' : '9001'
}

String getTag(String buildNumber, String branchName) {
    def safeBranch = branchName ?: "unknown"
    safeBranch = safeBranch.replaceAll('[^a-zA-Z0-9-]', '-')
    return (safeBranch == 'master') ?
           "${buildNumber}-stable" :
           "${buildNumber}-${safeBranch}-unstable"
}