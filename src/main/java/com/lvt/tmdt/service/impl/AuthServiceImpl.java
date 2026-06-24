package com.lvt.tmdt.service.impl;

import com.lvt.tmdt.dto.request.LoginRequest;
import com.lvt.tmdt.dto.request.RegisterRequest;
import com.lvt.tmdt.dto.response.AuthResponse;
import com.lvt.tmdt.entity.Role;
import com.lvt.tmdt.entity.User;
import com.lvt.tmdt.enums.UserStatus;
import com.lvt.tmdt.repository.RoleRepository;
import com.lvt.tmdt.repository.UserRepository;
import com.lvt.tmdt.sercurity.CustomUserDetails;
import com.lvt.tmdt.sercurity.JwtTokenProvider;
import com.lvt.tmdt.service.intf.AuthService;
import com.lvt.tmdt.service.intf.EmailService;
import com.lvt.tmdt.service.intf.OtpService;
import com.lvt.tmdt.dto.request.ResetPasswordRequest;
import com.lvt.tmdt.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (LockedException e) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ với bộ phận hỗ trợ.");
        } catch (DisabledException e) {
            throw new RuntimeException("Tài khoản của bạn chưa được kích hoạt. Vui lòng liên hệ với bộ phận hỗ trợ.");
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Email hoặc mật khẩu không chính xác.");
        } catch (AuthenticationException e) {
            throw new RuntimeException("Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return userMapper.toAuthResponse(jwt, userDetails, roles);
    }

    @Override
    public void sendRegisterOtp(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email này đã được đăng ký trên hệ thống!");
        }
        String otp = otpService.generateOtp(email);
        String subject = "Xác nhận đăng ký tài khoản EoViTi";
        String content = "Chào bạn,\n\nMã OTP để xác nhận đăng ký tài khoản của bạn là: " + otp
                + "\n\nMã này có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.\n\nTrân trọng,\nĐội ngũ EoViTi";
        emailService.sendEmail(email, subject, content);
    }

    @Override
    public void sendForgotPasswordOtp(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new RuntimeException("Không tìm thấy tài khoản nào sử dụng Email này!");
        }
        String otp = otpService.generateOtp(email);
        String subject = "Yêu cầu khôi phục mật khẩu EoViTi";
        String content = "Chào bạn,\n\nMã OTP để khôi phục mật khẩu tài khoản của bạn là: " + otp
                + "\n\nMã này có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.\n\nTrân trọng,\nĐội ngũ EoViTi";
        emailService.sendEmail(email, subject, content);
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (!otpService.validateOtp(request.getEmail(), request.getOtp())) {
            throw new RuntimeException("Mã OTP không hợp lệ hoặc đã hết hạn!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email này đã được đăng ký trên hệ thống!");
        }

        Set<Role> roles = new HashSet<>();
        Role customerRole = roleRepository.findByRoleName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException(
                        "Lỗi: Không tìm thấy vai trò mặc định CUSTOMER trong cơ sở dữ liệu."));
        roles.add(customerRole);

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(roles);

        userRepository.save(user);
        otpService.clearOtp(request.getEmail());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!otpService.validateOtp(request.getEmail(), request.getOtp())) {
            throw new RuntimeException("Mã OTP không hợp lệ hoặc đã hết hạn!");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng."));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        otpService.clearOtp(request.getEmail());
    }
}