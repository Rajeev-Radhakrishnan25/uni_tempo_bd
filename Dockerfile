FROM eclipse-temurin:23-jdk-alpine
WORKDIR /app
COPY build/libs/unicarpool.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]