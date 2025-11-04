package com.example.auth_service.repository;

import com.example.auth_service.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, String> {
    boolean existsByUserIdAndRoleId(String userId, String roleId);
    boolean existsByRole_Name(String name);
}
