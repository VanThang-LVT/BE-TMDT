package com.lvt.tmdt.mapper;

import com.lvt.tmdt.dto.request.RegisterRequest;
import com.lvt.tmdt.dto.response.AuthResponse;
import com.lvt.tmdt.entity.User;
import com.lvt.tmdt.sercurity.CustomUserDetails;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {
    public User toEntity(RegisterRequest request) {
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        return user;
    }
    
    public AuthResponse toAuthResponse(String jwt, CustomUserDetails userDetails, List<String> roles) {
        if (userDetails == null) return null;
        
        return AuthResponse.builder()
                .accessToken(jwt)
                .email(userDetails.getEmail())
                .fullName(userDetails.getFullName())
                .phone(userDetails.getPhone())
                .roles(roles)
                .build();
    }
}