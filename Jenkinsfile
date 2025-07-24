// Définir EMAIL_RECIPIENTS en dehors du bloc try pour qu'il soit accessible partout
def EMAIL_RECIPIENTS = "magassakara@gmail.com"

node {
    try {
        // Configuration des outils avec fallback
        def mavenHome = tool name: 'M3', type: 'maven'
        def jdkHome = tool name: 'JDK-21', type: 'jdk'

        // Tentative de configuration Docker avec gestion d'erreur
        def dockerHome = null
        try {
            dockerHome = tool name: 'docker', type: 'docker'
            env.PATH = "${dockerHome}/bin:${mavenHome}/bin:${jdkHome}/bin:${env.PATH}"
            env.DOCKER_AVAILABLE = "true"
            } catch (Exception e) {
                    echo "ATTENTION: Docker n'est pas configuré dans Jenkins"
                    env.DOCKER_AVAILABLE = "false"
                    env.PATH = "${mavenHome}/bin:${jdkHome}/bin:${env.PATH}"
                }

                def HTTP_PORT = getHTTPPort(env.BRANCH_NAME)
                def ENV_NAME = getEnvName(env.BRANCH_NAME)
                def CONTAINER_NAME = "poseidon-app"
                def CONTAINER_TAG = getTag(env.BUILD_NUMBER, env.BRANCH_NAME)

                // Configuration Java/Maven
                env.JAVA_HOME = jdkHome
                env.MAVEN_HOME = mavenHome

        stage('Checkout') {
            checkout scm
        }

        stage('Environment Setup') {
                    script {
                        echo "MAVEN_HOME: ${mavenHome}"
                        echo "JAVA_HOME: ${jdkHome}"
                        echo "Docker disponible: ${env.DOCKER_AVAILABLE}"
                        echo "Branch: ${env.BRANCH_NAME}"

                        sh 'mvn --version'
                        sh 'java -version'

                        if (env.DOCKER_AVAILABLE == "true") {
                            sh 'docker --version'
                        }
                    }
                }

        stage('Environment Setup') {
            script {
                echo "MAVEN_HOME: ${mavenHome}"
                echo "JAVA_HOME: ${jdkHome}"
                echo "DOCKER_HOME: ${dockerHome}"
                echo "Branch: ${env.BRANCH_NAME}"
                echo "Environment: ${ENV_NAME}"
                echo "HTTP Port: ${HTTP_PORT}"
                echo "Container: ${CONTAINER_NAME}:${CONTAINER_TAG}"

                // Vérification des outils
                sh 'mvn --version'
                sh 'java -version'
                sh 'docker --version'
            }
        }

        stage('Build & Test') {
            sh "mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install"
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

        stage('Docker Setup') {
            script {
                try {
                    sh """
                        docker --version
                        docker-compose --version || echo "docker-compose non installé"
                    """
                    env.DOCKER_AVAILABLE = "true"
                } catch (Exception e) {
                    echo "ATTENTION: Docker n'est pas installé ou accessible sur cet agent Jenkins"
                    env.DOCKER_AVAILABLE = "false"
                    currentBuild.result = 'UNSTABLE'
                }
            }
        }

        stage("Clean Docker Environment") {
            when {
                expression { env.DOCKER_AVAILABLE == "true" }
            }
            steps {
                script {
                    imagePrune(CONTAINER_NAME)
                }
            }
        }

        stage('Build Docker Image') {
            when {
                expression { env.DOCKER_AVAILABLE == "true" }
            }
            steps {
                script {
                    imageBuild(CONTAINER_NAME, CONTAINER_TAG)
                }
            }
        }

        stage('Push to Docker Registry') {
            when {
                expression { env.DOCKER_AVAILABLE == "true" }
            }
            steps {
                script {
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhubcredentials',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )]) {
                        pushToImage(CONTAINER_NAME, CONTAINER_TAG, DOCKER_USER, DOCKER_PASSWORD, DOCKER_REGISTRY)

                        // Tag supplémentaire pour master
                        if (env.BRANCH_NAME == 'master') {
                            sh """
                                docker tag ${CONTAINER_NAME}:${CONTAINER_TAG} ${DOCKER_USER}/${CONTAINER_NAME}:latest
                                docker push ${DOCKER_USER}/${CONTAINER_NAME}:latest
                            """
                        }
                    }
                }
            }
        }

        stage('Deploy Application') {
            when {
                expression { env.DOCKER_AVAILABLE == "true" }
            }
            steps {
                script {
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhubcredentials',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )]) {
                        runApp(CONTAINER_NAME, CONTAINER_TAG, DOCKER_USER, HTTP_PORT, ENV_NAME)
                    }
                }
            }
        }

        stage('Verify Deployment') {
            when {
                expression { env.DOCKER_AVAILABLE == "true" }
            }
            steps {
                script {
                    timeout(time: 2, unit: 'MINUTES') {
                        waitUntil {
                            try {
                                def status = sh(
                                    script: "curl -s -o /dev/null -w '%{http_code}' http://localhost:${HTTP_PORT}/actuator/health",
                                    returnStdout: true
                                ).trim()
                                return status == "200"
                            } catch (Exception e) {
                                echo "Application not ready yet..."
                                return false
                            }
                        }
                        echo "Application is up and running!"
                    }
                }
            }
        }

    } catch (Exception e) {
        currentBuild.result = 'FAILURE'
        echo "Pipeline failed: ${e.getMessage()}"
        throw e
    } finally {
        // Nettoyage
        script {
            if (env.DOCKER_AVAILABLE == "true") {
                sh "docker logout ${DOCKER_REGISTRY} || true"
            }
            deleteDir()
        }
        sendEmail(EMAIL_RECIPIENTS)
    }
}

