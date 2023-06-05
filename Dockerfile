FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /data
COPY . /data

RUN mvn clean install

FROM openjdk:17-oracle
WORKDIR /data
COPY --from=build /data/target/deduplication-service-example-1.0-SNAPSHOT.jar /data/deduplication.jar

ENTRYPOINT ["/bin/sh", "-c", "java -jar deduplication.jar"]