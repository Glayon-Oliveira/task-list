# Tasklist Project

- [Introduction](#introduction)
- [How to Use](#how-to-use)
  - [Docker](#docker)
  - [Running Without Docker](#running-without-docker)
- [OpenAPI](#openapi)
- [Main Technologies](#main-technologies)
- [About Deployment / Production](#about-deployment--production)
- [License](#license)

## Introduction

This repository contains the implementation of a relatively simple task list web application built with Spring Boot.
Its purpose is to serve as a study environment for experimenting with different technologies and implementation practices.

It offers the following features:

- Security using JWT tokens with internally generated keys
- Users and email support (without OAuth2)
- Ordered tasks and subtasks
- Optional support for ETag and request versioning

## How to Use

### Docker

You can use Docker to run the project. The repository includes a Dockerfile and two docker-compose files:

- **`docker-compose.yml`** — main configuration
- **`docker-compose.override.yml`** — additional configuration for development

Since an override file is present, you must explicitly specify the main compose file if you want to run it alone.

Alternatively, you can use the `tasklist.sh` script, which simplifies working with both files.

#### Docker Image

A prebuilt Docker image is also available for this project:

```docker pull lmlasmo/tasklist:latest```

This image is automatically updated from the `main` branch through a CI/CD pipeline configured with GitHub Actions.
It always reflects the latest state of the project and can be used for quick testing, experimentation, or integration into other study environments.

Although it is not intended for direct use in production, the image demonstrates a functional build and continuous delivery flow within a real pipeline.

### Running Without Docker

If you prefer to run the application without Docker, make sure the required environment variables are configured — you can refer to the main `docker-compose.yml` or the `application.properties` file.

After that, you can start the application with:

```
./mvnw spring-boot:run
```

Or, directly using Maven:

```
mvn clean install -DskipTests
```

## OpenAPI

The project exposes OpenAPI documentation through the **doc** profile, which is disabled by default. To enable it, simply set the `SPRING_PROFILES_ACTIVE` environment variable.

The development docker-compose file already enables this profile automatically.

The documentation can be accessed at:

```
http://host:port/swagger-ui/index
```

It includes a brief description of each endpoint.

## Main Technologies

This project uses a set of technologies from the Spring ecosystem and supporting tools for building reactive APIs, security, persistence, and documentation:

- Spring Boot (3.5)
- WebFlux
- Spring Security + OAuth2 Resource Server
- R2DBC
- Flyway
- MySQL — Execution environment
- H2 — Testing environment
- Spring Mail
- MapStruct
- Lombok
- Springdoc OpenAPI (WebFlux UI)
- Testing libraries

## About Deployment / Production

The `docker-compose.yml` file provides the configuration closest to a production-like environment, including the database setup and essential environment variables.

However, this project was not designed to be run directly in a production environment. Its purpose is to serve as a study-friendly and extensible foundation.

## License

This is a personal project and does not include a formal license.
You are welcome to review the code, study it, and adapt ideas as needed, but there is no guarantee of support, maintenance, or suitability for commercial use.
