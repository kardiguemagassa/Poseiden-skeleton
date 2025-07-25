@Library('shared-library') _

// Configuration centralisée
def config = [
    emailRecipients: "magassakara@gmail.com",
    containerName: "poseidon-app",
    dockerRegistry: "docker.io",
    dockerHome: '/usr/local/bin',
    sonarProjectKey: "Poseidon-skeleton",
    timeouts: [
        qualityGate: 2,
        deployment: 5
    ],
    ports: [
        master: '9003',
        develop: '9002',
        default: '9001'
    ],
    environments: [
        master: 'prod',
        develop: 'uat',
        default: 'dev'
    ]
]

pipeline {
    agent any

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        skipDefaultCheckout(true)
        timestamps()
        ansiColor('xterm')
    }

    tools {
        maven 'M3'
        jdk 'JDK-21'
    }

    environment {
        DOCKER_BUILDKIT = "1"
        COMPOSE_DOCKER_CLI_BUILD = "1"
        // Variables calculées dynamiquement
        BRANCH_NAME = "${env.BRANCH_NAME ?: 'unknown'}"
        BUILD_NUMBER = "${env.BUILD_NUMBER ?: '0'}"
        HTTP_PORT = "${getHTTPPort(env.BRANCH_NAME, config.ports)}"
        ENV_NAME = "${getEnvName(env.BRANCH_NAME, config.environments)}"
        CONTAINER_TAG = "${getTag(env.BUILD_NUMBER, env.BRANCH_NAME)}"
    }

    stages {
        stage('Checkout & Setup') {
            steps {
                script {
                    // Checkout du code
                    checkout scm

                    // Vérification de Docker avec retry
                    env.DOCKER_AVAILABLE = checkDockerAvailability()

                    // Affichage de la configuration
                    displayBuildInfo(config)
                }
            }
        }

        stage('Build & Test') {
            steps {
                script {
                    sh """
                        mvn clean verify \
                            org.jacoco:jacoco-maven-plugin:prepare-agent \
                            -DskipTests=false \
                            -Dmaven.test.failure.ignore=false \
                            -Dmaven.repo.local=\${WORKSPACE}/.m2/repository \
                            -B -U
                    """
                }
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                    publishCoverage adapters: [
                        jacocoAdapter('target/site/jacoco/jacoco.xml')
                    ], sourceFileResolver: sourceFiles('STORE_LAST_BUILD')
                }
            }
        }

        stage('Code Analysis') {
            when {
                anyOf {
                    branch 'master'
                    branch 'develop'
                    changeRequest()
                }
            }
            steps {
                withSonarQubeEnv('SonarQube') {
                    withCredentials([string(credentialsId: 'sonartoken', variable: 'SONAR_TOKEN')]) {
                        sh """
                            mvn sonar:sonar \
                                -Dsonar.projectKey=${config.sonarProjectKey} \
                                -Dsonar.host.url=\$SONAR_HOST_URL \
                                -Dsonar.token=\${SONAR_TOKEN} \
                                -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                                -Dsonar.java.binaries=target/classes \
                                -Dsonar.branch.name=${env.BRANCH_NAME} \
                                -B
                        """
                    }
                }
            }
        }

        stage('Quality Gate') {
            when {
                anyOf {
                    branch 'master'
                    branch 'develop'
                    changeRequest()
                }
            }
            steps {
                timeout(time: config.timeouts.qualityGate, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Docker Build') {
            when {
                anyOf {
                    branch 'master'
                    branch 'develop'
                }
            }
            steps {
                script {
                    validateDockerPrerequisites()
                    buildDockerImage(config)
                }
            }
        }

        stage('Docker Push') {
            when {
                anyOf {
                    branch 'master'
                    branch 'develop'
                }
            }
            steps {
                script {
                    pushDockerImage(config)
                }
            }
        }

        stage('Deploy') {
            when {
                anyOf {
                    branch 'master'
                    branch 'develop'
                }
            }
            steps {
                script {
                    deployApplication(config)
                }
            }
        }

        stage('Health Check') {
            when {
                anyOf {
                    branch 'master'
                    branch 'develop'
                }
            }
            steps {
                script {
                    performHealthCheck()
                }
            }
        }
    }

    post {
        always {
            script {
                // Nettoyage des images Docker locales
                cleanupDockerImages()

                // Archivage des artefacts
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, allowEmptyArchive: true

                // Nettoyage du workspace
                cleanWs()

                // Envoi de notification
                sendNotification(config.emailRecipients)
            }
        }
        failure {
            script {
                echo "❌ Pipeline échoué - Vérifiez les logs ci-dessus"
            }
        }
        success {
            script {
                echo "✅ Pipeline réussi - Application déployée avec succès"
            }
        }
        unstable {
            script {
                echo "⚠️ Pipeline instable - Vérifiez les avertissements"
            }
        }
    }
}

