package com.lvt.tmdt.service.intf;

import com.lvt.tmdt.dto.request.ShopApprovalRequest;
import com.lvt.tmdt.dto.request.ShopRegistrationRequest;
import com.lvt.tmdt.dto.response.ShopResponse;
import com.lvt.tmdt.enums.ShopStatus;

import java.util.List;

public interface ShopService {
    ShopResponse registerShop(String email, ShopRegistrationRequest request);
    List<ShopResponse> getAllShops(ShopStatus status);
    ShopResponse getShopById(Integer id);
    ShopResponse getMyShop(String email);
    ShopResponse approveOrRejectShop(Integer id, ShopApprovalRequest request);
}
