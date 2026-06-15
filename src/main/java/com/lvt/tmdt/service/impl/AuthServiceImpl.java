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
    @Transactional
    public void register(RegisterRequest request) {
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
    }
}