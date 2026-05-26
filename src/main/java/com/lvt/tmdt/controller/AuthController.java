package com.lvt.tmdt.controller;

import com.lvt.tmdt.dto.ApiResponse;
import com.lvt.tmdt.dto.request.LoginRequest;
import com.lvt.tmdt.dto.request.RegisterRequest;
import com.lvt.tmdt.dto.response.AuthResponse;
import com.lvt.tmdt.service.intf.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công!", authResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký tài khoản thành công!", "Thông tin đăng ký đã được ghi nhận."));
    }
}