package com.lvt.tmdt.mapper;

import com.lvt.tmdt.dto.request.ProductRequest;
import com.lvt.tmdt.dto.response.ProductResponse;
import com.lvt.tmdt.entity.Category;
import com.lvt.tmdt.entity.Product;
import com.lvt.tmdt.entity.ProductAttributeValue;
import com.lvt.tmdt.entity.Shop;
import com.lvt.tmdt.enums.ProductStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.lvt.tmdt.entity.CategoryAttribute;
import com.lvt.tmdt.entity.ProductImage;
import com.lvt.tmdt.entity.ProductVariant;
import com.lvt.tmdt.entity.VariantAttribute;
import com.lvt.tmdt.entity.ProductApprovalHistory;
import com.lvt.tmdt.dto.response.ProductVariantResponse;
import com.lvt.tmdt.dto.response.ApprovalHistoryResponse;

import org.springframework.beans.factory.annotation.Autowired;
import com.lvt.tmdt.repository.OrderRepository;

@Component
public class ProductMapper {

    @Autowired
    private OrderRepository orderRepository;

    public ProductResponse mapToResponse(Product product) {
        if (product == null)
            return null;
        
        ProductResponse res = new ProductResponse();
        res.setProductId(product.getProductId());
        
        if (product.getShop() != null) {
            res.setShopId(product.getShop().getShopId());
            res.setShopName(product.getShop().getShopName());
        }
        
        if (product.getCategory() != null) {
            res.setCategoryId(product.getCategory().getCategoryId());
            res.setCategoryName(product.getCategory().getCategoryName());
        }
        
        res.setProductName(product.getProductName());
        res.setDescription(product.getDescription());
        res.setPrice(product.getPrice());
        res.setStockQuantity(product.getStockQuantity());
        
        Integer sales = orderRepository.getTotalSalesByProductId(product.getProductId());
        res.setSalesCount(sales != null ? sales : 0);
        
        res.setBrand(product.getBrand());
        res.setKeywords(product.getKeywords());
        res.setSpecifications(product.getSpecifications());
        
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            java.util.Set<Integer> variantImageIds = new java.util.HashSet<>();
            if (product.getVariants() != null) {
                for (ProductVariant v : product.getVariants()) {
                    if (v.getImageUrl() != null && v.getImageUrl().startsWith("/api/public/images/")) {
                        try {
                            variantImageIds.add(Integer.parseInt(v.getImageUrl().substring("/api/public/images/".length())));
                        } catch (Exception ignored) {}
                    }
                }
            }

            java.util.List<Integer> imgIds = new ArrayList<>();
            for (ProductImage img : product.getImages()) {
                if (!variantImageIds.contains(img.getImageId())) {
                    imgIds.add(img.getImageId());
                    if (Boolean.TRUE.equals(img.getIsMain())) {
                        res.setMainImageId(img.getImageId());
                    }
                }
            }
            res.setImageIds(imgIds);
        }
        
        if (product.getAttributeValues() != null && !product.getAttributeValues().isEmpty()) {
            Map<String, String> attrMap = new HashMap<>();
            for (ProductAttributeValue val : product.getAttributeValues()) {
                if (val.getCategoryAttribute() != null) {
                    attrMap.put(val.getCategoryAttribute().getAttrName(), val.getValueString());
                }
            }
            res.setAttributes(attrMap);
        }
        
        if (product.getApprovalHistories() != null && !product.getApprovalHistories().isEmpty()) {
            java.util.List<ApprovalHistoryResponse> historyResponses = new ArrayList<>();
            for (ProductApprovalHistory history : product.getApprovalHistories()) {
                ApprovalHistoryResponse histRes = new ApprovalHistoryResponse();
                histRes.setApprovalId(history.getApprovalId());
                if (history.getAdmin() != null) {
                    histRes.setAdminName(history.getAdmin().getFullName());
                }
                histRes.setStatus(history.getStatus());
                histRes.setNote(history.getNote());
                histRes.setCreatedAt(history.getCreatedAt());
                historyResponses.add(histRes);
            }
            // Sort by createdAt descending
            historyResponses.sort((h1, h2) -> h2.getCreatedAt().compareTo(h1.getCreatedAt()));
            res.setApprovalHistories(historyResponses);
        }
        
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            java.util.List<ProductVariantResponse> variantResponses = new ArrayList<>();
            for (ProductVariant variant : product.getVariants()) {
                variantResponses.add(mapToVariantResponse(variant));
            }
            res.setVariants(variantResponses);
        }
        
        res.setStatus(product.getStatus());
        res.setCreatedAt(product.getCreatedAt());
        return res;
    }
    public Product mapToEntity(ProductRequest request, Shop shop, Category category) {
        if (request == null)
            return null;
        
        return Product.builder()
                .shop(shop)
                .category(category)
                .productName(request.getProductName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .brand(request.getBrand())
                .keywords(request.getKeywords())
                .specifications(request.getSpecifications())
                .status(ProductStatus.PENDING) // Always PENDING when newly created
                .images(new ArrayList<>())
                .variants(new ArrayList<>())
                .attributeValues(new ArrayList<>())
                .build();
    }
    
    public void updateEntityFromRequest(ProductRequest request, Product product, Category category) {
        if (request == null || product == null)
            return;
        
        product.setCategory(category);
        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setBrand(request.getBrand());
        product.setKeywords(request.getKeywords());
        product.setSpecifications(request.getSpecifications());
    }
    
    public ProductImage mapToProductImage(MultipartFile file, Product product, boolean isMain) {
        if (file == null || file.isEmpty())
            return null;
        
        try {
            ProductImage productImage = new ProductImage();
            productImage.setProduct(product);
            productImage.setImageData(file.getBytes());
            productImage.setContentType(file.getContentType());
            productImage.setIsMain(isMain);
            return productImage;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi xử lý file ảnh", e);
        }
    }
    
    public ProductAttributeValue mapToProductAttributeValue(CategoryAttribute categoryAttribute, String value, Product product) {
        if (categoryAttribute == null || value == null) return null;
        
        ProductAttributeValue val = new ProductAttributeValue();
        val.setProduct(product);
        val.setCategoryAttribute(categoryAttribute);
        val.setValueString(value);
        return val;
    }

    public ProductVariantResponse mapToVariantResponse(ProductVariant variant) {
        if (variant == null) return null;
        ProductVariantResponse res = new ProductVariantResponse();
        res.setVariantId(variant.getVariantId());
        res.setSku(variant.getSku());
        res.setPrice(variant.getPrice());
        res.setStockQuantity(variant.getStockQuantity());
        res.setImageUrl(variant.getImageUrl());
        
        if (variant.getVariantAttributes() != null && !variant.getVariantAttributes().isEmpty()) {
            Map<String, String> attrMap = new HashMap<>();
            for (VariantAttribute vAttr : variant.getVariantAttributes()) {
                if (vAttr.getCategoryAttribute() != null) {
                    attrMap.put(vAttr.getCategoryAttribute().getAttrName(), vAttr.getValueString());
                }
            }
            res.setAttributes(attrMap);
        }
        return res;
    }
}
