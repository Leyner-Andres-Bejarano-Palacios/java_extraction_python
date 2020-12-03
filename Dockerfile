FROM maven:3.6.3-jdk-8
#FROM maven:3-openjdk-8-slim AS builder
RUN java -version

COPY . /usr/src/myapp/
WORKDIR /usr/src/myapp/
RUN mvn package
ENTRYPOINT ["java", "-cp", "target/my-app-1.0-SNAPSHOT.jar", "com.mycompany.app.App"]

