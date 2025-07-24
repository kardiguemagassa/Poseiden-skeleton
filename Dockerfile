# Utilise une image plus légère et sécurisée (avec Java 21 pour correspondre à votre projet)
FROM eclipse-temurin:21-jre-alpine

# Définit l'utilisateur non-root pour plus de sécurité
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /opt/app

# Copie du JAR (le nom générique est préférable)
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Copie du script entrypoint
COPY entrypoint.sh .

# Vérification des permissions
RUN chmod +x entrypoint.sh

# Exposition du port (à adapter selon votre application)
EXPOSE 8080

# Point d'entrée
ENTRYPOINT ["./entrypoint.sh"]