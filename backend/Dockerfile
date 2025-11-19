FROM gradle:jdk17 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY src ./src
RUN gradle clean bootJar -x test

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
CMD ["java", "-jar", "app.jar"]
