FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
# Normalize line endings for builds from Windows checkouts.
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
COPY src src
# Run the client tests before packaging the application.
RUN ./mvnw clean package -B

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/productclient-0.0.1-SNAPSHOT.jar app.jar
USER 10001:10001
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
