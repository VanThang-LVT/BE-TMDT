package com.lvt.tmdt.service.impl;

import com.lvt.tmdt.dto.request.AddToCartRequest;
import com.lvt.tmdt.dto.response.CartResponse;
import com.lvt.tmdt.entity.Cart;
import com.lvt.tmdt.entity.CartItem;
import com.lvt.tmdt.entity.Product;
import com.lvt.tmdt.entity.ProductVariant;
import com.lvt.tmdt.entity.User;
import com.lvt.tmdt.mapper.CartMapper;
import com.lvt.tmdt.repository.CartItemRepository;
import com.lvt.tmdt.repository.CartRepository;
import com.lvt.tmdt.repository.ProductRepository;
import com.lvt.tmdt.repository.ProductVariantRepository;
import com.lvt.tmdt.repository.UserRepository;
import com.lvt.tmdt.service.intf.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional
    public Long addToCart(Integer userId, AddToCartRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        ProductVariant variant = null;
        if (request.getVariantId() != null) {
            variant = productVariantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phân loại sản phẩm"));
            
            if (variant.getStockQuantity() < request.getQuantity()) {
                throw new RuntimeException("Số lượng tồn kho của phân loại này không đủ");
            }
        } else {
            if (product.getStockQuantity() < request.getQuantity()) {
                throw new RuntimeException("Số lượng tồn kho của sản phẩm không đủ");
            }
        }

        Cart cart = cartRepository.findByUserUserId(userId).orElseGet(() -> {
            Cart newCart = Cart.builder()
                    .user(user)
                    .build();
            return cartRepository.save(newCart);
        });

        Optional<CartItem> existingItemOpt;
        if (variant != null) {
            existingItemOpt = cartItemRepository.findByCartCartIdAndProductProductIdAndProductVariantVariantId(
                    cart.getCartId(),
                    product.getProductId(),
                    variant.getVariantId());
        } else {
            existingItemOpt = cartItemRepository.findByCartCartIdAndProductProductIdAndProductVariantIsNull(
                    cart.getCartId(),
                    product.getProductId());
        }

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            int maxStock = variant != null ? variant.getStockQuantity() : product.getStockQuantity();
            if (newQuantity > maxStock) {
                throw new RuntimeException("Số lượng tồn kho không đủ");
            }
            
            existingItem.setQuantity(newQuantity);
            CartItem savedItem = cartItemRepository.save(existingItem);
            return savedItem.getCartItemId();
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .productVariant(variant)
                    .quantity(request.getQuantity())
                    .build();
            CartItem savedItem = cartItemRepository.save(newItem);
            return savedItem.getCartItemId();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Integer userId) {
        Cart cart = cartRepository.findByUserUserId(userId).orElse(null);
        if (cart == null) {
            return CartResponse.builder().items(List.of()).totalPrice(BigDecimal.ZERO).build();
        }

        List<CartItem> cartItems = cartItemRepository.findByCartCartId(cart.getCartId());
        return cartMapper.toCartResponse(cart, cartItems);
    }

    @Override
    @Transactional
    public void updateCartItemQuantity(Integer userId, Long cartItemId, Integer quantity) {
        if (quantity <= 0) {
            removeCartItem(userId, cartItemId);
            return;
        }
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));
        if (!item.getCart().getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền thực hiện thao tác này");
        }
        int maxStock = item.getProductVariant() != null ? item.getProductVariant().getStockQuantity() : item.getProduct().getStockQuantity();
        if (quantity > maxStock) {
            throw new RuntimeException("Số lượng tồn kho không đủ");
        }
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Override
    @Transactional
    public void removeCartItem(Integer userId, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        if (!item.getCart().getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền thực hiện thao tác này");
        }
        cartItemRepository.delete(item);
    }
}
