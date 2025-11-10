package com.example.auth_service.controller;

import com.example.auth_service.dto.*;
import com.example.auth_service.service.UserBulkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class UserBulkController {

    private final UserBulkService userBulkService;

    //Endpoint for bulk user creation
    @PostMapping("/bulk-create-users")
    public AuthBulkCreateResponse bulkCreateUsers(@RequestBody List<AuthUserCreateRequest> requests) {
        return userBulkService.bulkCreate(requests);
    }

    // Endpoint for transferring a user
    @PostMapping("/transfer-user")
    public AuthCreateResult transferUser(@RequestBody AuthTransferUserRequest request) {
        return userBulkService.transferUser(request);
    }

}
