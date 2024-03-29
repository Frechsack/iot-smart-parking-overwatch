#
# Build stage
#
FROM maven:3.8.7-amazoncorretto-17 AS Build
COPY src /opt/src
COPY pom.xml /opt
# Erstellt .jar mit allen Varianten von cpp für sämtliche Plattformen.
# RUN mvn -f /home/app/pom.xml clean package
# Erstellt .har mit jenen Varianten von cpp für linux auf Intel/Amd und ARM Basis.
RUN mvn -Djavacpp.platform.custom -Djavacpp.platform.linux-arm64 -Djavacpp.platform.linux-x86_64 -f /opt/pom.xml clean package

#
# Package stage
#
FROM debian:bullseye-slim
COPY --from=Build /opt/target/iot-smart-parking-overwatch-rolling-jar-with-dependencies.jar /opt/iot-smart-parking-overwatch.jar
RUN apt-get -y update
RUN apt-get -y install openjdk-17-jre-headless libgtk2.0-0
EXPOSE 8080
ENTRYPOINT ["java","-jar","/opt/iot-smart-parking-overwatch.jar"]
