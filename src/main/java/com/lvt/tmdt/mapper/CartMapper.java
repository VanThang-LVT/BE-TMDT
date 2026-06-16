package com.lvt.tmdt.mapper;

import com.lvt.tmdt.dto.response.CartItemResponse;
import com.lvt.tmdt.dto.response.CartResponse;
import com.lvt.tmdt.entity.Cart;
import com.lvt.tmdt.entity.CartItem;
import com.lvt.tmdt.entity.Product;
import com.lvt.tmdt.entity.ProductVariant;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartItemResponse toCartItemResponse(CartItem item) {
        Product product = item.getProduct();
        ProductVariant variant = item.getProductVariant();
        BigDecimal price = variant != null ? variant.getPrice() : product.getPrice();
        String imageUrl = null;
        if (variant != null && variant.getImageUrl() != null && !variant.getImageUrl().isEmpty()) {
            imageUrl = variant.getImageUrl();
        } else if (product.getImages() != null && !product.getImages().isEmpty()) {
            imageUrl = "/api/products/images/" + product.getImages().get(0).getImageId();
        }
        
        int stockQuantity = variant != null ? variant.getStockQuantity() : product.getStockQuantity();
        
        String variantAttributes = "";
        if (variant != null && variant.getVariantAttributes() != null) {
            variantAttributes = variant.getVariantAttributes().stream()
                    .map(va -> va.getValueString())
                    .collect(Collectors.joining(", "));
        }

        return CartItemResponse.builder()
                .cartItemId(item.getCartItemId())
                .productId(product.getProductId())
                .productName(product.getProductName())
                .variantId(variant != null ? variant.getVariantId() : null)
                .variantAttributes(variantAttributes)
                .shopId(product.getShop().getShopId())
                .shopName(product.getShop().getShopName())
                .imageUrl(imageUrl)
                .price(price)
                .quantity(item.getQuantity())
                .subTotal(price.multiply(BigDecimal.valueOf(item.getQuantity())))
                .stockQuantity(stockQuantity)
                .build();
    }

    public CartResponse toCartResponse(Cart cart, List<CartItem> cartItems) {
        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.toList());

        BigDecimal totalPrice = itemResponses.stream()
                .map(CartItemResponse::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart != null ? cart.getCartId() : null)
                .items(itemResponses)
                .totalPrice(totalPrice)
                .build();
    }
}
