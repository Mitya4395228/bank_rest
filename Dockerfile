FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY target/bankcards-0.0.1-SNAPSHOT.jar bankcards.jar
ENTRYPOINT ["java", "-jar", "bankcards.jar"]