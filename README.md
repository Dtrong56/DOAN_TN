# DOAN_TN — Hệ thống quản lý cư dân (Đồ án tốt nghiệp)

Ứng dụng: hệ thống quản lý cư dân đa thuê (multi-tenant) theo kiến trúc microservices, triển khai bằng Java + Spring Boot.

## Tổng quan
Dự án này là đồ án tốt nghiệp, mục tiêu xây dựng một hệ thống quản lý cư dân cho nhiều thuê bao (nhà, khu chung cư...) theo kiến trúc microservices. Các dịch vụ tách biệt đảm bảo khả năng mở rộng và bảo trì dễ dàng.

## Tính năng (Services)
Dự án bao gồm các service chính:
- `auth-service`: Xác thực và phân quyền
- `tenant-service`: Quản lý thông tin tenant (thuê bao)
- `contract-service`: Quản lý hợp đồng cư trú
- `resident-service`: Quản lý thông tin cư dân
- `payment-service`: Quản lý thanh toán
- `notification-service`: Gửi thông báo (email/SMS/queue)
- `monitoring-service`: Giám sát (metrics / health / logs)
- `gateway`: API Gateway (routing)
- `service-catalog-service`: Danh mục dịch vụ / helper
- `eureka`: Service registry (Eureka server)
- `libs/tenant-core`: Thư viện dùng chung giữa các service

## Những gì đã làm
- Đã hoàn thành: `auth-service`, `tenant-service`, `resident-service`.
- Đang phát triển / chưa hoàn thiện: `payment-service`, `notification-service`, `contract-service`.

(Chỉnh lại phần này nếu bạn muốn mô tả chính xác từng service — mình để tạm như trên.)

## Yêu cầu môi trường
- Java 17
- Maven 3.6+
- PostgreSQL (hoặc DB khác theo cấu hình từng service)
- Eureka server cần chạy để các service client đăng ký
- Docker & Docker Compose (repo đã cấu hình để deploy bằng Docker)

