#
# Build stage
#
FROM maven:3.8.7-amazoncorretto-17 AS build
COPY src /opt/src
COPY pom.xml /opt
#RUN mvn -f /home/app/pom.xml clean package
RUN mvn -Djavacpp.platform.custom -Djavacpp.platform.linux-arm64 -Djavacpp.platform.linux-x86_64 -f /opt/pom.xml clean package

#
# Package stage
#
FROM eclipse-temurin:17.0.6_10-jre-jammy
COPY --from=build /opt/target/iot-smart-parking-overwatch-rolling-jar-with-dependencies.jar /opt/iot-smart-parking-overwatch.jar
RUN apt-get -y update
RUN apt-get -y install libgtk2.0-0
EXPOSE 8080
ENTRYPOINT ["java","-jar","/opt/iot-smart-parking-overwatch.jar"]