// =============================================================================
// FONCTIONS UTILITAIRES
// =============================================================================

def checkDockerAvailability() {
    try {
        def result = sh(
            script: '''
                # Vérification avec retry
                for i in {1..3}; do
                    if command -v docker >/dev/null 2>&1; then
                        if docker info >/dev/null 2>&1; then
                            echo "true"
                            exit 0
                        fi
                    fi
                    echo "Tentative $i/3 échouée, retry dans 5s..."
                    sleep 5
                done
                echo "false"
            ''',
            returnStdout: true
        ).trim()

        if (result == "true") {
            echo "✅ Docker disponible et fonctionnel"
            sh 'docker --version'
        } else {
            echo "❌ Docker non disponible ou non fonctionnel"
        }

        return result
    } catch (Exception e) {
        echo "❌ Erreur lors de la vérification Docker: ${e.getMessage()}"
        return "false"
    }
}

def displayBuildInfo(config) {
    echo """
    ╔══════════════════════════════════════════════════════════════════════════════╗
    ║                            CONFIGURATION BUILD                               ║
    ╠══════════════════════════════════════════════════════════════════════════════╣
    ║ 🏗️  Build #: ${env.BUILD_NUMBER}
    ║ 🌿 Branch: ${env.BRANCH_NAME}
    ║ ☕ Java: ${env.JAVA_HOME}
    ║ 📦 Maven: ${env.MAVEN_HOME}
    ║ 🐳 Docker: ${env.DOCKER_AVAILABLE == "true" ? "✅ Disponible" : "❌ Indisponible"}
    ║ 🌍 Environnement: ${env.ENV_NAME}
    ║ 🚪 Port: ${env.HTTP_PORT}
    ║ 🏷️  Tag: ${env.CONTAINER_TAG}
    ║ 📧 Email: ${config.emailRecipients}
    ╚══════════════════════════════════════════════════════════════════════════════╝
    """
}

def validateDockerPrerequisites() {
    if (env.DOCKER_AVAILABLE != "true") {
        error "🚫 Docker n'est pas disponible. Impossible de continuer."
    }

    if (!fileExists('Dockerfile')) {
        error "🚫 Fichier Dockerfile introuvable à la racine du projet."
    }

    def jarFiles = findFiles(glob: 'target/*.jar').findAll {
        it.name.endsWith('.jar') && !it.name.contains('sources') && !it.name.contains('javadoc')
    }

    if (jarFiles.length == 0) {
        error "🚫 Aucun fichier JAR exécutable trouvé dans target/"
    }

    env.JAR_FILE = jarFiles[0].path
    echo "✅ JAR trouvé: ${env.JAR_FILE}"
}