// Fonctions utilitaires

def sendEmail(recipients) {
    mail(
        to: recipients,
        subject: "Build ${env.BUILD_NUMBER} - ${currentBuild.currentResult} - (${currentBuild.fullDisplayName})",
        body: """
            Résultat du build: ${currentBuild.currentResult}
            URL du build: ${env.BUILD_URL}
            Branch: ${env.BRANCH_NAME}
            Détails: ${env.BUILD_URL}/console
        """
    )
}

def imagePrune(containerName) {
    try {
        sh """
            docker stop ${containerName} || true
            docker rm ${containerName} || true
            docker system prune -f || true
        """
    } catch (Exception e) {
        echo "Erreur lors du nettoyage Docker: ${e.getMessage()}"
    }
}

def imageBuild(containerName, tag) {
    try {
        sh """
            docker build \
                -t ${containerName}:${tag} \
                --pull \
                --no-cache \
                --build-arg BUILD_NUMBER=${env.BUILD_NUMBER} \
                --build-arg GIT_COMMIT=${env.GIT_COMMIT} \
                .
        """
    } catch (Exception e) {
        echo "Erreur lors du build Docker: ${e.getMessage()}"
        throw e
    }
}

def pushToImage(containerName, tag, dockerUser, dockerPassword, registry) {
    try {
        sh """
            echo '${dockerPassword}' | docker login -u ${dockerUser} --password-stdin ${registry}
            docker tag ${containerName}:${tag} ${dockerUser}/${containerName}:${tag}
            docker push ${dockerUser}/${containerName}:${tag}
        """
    } catch (Exception e) {
        echo "Erreur lors du push Docker: ${e.getMessage()}"
        throw e
    }
}

def runApp(containerName, tag, dockerUser, httpPort, envName) {
    try {
        // Pull de la dernière image
        timeout(time: 5, unit: 'MINUTES') {
            sh "docker pull ${dockerUser}/${containerName}:${tag}"
        }

        // Démarrer le conteneur avec vérification de santé
        sh """
            docker run \
                --name ${containerName} \
                --env SPRING_PROFILES_ACTIVE=${envName} \
                -p ${httpPort}:${httpPort} \
                -d \
                --health-cmd="curl -f http://localhost:${httpPort}/actuator/health || exit 1" \
                --health-interval=5s \
                --health-retries=3 \
                --health-timeout=2s \
                ${dockerUser}/${containerName}:${tag}
        """

        // Vérification du statut
        sleep(time: 5, unit: 'SECONDS')
        def health = sh(
            script: "docker inspect --format='{{.State.Health.Status}}' ${containerName}",
            returnStdout: true
        ).trim()

        if (health != "healthy") {
            error "Le conteneur n'est pas healthy (statut: ${health})"
        }
    } catch (Exception e) {
        // Afficher les logs en cas d'erreur
        sh "docker logs ${containerName} || true"
        throw e
    }
}

// Helpers

String getEnvName(String branchName) {
    switch(branchName) {
        case 'master': return 'prod'
        case 'develop': return 'uat'
        default: return 'dev'
    }
}

String getHTTPPort(String branchName) {
    switch(branchName) {
        case 'master': return '9003'
        case 'develop': return '9002'
        default: return '9001'
    }
}

String getTag(String buildNumber, String branchName) {
    return (branchName == 'master') ?
        "${buildNumber}-stable" :
        "${buildNumber}-${branchName.replace('/', '-')}-unstable"
}