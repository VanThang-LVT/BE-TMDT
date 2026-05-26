package com.lvt.tmdt.service.intf;
import com.lvt.tmdt.dto.request.LoginRequest;
import com.lvt.tmdt.dto.request.RegisterRequest;
import com.lvt.tmdt.dto.response.AuthResponse;

public interface AuthService {
    void register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}