FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src src
RUN ./mvnw -B -DskipTests package
FROM eclipse-temurin:17-jre
RUN groupadd --system plaza && useradd --system --gid plaza --home-dir /app plaza && mkdir -p /app/data/uploads && chown -R plaza:plaza /app
WORKDIR /app
COPY --from=build --chown=plaza:plaza /app/target/pena-plaza-portal-0.0.1-SNAPSHOT.jar app.jar
USER plaza
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
