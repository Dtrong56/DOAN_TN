package com.example.multi_tenant_service.client;

import com.example.multi_tenant_service.dto.CreateUserRequest;
import com.example.multi_tenant_service.dto.CreateUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "auth-service", path = "/auth")
public interface AuthClient {

    @PostMapping("/create-user")
    CreateUserResponse createUser(@RequestBody CreateUserRequest request);

    @GetMapping("/check-username")
    boolean checkUsernameExists(@RequestParam("username") String username);

    // ✅ Thêm hàm cập nhật trạng thái user
    @PutMapping("/update-active/{userId}")
    void updateUserActiveStatus(@PathVariable("userId") String userId,
                                @RequestParam("active") boolean active);
}

