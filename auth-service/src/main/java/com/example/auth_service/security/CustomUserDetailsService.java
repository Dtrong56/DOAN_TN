package com.example.auth_service.security;

import com.example.auth_service.entity.User;
import com.example.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
        @Autowired
        private UserRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String username) {
                User user = userRepository.findByUsernameWithRoles(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                return new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        "N/A",
                        user.getUserRoles().stream()
                                .map(ur -> new SimpleGrantedAuthority(ur.getRole().getName()))
                                .toList()
                );
        }

}
