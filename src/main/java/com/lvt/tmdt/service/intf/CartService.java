package com.lvt.tmdt.service.intf;

import com.lvt.tmdt.dto.request.AddToCartRequest;
import com.lvt.tmdt.dto.response.CartResponse;

public interface CartService {
    Long addToCart(Integer userId, AddToCartRequest request);
    CartResponse getCart(Integer userId);
    void updateCartItemQuantity(Integer userId, Long cartItemId, Integer quantity);
    void removeCartItem(Integer userId, Long cartItemId);
}
