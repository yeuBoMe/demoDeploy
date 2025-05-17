# Sử dụng image OpenJDK 21
FROM openjdk:21-jdk-slim

# Thiết lập thư mục làm việc
WORKDIR /app

# Cài đặt postgresql-client để kiểm tra kết nối
RUN apt-get update && apt-get install -y postgresql-client

# Copy mã nguồn và file Maven Wrapper
COPY . .

# Cấp quyền thực thi cho mvnw
RUN chmod +x mvnw

# Kiểm tra kết nối database
RUN PGHOST=dpg-d0jge363jp1c739tan60-a.singapore-postgres.render.com \
    PGPORT=5432 \
    PGDATABASE=he \
    PGUSER=admin \
    PGPASSWORD=sEXzbHE9QAHuW3cmLlTZ1o9Qsz8nrzRs \
    psql -c "SELECT 1" || exit 1

# Build ứng dụng
RUN ./mvnw clean package -DskipTests

# Chạy ứng dụng
CMD ["java", "-jar", "target/demoComputer-0.0.1-SNAPSHOT.jar"]