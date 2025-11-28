package com.example.multi_tenant_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemLogDTO {

    // Thời điểm ghi nhận log
    private LocalDateTime timestamp;

    // Thông tin người thực hiện
    private String userId;
    private String tenantId;
    private String role; // ADMIN, BQL, RESIDENT

    // Hành động nghiệp vụ
    private String action; // CREATE_CONTRACT, APPROVE_APPENDIX, GENERATE_INVOICE...

    // Đối tượng nghiệp vụ liên quan
    private String objectType; // Contract, ServiceAppendix, Invoice...
    private String objectId;

    // Mô tả chi tiết hành động
    private String message;

    // Metadata bổ sung (tuỳ ý)
    private Map<String, Object> metadata;

    // Thông tin kỹ thuật (tuỳ chọn)
    private String serviceName; // Tên service gọi log (ContractService, InvoiceService...)
    private String endpoint;    // API hoặc controller thực hiện
    private String traceId;     // Để liên kết các log trong cùng một request
}
