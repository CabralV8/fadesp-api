
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app


COPY pom.xml .
RUN mvn -B -q -e -DskipTests dependency:go-offline


COPY src ./src
RUN mvn -B -q -DskipTests clean package


FROM eclipse-temurin:17-jre
WORKDIR /app


RUN useradd -ms /bin/bash spring && chown -R spring:spring /app
USER spring


ARG JAR_FILE=/app/target/*.jar
COPY --from=build ${JAR_FILE} app.jar


ENV SPRING_PROFILES_ACTIVE=default

ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -XX:ActiveProcessorCount=2"

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]

LABEL version="1.0" \
      description="Imagem Docker da aplicação Payment - FADESP" \
      maintainer="Valberton Viana Cabral da Silva <valberton@gmail.com>" \
      author="Valberton Viana Cabral da Silva"
