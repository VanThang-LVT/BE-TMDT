package com.lvt.tmdt.controller;

import com.lvt.tmdt.dto.ApiResponse;
import com.lvt.tmdt.dto.request.LoginRequest;
import com.lvt.tmdt.dto.request.RegisterRequest;
import com.lvt.tmdt.dto.request.SendOtpRequest;
import com.lvt.tmdt.dto.request.ResetPasswordRequest;
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

    @PostMapping("/send-register-otp")
    public ResponseEntity<ApiResponse<String>> sendRegisterOtp(@Valid @RequestBody SendOtpRequest request) {
        authService.sendRegisterOtp(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Đã gửi mã OTP đăng ký", "Vui lòng kiểm tra email của bạn."));
    }

    @PostMapping("/send-forgot-password-otp")
    public ResponseEntity<ApiResponse<String>> sendForgotPasswordOtp(@Valid @RequestBody SendOtpRequest request) {
        authService.sendForgotPasswordOtp(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Đã gửi mã OTP khôi phục mật khẩu", "Vui lòng kiểm tra email của bạn."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật mật khẩu thành công!", "Bạn có thể đăng nhập bằng mật khẩu mới."));
    }
}