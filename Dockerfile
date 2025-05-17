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
RUN PGHOST=dpg-d0k9810gjchc73a7shkg-a.singapore-postgres.render.com \
    PGPORT=5432 \
    PGDATABASE=hhe_9cc5e \
    PGUSER=admin \
    PGPASSWORD=1omneSh0DC6lQPo4ueZ28FbySVAXPytW \
    psql -c "SELECT 1" || exit 1

# Build ứng dụng
RUN ./mvnw clean package -DskipTests

# Chạy ứng dụng
CMD ["java", "-jar", "target/demoComputer-0.0.1-SNAPSHOT.jar"]