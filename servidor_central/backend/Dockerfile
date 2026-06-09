# Build
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copia só o pom.xml primeiro — aproveita cache se as deps não mudarem
COPY pom.xml .

RUN mvn dependency:go-offline -q

# Agora copia o código e compila
COPY src ./src

RUN mvn clean package -DskipTests -q

# Run
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/servidor-central-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]