## Biến môi trường & cấu hình mẫu
Mỗi service sẽ cần các biến cơ bản (ví dụ):
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/<db>
SPRING_DATASOURCE_USERNAME=<user>
SPRING_DATASOURCE_PASSWORD=<pass>
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
JWT_SECRET=your_jwt_secret
SERVER_PORT=8081
```

## Cài đặt & chạy (local, từng service)
1. Clone repo
```bash
git clone https://github.com/Dtrong56/DOAN_TN.git
cd DOAN_TN
```

2. Chạy Eureka
```bash
cd eureka
mvn clean install
mvn spring-boot:run
# hoặc: java -jar target/eureka-*.jar
```

3. Chạy một service (ví dụ auth-service)
```bash
cd api/auth-service
# chỉnh application.yml/properties cho đúng DB và EUREKA URL
mvn clean install
mvn spring-boot:run
```

4. Chạy các service khác tương tự. Đảm bảo Eureka đang chạy trước.

## Chạy bằng Docker
Repo đã được cấu hình để deploy bằng Docker. Nếu bạn có file `docker-compose.yml` ở thư mục gốc, bạn có thể chạy:
```bash
docker-compose up --build
```
Nếu cần, mình có thể tạo hoặc cập nhật file `docker-compose.yml` mẫu cho các service.

## 2.4 Các API chính
Dưới đây là danh mục tóm tắt các endpoint chính theo từng service.

### 2.4.1 Auth-service
| HTTP Method | Endpoint | Input | Output | Mục đích |
|---|---|---|---|---|
| POST | /auth/login | Body: LoginRequest | JwtResponse | Xác thực người dùng, trả về JWT và thông tin đăng nhập. |
| POST | /auth/init-admin | Body: CreateAdminRequest | ResponseEntity<?> | Tạo tài khoản admin ban đầu. |
| POST | /auth/validate-token | Body: ValidateTokenRequest | ResponseEntity<Boolean> | Kiểm tra tính hợp lệ của token. |
| PUT | /auth/v1/users/reset | Body: ResetBqlRequest | UserResponse | Reset tài khoản BQL cho user. |
| PUT | /auth/change-password | Body: ChangePasswordRequest; Header: Authorization | ChangePasswordResponse | Đổi mật khẩu (yêu cầu token trong header). |
| POST | /auth/digital-signature/upload | multipart: publicKeyFile, certificateFile (optional); query: validFrom, validTo | DigitalSignatureUploadResponse | Upload/đăng ký chữ ký số cho user. |
| POST | /auth/create-user | Body: CreateUserRequest | CreateUserResponse | Tạo user nội bộ mới. |
| GET | /auth/check-username | Query: username | boolean | Kiểm tra username đã tồn tại hay chưa. |
| PUT | /auth/update-active/{userId} | Path: userId; Query: active | ResponseEntity<String> | Cập nhật trạng thái active của user. |
| GET | /auth/digital-signature/{userId} | Path: userId | DigitalSignatureInternalDTO | Lấy thông tin/chữ ký số nội bộ của user. |
| GET | /auth/email/{userId} | Path: userId | String (email) | Lấy email của user theo userId. |
| POST | /api/auth/forgot-password | Body: ForgotPasswordRequest (chứa email) | ResponseEntity<String> | Yêu cầu gửi link/điều kiện reset mật khẩu. |
| POST | /api/auth/reset-password | Body: ResetPasswordRequest | ResponseEntity<String> | Thực hiện reset mật khẩu bằng token. |
| POST | /api/v1/auth/bulk-create-users | Body: List<AuthUserCreateRequest> | AuthBulkCreateResponse | Tạo nhiều user hàng loạt (bulk create). |
| POST | /api/v1/auth/transfer-user | Body: AuthTransferUserRequest | AuthCreateResult | Chuyển/transfer user. |

### 2.4.2 Contract-service
| HTTP Method | Endpoint | Input | Output | Mục đích |
|---|---|---|---|---|
| POST | /contract/common | form-data: signedDate, effectiveDate, expirationDate, monthlyFeePerM2, file (MultipartFile) | MainContractResponse | Upload (đăng) hợp đồng chung kèm file PDF và metadata. |
| GET | /contract/common/{id}/file | Path: id | Resource (PDF) | Tải/xem file hợp đồng chung. |
| GET | /contract/common | Không có | List<MainContractResponse> | Lấy danh sách tất cả hợp đồng chính (main contracts). |
| GET | /contract/common/{id} | Path: id | MainContractResponse | Lấy chi tiết 1 hợp đồng chính theo ID. |
| GET | /contract/annex/by-resident | Không có | List<ServiceAppendixResponse> | Lấy danh sách các phụ lục liên quan đến resident (người thuê). |
| GET | /contract/annex/{id} | Path: id | ServiceAppendixResponse | Lấy chi tiết phụ lục theo ID. |
| POST | /contract/appendix/register | RequestBody: RegisterAndSignAppendixRequest | RegisterAppendixResponse | Đăng ký và ký phụ lục hợp đồng. |
| POST | /contract/appendix/approve | RequestBody: ApproveAppendixRequest | ApproveAppendixResponse | Phê duyệt phụ lục hợp đồng. |
| GET | /contract | Không có (sử dụng context) | List<ContractDto> | Lấy danh sách hợp đồng liên quan đến user hiện tại. |
| GET | /contract/{contractId}/pdf | Path: contractId | Resource (PDF) | Xem file PDF của hợp đồng. |
| GET | /contract/appendices/{appendixId}/pdf | Path: appendixId | Resource (PDF) | Xem file PDF của phụ lục. |
| GET | /api/internal/operation-contract/active | query: tenantId | OperationContractDTO | Lấy thông tin hợp đồng đang hoạt động cho tenantId (nội bộ). |
| GET | /api/internal/service-appendices/active | query: tenantId, residentId, periodMonth, periodYear | List<ServiceAppendixDTO> | Lấy danh sách phụ lục dịch vụ đang active cho tenant/resident (nội bộ). |

### 2.4.3 Service-catalog-service
| HTTP Method | Endpoint | Input | Output | Mục đích |
|---|---|---|---|---|
| GET | /services | Không có | List<ServiceCatalog> | Lấy danh sách tất cả các dịch vụ hiện có. |
| GET | /services/{serviceId} | Path: serviceId | ServiceCatalogDetailResponse | Lấy thông tin chi tiết của một dịch vụ cụ thể. |
| POST | /services | Body: CreateServiceRequest | ServiceCatalog | Tạo một dịch vụ mới. |
| PUT | /services/{serviceId} | Path: serviceId; Body: UpdateServiceRequest | ServiceCatalog | Cập nhật thông tin của một dịch vụ. |
| PUT | /services/{serviceId}/deactivate | Path: serviceId | void (HTTP 200) | Hủy kích hoạt dịch vụ. |
| GET | /internal/service/{serviceId} | Path: serviceId | ServiceInfoDTO | Lấy thông tin dịch vụ cơ bản (nội bộ) để xác thực. |
| GET | /internal/service/{serviceId}/package/{packageId} | Path: serviceId, packageId | PackageInfoDTO | Lấy thông tin gói cước chi tiết (nội bộ). |

### 2.4.4 Multi-tenant-service
| HTTP Method | Endpoint | Input | Output | Mục đích |
|---|---|---|---|---|
| GET | /api/internal/tenants/active | Không có | Danh sách TenantDTO | Lấy danh sách các tenant đang hoạt động (nội bộ). |
| GET | /api/internal/management-accounts/by-user/{userId} | Path: userId | ManagementAccountResponse | Trả về tenantId và thông tin cơ bản của BQL theo userId (nội bộ). |
| POST | /tenant | Body: TenantCreateRequest | TenantResponse | Tạo mới tenant. |
| PUT | /tenant/{tenantId}/status | Path: tenantId; Query: active | TenantResponse | Cập nhật trạng thái kích hoạt của tenant. |
| PUT | /tenant/{id} | Path: id; Body: Tenant | Tenant | Cập nhật thông tin tenant. |
| GET | /tenant | Không có | Danh sách Tenant | Lấy danh sách tất cả tenant. |

### 2.4.5 Resident-service
| HTTP Method | Endpoint | Input | Output | Mục đích |
|---|---|---|---|---|
| GET | /api/internal/resident-accounts/by-user/{userId} | Path: userId | ResidentAccountResponse | Lấy tenantId của cư dân theo userId (nội bộ cho AuthService). |
| GET | /api/internal/resident-accounts/by-user-contract/{userId} | Path: userId | ResidentAccountInternalResponse | Lấy thông tin cư dân theo userId (nội bộ cho contract-service). |
| GET | /api/internal/residents | Query: tenantId, activeOnly (default: true), includeApartment (default: true) | List<ResidentDTO> | Lấy danh sách cư dân theo tenantId (nội bộ). |
| POST | /resident/import/preview | file (MultipartFile) | ImportPreviewResponse | Xem trước file nhập liệu cư dân hàng loạt. |
| POST | /resident/import/confirm | Body: ImportConfirmRequest | ImportResultResponse | Xác nhận và thực hiện nhập liệu cư dân hàng loạt. |
| POST | /resident/import/transfer | Body: OwnershipTransferRequest | OwnershipTransferResult | Xử lý chuyển quyền sở hữu (căn hộ/cư dân). |

### 2.4.6 Payment-service
| HTTP Method | Endpoint | Input | Output | Mục đích |
|---|---|---|---|---|
| POST | /api/internal/invoices/generate | Query: tenantId, month, year | String (message) | Tạo hóa đơn hàng tháng cho một tenant cụ thể (nội bộ). |
| GET | /api/internal/reports/revenue-debt | Query: fromMonth, fromYear, toMonth, toYear | RevenueDebtReportDTO | Lấy báo cáo doanh thu và nợ (nội bộ). |
| GET | /api/internal/reports/revenue-debt/export | Query: fromMonth, fromYear, toMonth, toYear, format | ResponseEntity<byte[]> | Xuất báo cáo doanh thu và nợ dưới dạng file (nội bộ). |
| POST | /payment/direct | Body: DirectPaymentRequest | DirectPaymentResponse | Ghi nhận thanh toán trực tiếp. |
| POST | /payment/online/init | Body: InitOnlinePaymentRequest | InitOnlinePaymentResponse | Khởi tạo thanh toán online. |
| GET | /payment/online/return | Query: transactionId, status, gatewayCode | PaymentCallbackResult | Xử lý callback từ cổng thanh toán. |
| GET | /api/resident/invoices | Query: month, year, status | List<InvoiceSummaryDTO> | Lấy danh sách hóa đơn của cư dân. |
| GET | /api/resident/invoices/{invoiceId} | Path: invoiceId | InvoiceDetailDTO | Lấy chi tiết hóa đơn của cư dân. |
| GET | /sandbox/pay | Query: transactionId, amount | RedirectView | Mô phỏng thanh toán sandbox. |

### 2.4.7 Notification-service
| HTTP Method | Endpoint | Input | Output | Mục đích |
|---|---|---|---|---|
| POST | /notify/send | Body: AdminSendNotificationRequestDTO | AdminSendNotificationResponseDTO | Gửi thông báo ngay lập tức tới cư dân (dành cho Admin/BQL). |
| POST | /notify/internal/notify | Body: NotificationRequestDTO | ResponseEntity<InternalNotificationResponseDTO> | Tạo mới một thông báo nội bộ. |
| POST | /notify/internal/send | Query: tenantId, residentId, type, message | NotificationResponseDTO | Gửi thông báo nội bộ tới cư dân cụ thể. |

### 2.4.8 Monitoring-service
| HTTP Method | Endpoint | Input | Output | Mục đích |
|---|---|---|---|---|
| POST | /monitoring/internal/log | Body: SystemLogDTO | Không có (void) | Ghi lại một log hệ thống (nội bộ). |
| GET | /monitoring/log/recent | Query: limit (default: 50) | List<SystemLogResponseDTO> | Lấy danh sách các log gần đây nhất. |
| GET | /monitoring/log/tenant/{tenantId} | Path: tenantId | List<SystemLogResponseDTO> | Lấy danh sách các log theo ID của tenant. |

## Testing
- Unit tests: `mvn test` từng service
- Integration tests: (nếu có) mô tả thêm (Testcontainers, MockMvc...)

## Contribution
- Fork -> branch feature/<tên> -> commit -> PR
- Viết rõ guideline nếu cần (mình có thể tạo `CONTRIBUTING.md`).

## License
Hiện tại dự án phục vụ mục đích học tập. Nếu bạn muốn, mình có thể thêm `LICENSE` (MIT / Apache-2.0 / GPL-3.0).

## Tác giả
- Dtrong56 — Sinh viên thực hiện đồ án

---

Hành động tiếp theo: Mình đã cập nhật README.md. Nếu bạn muốn thay đổi chi tiết trạng thái các service, port, biến môi trường hoặc muốn mình thêm `docker-compose.yml`/`CONTRIBUTING.md`/`LICENSE`, hãy cho mình biết.