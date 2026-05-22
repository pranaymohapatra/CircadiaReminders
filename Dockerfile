FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /home/app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts gradle.properties ./

RUN chmod +x ./gradlew && ./gradlew --no-daemon -v

COPY src ./src
RUN ./gradlew --no-daemon clean shadowJar

FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /home/app

COPY --from=build /home/app/build/libs/*-all.jar ./app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
