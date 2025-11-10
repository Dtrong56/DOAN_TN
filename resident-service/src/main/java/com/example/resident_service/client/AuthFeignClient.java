package com.example.resident_service.client;

import com.example.resident_service.dto.AuthBulkCreateResponse;
import com.example.resident_service.dto.AuthCreateResult;
import com.example.resident_service.dto.AuthTransferUserRequest;
import com.example.resident_service.dto.AuthUserCreateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "auth-service")
public interface AuthFeignClient {

    @PostMapping("/api/v1/auth/bulk-create-users")
    AuthBulkCreateResponse bulkCreateUsers(List<AuthUserCreateRequest> users);

    @PostMapping("/api/v1/auth/transfer-user")
    AuthCreateResult transferUser(@RequestBody AuthTransferUserRequest request);

}
