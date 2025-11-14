🏠 DOAN_TN – Đồ án tốt nghiệp hệ thống quản lý cư dân

Đây là đồ án tốt nghiệp xây dựng hệ thống quản lý cư dân trong môi trường đa thuê (multi-tenant), sử dụng kiến trúc microservices với ngôn ngữ Java.

🚀 Tính năng chính

Hệ thống bao gồm nhiều dịch vụ nhỏ, mỗi dịch vụ đảm nhận một chức năng riêng biệt:

- `auth-service`: Xác thực và phân quyền người dùng
- `tenant-service`: Quản lý thông tin thuê bao
- `contract-service`: Quản lý hợp đồng cư trú
- `resident-service`: Quản lý thông tin cư dân
- `payment-service`: Quản lý thanh toán
- `notification-service`: Gửi thông báo
- `monitoring-service`: Giám sát hệ thống
- `gateway`: Cổng truy cập API
- `service-catalog-service`: Danh mục dịch vụ

🛠️ Công nghệ sử dụng

- Ngôn ngữ: Java
- Framework: Spring Boot
- Quản lý dịch vụ: Eureka
- Kiến trúc: Microservices
- Giao tiếp giữa các service: REST API
- IDE: Visual Studio Code (có thư mục `.vscode`)

📦 Cài đặt & chạy thử

```bash
# Clone repo
git clone https://github.com/Dtrong56/DOAN_TN.git

# Di chuyển vào thư mục dự án
cd DOAN_TN

# Mỗi service có thể được build và chạy riêng bằng Maven
cd auth-service
mvn clean install
mvn spring-boot:run
```

Lưu ý: Cần cấu hình cơ sở dữ liệu và Eureka server trước khi chạy toàn bộ hệ thống.

📁 Cấu trúc thư mục

```
DOAN_TN/
├── api/
│   ├── auth-service/
│   ├── contract-service/
│   ├── resident-service/
│   ├── ...
├── libs/
│   └── tenant-core/
├── eureka/
├── gateway/
├── monitoring-service/
├── README.md
```

📄 License

Dự án phục vụ mục đích học tập, chưa áp dụng license cụ thể.

👨‍💻 Tác giả

- Dtrong56 – Sinh viên thực hiện đồ án
