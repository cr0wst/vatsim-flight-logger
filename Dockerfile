FROM openjdk:15-jdk-alpine

COPY target/data-aggregator*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080