def buildDockerImage(config) {
    try {
        echo "🏗️ Construction de l'image Docker..."

        sh """
            docker build \
                --pull \
                --no-cache \
                --build-arg JAR_FILE=${env.JAR_FILE} \
                --build-arg BUILD_DATE="\$(date -u +'%Y-%m-%dT%H:%M:%SZ')" \
                --build-arg VCS_REF="\$(git rev-parse --short HEAD)" \
                --build-arg BUILD_NUMBER="${env.BUILD_NUMBER}" \
                --label "org.opencontainers.image.created=\$(date -u +'%Y-%m-%dT%H:%M:%SZ')" \
                --label "org.opencontainers.image.revision=\$(git rev-parse --short HEAD)" \
                --label "org.opencontainers.image.version=${env.CONTAINER_TAG}" \
                -t "${config.containerName}:${env.CONTAINER_TAG}" \
                .
        """

        echo "✅ Image Docker construite avec succès"

        // Vérification de l'image
        sh "docker images ${config.containerName}:${env.CONTAINER_TAG}"

    } catch (Exception e) {
        error "🚫 Échec de la construction Docker: ${e.getMessage()}"
    }
}

def pushDockerImage(config) {
    try {
        withCredentials([usernamePassword(
            credentialsId: 'dockerhub-credentials',
            usernameVariable: 'DOCKER_USER',
            passwordVariable: 'DOCKER_PASSWORD'
        )]) {

            echo "🚀 Connexion au registre Docker..."
            sh """
                echo "\${DOCKER_PASSWORD}" | docker login -u "\${DOCKER_USER}" --password-stdin ${config.dockerRegistry}
            """

            echo "🏷️ Tagging de l'image..."
            sh """
                docker tag "${config.containerName}:${env.CONTAINER_TAG}" "\${DOCKER_USER}/${config.containerName}:${env.CONTAINER_TAG}"
            """

            echo "📤 Push de l'image..."
            sh """
                docker push "\${DOCKER_USER}/${config.containerName}:${env.CONTAINER_TAG}"
            """

            // Tag latest pour master
            if (env.BRANCH_NAME == 'master') {
                echo "🏷️ Tagging latest pour master..."
                sh """
                    docker tag "${config.containerName}:${env.CONTAINER_TAG}" "\${DOCKER_USER}/${config.containerName}:latest"
                    docker push "\${DOCKER_USER}/${config.containerName}:latest"
                """
            }

            echo "🔒 Déconnexion du registre..."
            sh "docker logout ${config.dockerRegistry}"

            echo "✅ Image poussée avec succès"
        }
    } catch (Exception e) {
        error "🚫 Échec du push Docker: ${e.getMessage()}"
    }
}

def deployApplication(config) {
    try {
        withCredentials([usernamePassword(
            credentialsId: 'dockerhub-credentials',
            usernameVariable: 'DOCKER_USER',
            passwordVariable: 'DOCKER_PASSWORD'
        )]) {

            echo "🛑 Arrêt du conteneur existant..."
            sh """
                docker stop ${config.containerName} 2>/dev/null || echo "Conteneur non trouvé"
                docker rm ${config.containerName} 2>/dev/null || echo "Conteneur non trouvé"
            """

            echo "🚀 Démarrage du nouveau conteneur..."
            sh """
                docker run -d \
                    --name "${config.containerName}" \
                    --restart unless-stopped \
                    -p "${env.HTTP_PORT}:8080" \
                    -e "SPRING_PROFILES_ACTIVE=${env.ENV_NAME}" \
                    -e "SERVER_PORT=8080" \
                    -e "JAVA_OPTS=-Xmx512m -Xms256m" \
                    --health-cmd="curl -f http://localhost:8080/actuator/health || exit 1" \
                    --health-interval=30s \
                    --health-timeout=10s \
                    --health-retries=3 \
                    --health-start-period=40s \
                    "\${DOCKER_USER}/${config.containerName}:${env.CONTAINER_TAG}"
            """

            echo "✅ Conteneur démarré avec succès"
        }
    } catch (Exception e) {
        error "🚫 Échec du déploiement: ${e.getMessage()}"
    }
}

