FROM openjdk:23-jdk-slim

RUN addgroup --system app && adduser --system --ingroup app app
USER app
WORKDIR /app

COPY --chown=app:app /target/*.jar /app/app.jar
EXPOSE 8888
ENV TZ="UTC"
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/app.jar"]
