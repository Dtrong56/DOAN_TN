package com.example.auth_service.security;

import com.example.auth_service.entity.Credential;
import com.example.auth_service.entity.User;
import com.example.auth_service.repository.CredentialRepository;
import com.example.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CredentialRepository credentialRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Lấy mật khẩu (hashed_password) từ Credential
        Credential credential = credentialRepo.findByUserId(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Credential not found for user: " + username));

        // Lấy danh sách roles từ UserRole -> Role
        List<GrantedAuthority> authorities = user.getUserRoles().stream()
                .map(userRole -> new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getName()))
                .collect(Collectors.toList());

        // Tạo đối tượng UserDetails của Spring Security
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(credential.getHashedPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!user.getActive())
                .credentialsExpired(false)
                .disabled(!user.getActive())
                .build();
    }
}
