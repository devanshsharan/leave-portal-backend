FROM openjdk:24-slim-bullseye
ADD target/leave-portal.jar leave-portal.jar
ENTRYPOINT ["java", "-jar", "/leave-portal.jar"]

