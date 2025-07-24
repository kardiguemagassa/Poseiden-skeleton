node {
    // Définir les outils au début du node
    def MAVEN_HOME = tool name: 'M3', type: 'maven'
    def JAVA_HOME = tool name: 'JDK-21', type: 'jdk'

    // Configurer l'environnement PATH
    env.PATH = "${MAVEN_HOME}/bin:${JAVA_HOME}/bin:${env.PATH}"
    env.JAVA_HOME = JAVA_HOME
    env.MAVEN_HOME = MAVEN_HOME

    stage('Checkout') {
        // Vérification de la branche et du commit
        checkout scm
    }

    stage('Check Maven') {
        // Supprimer le bloc 'steps' - pas nécessaire en syntaxe scriptée
        script {
            // Affiche l'emplacement de Maven
            echo "MAVEN_HOME is: ${MAVEN_HOME}"
            sh 'mvn --version'  // Test pour vérifier que Maven fonctionne
        }
    }

    stage('Build & Test') {
        // Les outils sont déjà définis au début

        // Affichage des variables d'environnement pour le diagnostic
        sh 'echo "JAVA_HOME: $JAVA_HOME"'
        sh 'echo "MAVEN_HOME: $MAVEN_HOME"'

        // Exécution du build avec Maven (utiliser directement mvn)
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
        // Vérification du quality gate
        timeout(time: 2, unit: 'MINUTES') {
            waitForQualityGate abortPipeline: true
        }
    }

    stage('Résultat') {
        // Affichage du résultat du build
        script {
            if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
                echo '✔️ Build, couverture et qualité OK !'
            } else {
                echo '❌ Échec, vérifier les logs et SonarQube.'
            }
        }
    }
}