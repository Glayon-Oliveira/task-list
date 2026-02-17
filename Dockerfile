FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /usr/local/app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src src
RUN ./mvnw clean install -Dmaven.test.skip=true

FROM eclipse-temurin:17-jdk-jammy AS dev
WORKDIR /usr/local/app
COPY wait-for-it.sh ./
RUN chmod +x wait-for-it.sh
COPY .mvn .mvn
COPY mvnw ./

FROM eclipse-temurin:17-jre-jammy AS final
WORKDIR /opt/app
COPY wait-for-it.sh ./
RUN chmod +x wait-for-it.sh
COPY --from=builder /usr/local/app/target/task-list.jar /opt/app/task-list.jar
ENTRYPOINT ["java", "-jar", "/opt/app/task-list.jar"]
