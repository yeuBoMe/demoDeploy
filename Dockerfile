FROM openjdk:21-jdk-slim

WORKDIR /app

RUN apt-get update && apt-get install -y postgresql-client

COPY . .

RUN chmod +x mvnw

RUN ./mvnw clean package -DskipTests

CMD ["java", "-jar", "target/demoComputer-0.0.1-SNAPSHOT.jar"]
