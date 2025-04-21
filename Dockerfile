FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /usr/local/app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw clean dependency:go-offline
COPY src src
RUN ./mvnw clean install

FROM eclipse-temurin:17-jre-jammy AS final
WORKDIR /opt/app
COPY --from=builder /usr/local/app/target/task-list.jar /opt/app/task-list.jar
ENTRYPOINT ["java", "-jar", "/opt/app/task-list.jar"]
