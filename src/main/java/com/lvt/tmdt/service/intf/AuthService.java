package com.lvt.tmdt.service.intf;
import com.lvt.tmdt.dto.request.LoginRequest;
import com.lvt.tmdt.dto.request.RegisterRequest;
import com.lvt.tmdt.dto.response.AuthResponse;

public interface AuthService {
    void register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    void sendRegisterOtp(String email);
    void sendForgotPasswordOtp(String email);
    void resetPassword(com.lvt.tmdt.dto.request.ResetPasswordRequest request);
}