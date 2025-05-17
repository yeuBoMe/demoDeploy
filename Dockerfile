# Sử dụng image OpenJDK 21
FROM openjdk:21-jdk-slim

# Thiết lập thư mục làm việc
WORKDIR /app

RUN apt-get update && apt-get install -y postgresql-client

# Copy mã nguồn và file Maven Wrapper
COPY . .

# Cấp quyền thực thi cho mvnw
RUN chmod +x mvnw

# Build ứng dụng
RUN ./mvnw clean package -DskipTests

# Chạy ứng dụng
CMD ["java", "-jar", "target/demoComputer-0.0.1-SNAPSHOT.jar"]