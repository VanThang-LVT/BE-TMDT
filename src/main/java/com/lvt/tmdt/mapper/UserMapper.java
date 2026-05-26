package com.lvt.tmdt.mapper;

import com.lvt.tmdt.dto.request.RegisterRequest;
import com.lvt.tmdt.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toEntity(RegisterRequest request) {
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        return user;
    }
}