def performHealthCheck() {
    try {
        echo "🩺 Vérification de la santé de l'application..."

        timeout(time: config.timeouts.deployment, unit: 'MINUTES') {
            waitUntil {
                script {
                    def status = sh(
                        script: "docker inspect -f '{{.State.Health.Status}}' ${config.containerName} 2>/dev/null || echo 'no-health'",
                        returnStdout: true
                    ).trim()

                    echo "Status de santé: ${status}"

                    if (status == "healthy") {
                        return true
                    } else if (status == "unhealthy") {
                        error "❌ L'application est en mauvaise santé"
                    }

                    sleep(10)
                    return false
                }
            }
        }

        // Test HTTP additionnel
        sh """
            curl -f "http://localhost:${env.HTTP_PORT}/actuator/health" || {
                echo "❌ Health check HTTP échoué"
                docker logs ${config.containerName} --tail 50
                exit 1
            }
        """

        echo "✅ Application en bonne santé et accessible"

    } catch (Exception e) {
        // Logs pour debug
        sh "docker logs ${config.containerName} --tail 100 || echo 'Impossible de récupérer les logs'"
        error "🚫 Health check échoué: ${e.getMessage()}"
    }
}

def cleanupDockerImages() {
    try {
        if (env.DOCKER_AVAILABLE == "true") {
            echo "🧹 Nettoyage des images Docker..."
            sh """
                # Suppression des images non taguées
                docker image prune -f || true

                # Garde seulement les 3 dernières versions de notre image
                docker images "${config.containerName}" --format "table {{.Repository}}:{{.Tag}}\t{{.CreatedAt}}" | \
                tail -n +2 | sort -k2 -r | tail -n +4 | awk '{print \$1}' | \
                xargs -r docker rmi || true
            """
        }
    } catch (Exception e) {
        echo "⚠️ Erreur lors du nettoyage Docker: ${e.getMessage()}"
    }
}

def sendNotification(recipients) {
    try {
        def cause = currentBuild.getBuildCauses()?.collect { it.shortDescription }?.join(', ') ?: "Non spécifiée"
        def duration = currentBuild.durationString.replace(' and counting', '')
        def status = currentBuild.currentResult ?: 'SUCCESS'

        def statusIcon = [
            'SUCCESS': '✅',
            'FAILURE': '❌',
            'UNSTABLE': '⚠️',
            'ABORTED': '🛑'
        ][status] ?: '❓'

        def subject = "${statusIcon} [Jenkins] ${env.JOB_NAME} - Build #${env.BUILD_NUMBER} - ${status}"

        def body = """
        ${statusIcon} Résultat: ${status}

        📊 Détails du Build:
        • Projet: ${env.JOB_NAME}
        • Build: #${env.BUILD_NUMBER}
        • Branche: ${env.BRANCH_NAME ?: 'N/A'}
        • Durée: ${duration}
        • Environnement: ${env.ENV_NAME}
        • Port: ${env.HTTP_PORT}

        🔗 Liens:
        • Console: ${env.BUILD_URL}console
        • Artefacts: ${env.BUILD_URL}artifact/

        🐳 Docker: ${env.DOCKER_AVAILABLE == "true" ? "✅ Disponible" : "❌ Indisponible"}
        🚀 Cause: ${cause}

        ${status == 'SUCCESS' ? '🎉 Déploiement réussi!' : '🔍 Vérifiez les logs pour plus de détails.'}
        """

        mail(
            to: recipients,
            subject: subject,
            body: body,
            mimeType: 'text/plain'
        )

        echo "📧 Email de notification envoyé à: ${recipients}"

    } catch (Exception e) {
        echo "⚠️ Échec de l'envoi d'email: ${e.getMessage()}"
    }
}

// Fonctions utilitaires pour la configuration
String getEnvName(String branchName, Map environments) {
    def branch = branchName?.toLowerCase()
    return environments[branch] ?: environments.default
}

String getHTTPPort(String branchName, Map ports) {
    def branch = branchName?.toLowerCase()
    return ports[branch] ?: ports.default
}

String getTag(String buildNumber, String branchName) {
    def safeBranch = (branchName ?: "unknown")
        .replaceAll('[^a-zA-Z0-9-]', '-')
        .toLowerCase()

    return (safeBranch == 'master') ?
        "${buildNumber}-stable" :
        "${buildNumber}-${safeBranch}-snapshot"
}