FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/source_server.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
