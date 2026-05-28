package com.lvt.tmdt.service.impl;

import com.lvt.tmdt.dto.request.ShopApprovalRequest;
import com.lvt.tmdt.dto.request.ShopRegistrationRequest;
import com.lvt.tmdt.dto.response.ShopResponse;
import com.lvt.tmdt.entity.Role;
import com.lvt.tmdt.entity.Shop;
import com.lvt.tmdt.entity.User;
import com.lvt.tmdt.enums.ShopStatus;
import com.lvt.tmdt.repository.RoleRepository;
import com.lvt.tmdt.repository.ShopRepository;
import com.lvt.tmdt.repository.UserRepository;
import com.lvt.tmdt.service.intf.ShopService;
import com.lvt.tmdt.service.intf.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShopServiceImpl implements ShopService {

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public ShopResponse registerShop(String email, ShopRegistrationRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        if (shopRepository.existsByUser(user)) {
            throw new RuntimeException("Bạn đã gửi yêu cầu đăng ký gian hàng trước đó.");
        }

        boolean isSeller = user.getRoles().stream().anyMatch(role -> role.getRoleName().equals("SELLER"));
        if (isSeller) {
            throw new RuntimeException("Bạn đã là Người Bán.");
        }

        Shop shop = Shop.builder()
                .user(user)
                .shopName(request.getShopName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .description(request.getDescription())
                .status(ShopStatus.PENDING)
                .build();

        Shop saved = shopRepository.save(shop);
        
        // Gửi thông báo cho Admin
        notificationService.sendToAllAdmins(
                "Yêu cầu mở gian hàng mới",
                "Người dùng " + user.getFullName() + " vừa gửi yêu cầu đăng ký gian hàng: " + request.getShopName()
        );
        
        return mapToResponse(saved);
    }

    @Override
    public List<ShopResponse> getAllShops(ShopStatus status) {
        List<Shop> list;
        if (status != null) {
            list = shopRepository.findByStatus(status);
        } else {
            list = shopRepository.findAll();
        }
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public ShopResponse getShopById(Integer id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cửa hàng"));
        return mapToResponse(shop);
    }

    @Override
    public ShopResponse getMyShop(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        Shop shop = shopRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Bạn chưa đăng ký gian hàng nào."));
                
        return mapToResponse(shop);
    }

    @Override
    @Transactional
    public ShopResponse approveOrRejectShop(Integer id, ShopApprovalRequest request) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gian hàng"));

        if (shop.getStatus() != ShopStatus.PENDING) {
            throw new RuntimeException("Yêu cầu này đã được xử lý.");
        }

        shop.setStatus(request.getStatus());
        User shopOwner = shop.getUser();

        if (request.getStatus() == ShopStatus.REJECTED) {
            shop.setRejectReason(request.getReason());
            notificationService.createNotification(
                    shopOwner,
                    "Yêu cầu mở gian hàng bị từ chối",
                    "Yêu cầu đăng ký gian hàng '" + shop.getShopName() + "' của bạn đã bị từ chối. Lý do: " + request.getReason()
            );
        } else if (request.getStatus() == ShopStatus.ACTIVE) {
            shop.setRejectReason(null);
            Role sellerRole = roleRepository.findByRoleName("SELLER")
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy quyền SELLER trong hệ thống"));

            shopOwner.getRoles().clear();
            shopOwner.getRoles().add(sellerRole);
            
            userRepository.save(shopOwner);
            
            notificationService.createNotification(
                    shopOwner,
                    "Gian hàng đã được duyệt",
                    "Chúc mừng! Gian hàng '" + shop.getShopName() + "' của bạn đã được duyệt. Hãy đăng nhập lại để bắt đầu bán hàng."
            );
        }

        Shop saved = shopRepository.save(shop);
        return mapToResponse(saved);
    }

    private ShopResponse mapToResponse(Shop entity) {
        return ShopResponse.builder()
                .shopId(entity.getShopId())
                .shopName(entity.getShopName())
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .description(entity.getDescription())
                .logoUrl(entity.getLogoUrl())
                .status(entity.getStatus())
                .rejectReason(entity.getRejectReason())
                .createdAt(entity.getCreatedAt())
                .userId(entity.getUser().getUserId())
                .fullName(entity.getUser().getFullName())
                .email(entity.getUser().getEmail())
                .build();
    }
}
