package com.lvt.tmdt.service.impl;

import com.lvt.tmdt.dto.request.ProductRequest;
import com.lvt.tmdt.dto.response.ProductResponse;
import com.lvt.tmdt.entity.Category;
import com.lvt.tmdt.entity.Product;
import com.lvt.tmdt.entity.ProductImage;
import com.lvt.tmdt.entity.ProductAttributeValue;
import com.lvt.tmdt.entity.Shop;
import com.lvt.tmdt.entity.User;
import com.lvt.tmdt.enums.ProductStatus;
import com.lvt.tmdt.repository.CategoryRepository;
import com.lvt.tmdt.repository.ProductRepository;
import com.lvt.tmdt.repository.ShopRepository;
import com.lvt.tmdt.repository.UserRepository;
import com.lvt.tmdt.repository.ProductAttributeValueRepository;
import com.lvt.tmdt.repository.CategoryAttributeRepository;
import com.lvt.tmdt.service.intf.ProductService;
import com.lvt.tmdt.service.intf.NotificationService;
import com.lvt.tmdt.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ShopRepository shopRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ProductAttributeValueRepository productAttributeValueRepository;
    
    @Autowired
    private CategoryAttributeRepository categoryAttributeRepository;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private NotificationService notificationService;

    @Override
    public ProductResponse createProduct(ProductRequest request, Integer userId, List<MultipartFile> images) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
                
        Shop shop = shopRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cửa hàng của người dùng này"));
                
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        Product product = productMapper.mapToEntity(request, shop, category);
                
        if (images != null && !images.isEmpty()) {
            boolean isFirst = true;
            for (MultipartFile file : images) {
                if (file.isEmpty()) continue;
                ProductImage productImage = productMapper.mapToProductImage(file, product, isFirst);
                if (productImage != null) {
                    product.getImages().add(productImage);
                    isFirst = false;
                }
            }
        }
                
        Product saved = productRepository.save(product);

        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            for (Map.Entry<Integer, String> entry : request.getAttributes().entrySet()) {
                categoryAttributeRepository.findById(entry.getKey()).ifPresent(catAttr -> {
                    ProductAttributeValue val = productMapper.mapToProductAttributeValue(catAttr, entry.getValue(), saved);
                    if (val != null) {
                        productAttributeValueRepository.save(val);
                    }
                });
            }
        }
        
        notificationService.sendToAllAdmins("Sản phẩm mới chờ duyệt", "Cửa hàng " + shop.getShopName() + " vừa đăng sản phẩm mới: " + saved.getProductName()) ;
        
        return productMapper.mapToResponse(saved);
    }

    @Override
    public List<ProductResponse> getProductsBySeller(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
                
        Shop shop = shopRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cửa hàng của người dùng này"));
                
        List<Product> products = productRepository.findByShop_ShopId(shop.getShopId());
        return products.stream()
                .filter(p -> p.getStatus() != ProductStatus.DELETED)
                .map(productMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse updateProduct(Integer productId, ProductRequest request, Integer userId, List<MultipartFile> images) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        
        if (!product.getShop().getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Không có quyền sửa sản phẩm này");
        }
        
        if (product.getStatus() == ProductStatus.ACTIVE) {
            throw new RuntimeException("Không thể sửa sản phẩm đang bán. Vui lòng ẩn sản phẩm trước.");
        }
        
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        productMapper.updateEntityFromRequest(request, product, category);
        if (product.getStatus() == ProductStatus.REJECTED) {
            product.setStatus(ProductStatus.PENDING);
        }

        if (images != null && !images.isEmpty() && images.stream().anyMatch(f -> !f.isEmpty())) {
            product.getImages().clear();
            boolean isFirst = true;
            for (MultipartFile file : images) {
                if (file.isEmpty()) continue;
                ProductImage productImage = productMapper.mapToProductImage(file, product, isFirst);
                if (productImage != null) {
                    product.getImages().add(productImage);
                    isFirst = false;
                }
            }
        }

        product.getAttributeValues().clear();
        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            for (Map.Entry<Integer, String> entry : request.getAttributes().entrySet()) {
                categoryAttributeRepository.findById(entry.getKey()).ifPresent(catAttr -> {
                    ProductAttributeValue val = productMapper.mapToProductAttributeValue(catAttr, entry.getValue(), product);
                    if (val != null) {
                        product.getAttributeValues().add(val);
                    }
                });
            }
        }
        
        Product saved = productRepository.save(product);
        return productMapper.mapToResponse(saved);
    }

    @Override
    public void deleteProduct(Integer productId, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
                
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
                
        if (!product.getShop().getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Không có quyền xóa sản phẩm này");
        }
        
        if (product.getStatus() == ProductStatus.ACTIVE) {
            throw new RuntimeException("Không thể xóa sản phẩm đang bán. Vui lòng ẩn sản phẩm trước.");
        }
        
        product.setStatus(ProductStatus.DELETED);
        productRepository.save(product);
    }
}
