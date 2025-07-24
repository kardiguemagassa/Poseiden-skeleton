// Définir EMAIL_RECIPIENTS en dehors du bloc try pour qu'il soit accessible partout
def EMAIL_RECIPIENTS = "magassakara@gmail.com"

node {
    try {
        // Définir les outils au début du pipeline
        def mavenHome = tool name: 'M3', type: 'maven'
        def jdkHome = tool name: 'JDK-21', type: 'jdk'
        def HTTP_PORT = getHTTPPort(env.BRANCH_NAME)
        def ENV_NAME = getEnvName(env.BRANCH_NAME)
        def CONTAINER_NAME = "poseidon-app"
        def CONTAINER_TAG = getTag(env.BUILD_NUMBER, env.BRANCH_NAME)

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
                echo "Container: ${CONTAINER_NAME}:${CONTAINER_TAG}"
            }
        }

        stage('Build & Test') {
            // Affichage des variables d'environnement pour le diagnostic
            sh 'echo "JAVA_HOME: $JAVA_HOME"'
            sh 'echo "MAVEN_HOME: $MAVEN_HOME"'
            sh 'echo "PATH: $PATH"'
            sh 'mvn --version'
            // Exécution du build avec Maven (une seule commande suffit)
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

        stage('Docker Availability Check') {
            script {
                try {
                    sh 'docker --version'
                    echo "Docker est disponible"
                    env.DOCKER_AVAILABLE = "true"
                } catch (Exception e) {
                    echo "ATTENTION: Docker n'est pas installé ou accessible sur cet agent Jenkins"
                    echo "Les étapes Docker seront ignorées"
                    env.DOCKER_AVAILABLE = "false"
                    currentBuild.result = 'UNSTABLE'
                }
            }
        }

        stage("Image Prune") {
            script {
                if (env.DOCKER_AVAILABLE == "true") {
                    imagePrune(CONTAINER_NAME)
                } else {
                    echo "Étape Image Prune ignorée - Docker non disponible"
                }
            }
        }

        stage('Image Build') {
            script {
                if (env.DOCKER_AVAILABLE == "true") {
                    imageBuild(CONTAINER_NAME, CONTAINER_TAG)
                } else {
                    echo "Étape Image Build ignorée - Docker non disponible"
                }
            }
        }

        stage('Push to Docker Registry') {
            script {
                if (env.DOCKER_AVAILABLE == "true") {
                    try {
                        withCredentials([usernamePassword(credentialsId: 'dockerhubcredentials', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                            pushToImage(CONTAINER_NAME, CONTAINER_TAG, USERNAME, PASSWORD)
                        }
                    } catch (Exception e) {
                        echo "ATTENTION: Credentials 'dockerhubcredentials' non trouvés. Push vers Docker Hub ignoré."
                        echo "Erreur: ${e.getMessage()}"
                        echo "Veuillez créer les credentials Docker Hub avec l'ID 'dockerhubcredentials' dans Jenkins."
                        currentBuild.result = 'UNSTABLE'
                    }
                } else {
                    echo "Étape Push to Docker Registry ignorée - Docker non disponible"
                }
            }
        }

        stage('Run App') {
            script {
                if (env.DOCKER_AVAILABLE == "true") {
                    // Vérifier si les credentials Docker Hub existent
                    try {
                        withCredentials([usernamePassword(credentialsId: 'dockerhubcredentials', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                            runApp(CONTAINER_NAME, CONTAINER_TAG, USERNAME, HTTP_PORT, ENV_NAME)
                        }
                    } catch (Exception e) {
                        echo "ATTENTION: Impossible de démarrer l'application."
                        echo "Erreur: ${e.getMessage()}"
                        echo "Vérifiez que l'image a été correctement construite et pushée."
                        currentBuild.result = 'UNSTABLE'
                    }
                } else {
                    echo "Étape Run App ignorée - Docker non disponible"
                }
            }
        }

        stage('Résultat') {
            // Affichage du résultat du build
            script {
                if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
                    echo 'Build, couverture et qualité OK !'
                    if (env.DOCKER_AVAILABLE != "true") {
                        echo 'Note: Les étapes Docker ont été ignorées (Docker non disponible)'
                    }
                } else if (currentBuild.result == 'UNSTABLE') {
                    echo 'Build terminé avec des avertissements. Vérifiez les logs.'
                    if (env.DOCKER_AVAILABLE != "true") {
                        echo 'Avertissement: Docker n\'est pas disponible sur cet agent Jenkins'
                    }
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

def imagePrune(containerName) {
    try {
        sh "docker image prune -f"
        sh "docker stop ${containerName} || true"
        sh "docker rm ${containerName} || true"
    } catch (Exception e) {
        echo "Nettoyage des images/conteneurs: ${e.getMessage()}"
    }
}

def imageBuild(containerName, tag) {
    try {
        sh "docker build -t ${containerName}:${tag} --pull --no-cache ."
        echo "Construction de l'image terminée: ${containerName}:${tag}"
    } catch (Exception e) {
        echo "Erreur lors de la construction de l'image: ${e.getMessage()}"
        throw e
    }
}

def pushToImage(containerName, tag, dockerUser, dockerPassword) {
    try {
        sh "docker login -u ${dockerUser} -p ${dockerPassword}"
        sh "docker tag ${containerName}:${tag} ${dockerUser}/${containerName}:${tag}"
        sh "docker push ${dockerUser}/${containerName}:${tag}"
        echo "Push de l'image terminé: ${dockerUser}/${containerName}:${tag}"
    } catch (Exception e) {
        echo "Erreur lors du push de l'image: ${e.getMessage()}"
        throw e
    } finally {
        // Nettoyage des credentials Docker
        sh "docker logout || true"
    }
}

def runApp(containerName, tag, dockerHubUser, httpPort, envName) {
    try {
        // Arrêter et supprimer le conteneur existant s'il existe
        sh "docker stop ${containerName} || true"
        sh "docker rm ${containerName} || true"

        // Tirer la dernière image
        sh "docker pull ${dockerHubUser}/${containerName}:${tag}"

        // Démarrer le nouveau conteneur
        sh "docker run --env SPRING_PROFILES_ACTIVE=${envName} -d -p ${httpPort}:${httpPort} --name ${containerName} ${dockerHubUser}/${containerName}:${tag}"

        echo "Application démarrée sur le port: ${httpPort} (http)"
        echo "Profil Spring actif: ${envName}"

        // Vérifier que le conteneur fonctionne
        sleep(time: 10, unit: 'SECONDS')
        sh "docker ps | grep ${containerName}"

    } catch (Exception e) {
        echo "Erreur lors du démarrage de l'application: ${e.getMessage()}"
        // Afficher les logs du conteneur pour le debug
        sh "docker logs ${containerName} || true"
        throw e
    }
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