package com.lvt.tmdt.controller;

import com.lvt.tmdt.dto.request.AddToCartRequest;
import com.lvt.tmdt.dto.response.CartResponse;
import com.lvt.tmdt.sercurity.CustomUserDetails;
import com.lvt.tmdt.service.intf.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import static java.util.Collections.singletonMap;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddToCartRequest request) {
        cartService.addToCart(userDetails.getUserId(), request);
        return ResponseEntity.ok().body(singletonMap("message", "Thêm vào giỏ hàng thành công"));
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(cartService.getCart(userDetails.getUserId()));
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<?> updateCartItemQuantity(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        cartService.updateCartItemQuantity(userDetails.getUserId(), cartItemId, quantity);
        return ResponseEntity.ok().body(singletonMap("message", "Cập nhật số lượng thành công"));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<?> removeCartItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartItemId) {
        cartService.removeCartItem(userDetails.getUserId(), cartItemId);
        return ResponseEntity.ok().body(singletonMap("message", "Xóa khỏi giỏ hàng thành công"));
    }
}
