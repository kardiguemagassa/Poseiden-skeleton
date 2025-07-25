#!/bin/sh

echo "Starting application with profile: ${SPRING_ACTIVE_PROFILES:-default}"

# Ajout de paramètres JVM recommandés
exec java \
    -Dspring.profiles.active=${SPRING_ACTIVE_PROFILES:-default} \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+ExitOnOutOfMemoryError \
    -jar app.jar