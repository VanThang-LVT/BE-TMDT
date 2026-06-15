package com.lvt.tmdt.service.impl;

import com.lvt.tmdt.dto.request.ProductRequest;
import com.lvt.tmdt.dto.response.ProductResponse;
import com.lvt.tmdt.entity.Category;
import com.lvt.tmdt.entity.CategoryAttribute;
import com.lvt.tmdt.entity.Product;
import com.lvt.tmdt.entity.ProductImage;
import com.lvt.tmdt.entity.ProductAttributeValue;
import com.lvt.tmdt.entity.ProductApprovalHistory;
import com.lvt.tmdt.entity.ProductVariant;
import com.lvt.tmdt.entity.VariantAttribute;
import com.lvt.tmdt.dto.request.ProductVariantRequest;
import com.lvt.tmdt.entity.Shop;
import com.lvt.tmdt.entity.User;
import com.lvt.tmdt.enums.ApprovalStatus;
import com.lvt.tmdt.enums.ProductStatus;
import com.lvt.tmdt.repository.CategoryRepository;
import com.lvt.tmdt.repository.ProductRepository;
import com.lvt.tmdt.repository.ProductApprovalHistoryRepository;
import com.lvt.tmdt.repository.ShopRepository;
import com.lvt.tmdt.repository.UserRepository;
import com.lvt.tmdt.repository.ProductAttributeValueRepository;
import com.lvt.tmdt.repository.CategoryAttributeRepository;
import com.lvt.tmdt.repository.ProductImageRepository;
import com.lvt.tmdt.service.intf.ProductService;
import com.lvt.tmdt.service.intf.NotificationService;
import com.lvt.tmdt.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
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
    private ProductApprovalHistoryRepository productApprovalHistoryRepository;

    @Autowired
    private ProductAttributeValueRepository productAttributeValueRepository;

    @Autowired
    private CategoryAttributeRepository categoryAttributeRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

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
                if (file.isEmpty())
                    continue;
                ProductImage productImage = productMapper.mapToProductImage(file, product, isFirst);
                if (productImage != null) {
                    product.getImages().add(productImage);
                    isFirst = false;
                }
            }
        }

        Product saved = productRepository.save(product);

        final Product finalSaved = saved;
        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            for (Map.Entry<Integer, String> entry : request.getAttributes().entrySet()) {
                categoryAttributeRepository.findById(entry.getKey()).ifPresent(catAttr -> {
                    ProductAttributeValue val = productMapper.mapToProductAttributeValue(catAttr, entry.getValue(),
                            finalSaved);
                    if (val != null) {
                        productAttributeValueRepository.save(val);
                    }
                });
            }
        }

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            for (ProductVariantRequest vReq : request.getVariants()) {
                ProductVariant variant = new ProductVariant();
                variant.setProduct(saved);
                variant.setSku(vReq.getSku());
                variant.setPrice(vReq.getPrice() != null ? vReq.getPrice() : request.getPrice());
                variant.setStockQuantity(vReq.getStockQuantity() != null ? vReq.getStockQuantity() : request.getStockQuantity());
                
                String imageUrl = vReq.getImageUrl();
                if (imageUrl != null && imageUrl.startsWith("data:image")) {
                    try {
                        String[] parts = imageUrl.split(",");
                        String base64Data = parts[1];
                        
                        String contentType = "image/jpeg"; // default
                        try {
                            String metaPart = parts[0].split(";")[0]; // "data:image/png"
                            if (metaPart.contains(":")) {
                                contentType = metaPart.split(":")[1];
                            }
                        } catch (Exception ignored) {}

                        byte[] decodedBytes = java.util.Base64.getMimeDecoder().decode(base64Data);

                        ProductImage pImage = new ProductImage();
                        pImage.setProduct(saved);
                        pImage.setImageData(decodedBytes);
                        pImage.setContentType(contentType);
                        pImage.setIsMain(false);
                        pImage = productImageRepository.save(pImage);
                        log.info("Saved variant image with ID: {}", pImage.getImageId());
                        saved.getImages().add(pImage);
                        variant.setImageUrl("/api/public/images/" + pImage.getImageId());
                    } catch (Exception e) {
                        log.error("FAILED TO SAVE VARIANT IMAGE", e);
                        variant.setImageUrl(null);
                    }
                } else {
                    variant.setImageUrl(imageUrl);
                }
                
                List<VariantAttribute> vAttrs = new ArrayList<>();
                if (vReq.getAttributes() != null) {
                    for (Map.Entry<Integer, String> entry : vReq.getAttributes().entrySet()) {
                        CategoryAttribute catAttr = categoryAttributeRepository.findById(entry.getKey()).orElse(null);
                        if (catAttr != null) {
                            VariantAttribute vAttr = new VariantAttribute();
                            vAttr.setVariant(variant);
                            vAttr.setCategoryAttribute(catAttr);
                            vAttr.setValueString(entry.getValue());
                            vAttrs.add(vAttr);
                        }
                    }
                }
                variant.setVariantAttributes(vAttrs);
                saved.getVariants().add(variant);
            }
            saved = productRepository.save(saved);
        }

        notificationService.sendToAllAdmins("Sản phẩm mới chờ duyệt",
                "Cửa hàng " + shop.getShopName() + " vừa đăng sản phẩm mới: " + saved.getProductName());

        return productMapper.mapToResponse(saved);
    }

    @Override
    public List<ProductResponse> getProductsBySeller(Integer userId, String keyword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Shop shop = shopRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cửa hàng của người dùng này"));

        List<Product> products;
        if (keyword != null && !keyword.trim().isEmpty()) {
            products = productRepository.searchBySeller(shop.getShopId(), keyword.trim());
        } else {
            products = productRepository.findByShop_ShopId(shop.getShopId());
        }
        return products.stream()
                .filter(p -> p.getStatus() != ProductStatus.DELETED)
                .map(productMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse updateProduct(Integer productId, ProductRequest request, Integer userId,
            List<MultipartFile> images) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (!product.getShop().getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Không có quyền sửa sản phẩm này");
        }

        ProductStatus initialStatus = product.getStatus();
        boolean requiresReapproval = (initialStatus == ProductStatus.ACTIVE) && isReapprovalRequired(product, request, images);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        productMapper.updateEntityFromRequest(request, product, category);
        
        if (initialStatus == ProductStatus.REJECTED) {
            product.setStatus(ProductStatus.PENDING);
        } else if (initialStatus == ProductStatus.ACTIVE && requiresReapproval) {
            product.setStatus(ProductStatus.PENDING);
        }

        // Extract all existing variant image IDs
        List<Integer> variantImageIds = new ArrayList<>();
        for (ProductVariant v : product.getVariants()) {
            if (v.getImageUrl() != null && v.getImageUrl().startsWith("/api/public/images/")) {
                try {
                    String idStr = v.getImageUrl().substring("/api/public/images/".length());
                    variantImageIds.add(Integer.parseInt(idStr));
                } catch (Exception ignored) {}
            }
        }

        // Add them to existingImageIdsToKeep so they are not deleted
        List<Integer> keepIds = request.getExistingImageIdsToKeep();
        if (keepIds == null) {
            keepIds = new ArrayList<>();
        }
        keepIds.addAll(variantImageIds);

        log.debug("Protecting Variant Image IDs: {}", variantImageIds);
        log.debug("All Image IDs to keep: {}", keepIds);

        // Handle existing images
        final List<Integer> finalKeepIds = keepIds;
        product.getImages().removeIf(img -> !finalKeepIds.contains(img.getImageId()));

        // Set main image among existing
        if (request.getMainImageId() != null) {
            for (ProductImage img : product.getImages()) {
                img.setIsMain(img.getImageId().equals(request.getMainImageId()));
            }
        } else {
            // Unset all existing if a new file is main
            for (ProductImage img : product.getImages()) {
                img.setIsMain(false);
            }
        }

        if (images != null && !images.isEmpty() && images.stream().anyMatch(f -> !f.isEmpty())) {
            boolean isFirst = (request.getMainImageId() == null);
            for (MultipartFile file : images) {
                if (file.isEmpty())
                    continue;
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
                    ProductAttributeValue val = productMapper.mapToProductAttributeValue(catAttr, entry.getValue(),
                            product);
                    if (val != null) {
                        product.getAttributeValues().add(val);
                    }
                });
            }
        }

        List<ProductVariant> existingVariants = new ArrayList<>(product.getVariants());
        product.getVariants().clear();

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            for (ProductVariantRequest vReq : request.getVariants()) {
                ProductVariant variant = null;
                if (vReq.getVariantId() != null) {
                    variant = existingVariants.stream()
                            .filter(v -> vReq.getVariantId().equals(v.getVariantId()))
                            .findFirst()
                            .orElse(null);
                }
                if (variant == null) {
                    variant = new ProductVariant();
                    variant.setProduct(product);
                    variant.setVariantAttributes(new ArrayList<>());
                }

                variant.setSku(vReq.getSku());
                variant.setPrice(vReq.getPrice() != null ? vReq.getPrice() : request.getPrice());
                variant.setStockQuantity(vReq.getStockQuantity() != null ? vReq.getStockQuantity() : request.getStockQuantity());
                
                String imageUrl = vReq.getImageUrl();
                if (imageUrl != null && imageUrl.startsWith("data:image")) {
                    try {
                        String[] parts = imageUrl.split(",");
                        String base64Data = parts[1];
                        
                        String contentType = "image/jpeg"; // default
                        try {
                            String metaPart = parts[0].split(";")[0]; // "data:image/png"
                            if (metaPart.contains(":")) {
                                contentType = metaPart.split(":")[1];
                            }
                        } catch (Exception ignored) {
                            log.debug("Could not parse content type from {}", parts[0]);
                        }

                        byte[] decodedBytes = java.util.Base64.getMimeDecoder().decode(base64Data);

                        ProductImage pImage = new ProductImage();
                        pImage.setProduct(product);
                        pImage.setImageData(decodedBytes);
                        pImage.setContentType(contentType);
                        pImage.setIsMain(false);
                        pImage = productImageRepository.save(pImage);
                        log.info("Saved variant image with ID: {}", pImage.getImageId());
                        product.getImages().add(pImage);
                        variant.setImageUrl("/api/public/images/" + pImage.getImageId());
                    } catch (Exception e) {
                        log.error("FAILED TO SAVE VARIANT IMAGE", e);
                        variant.setImageUrl(null);
                    }
                } else {
                    variant.setImageUrl(imageUrl);
                }
                
                if (variant.getVariantAttributes() != null) {
                    variant.getVariantAttributes().clear();
                } else {
                    variant.setVariantAttributes(new ArrayList<>());
                }

                if (vReq.getAttributes() != null) {
                    for (Map.Entry<Integer, String> entry : vReq.getAttributes().entrySet()) {
                        CategoryAttribute catAttr = categoryAttributeRepository.findById(entry.getKey()).orElse(null);
                        if (catAttr != null) {
                            VariantAttribute vAttr = new VariantAttribute();
                            vAttr.setVariant(variant);
                            vAttr.setCategoryAttribute(catAttr);
                            vAttr.setValueString(entry.getValue());
                            variant.getVariantAttributes().add(vAttr);
                        }
                    }
                }
                product.getVariants().add(variant);
            }
        }

        Product saved = productRepository.save(product);
        
        boolean transitionedToPending = (initialStatus != ProductStatus.PENDING && saved.getStatus() == ProductStatus.PENDING);
        if (transitionedToPending) {
            notificationService.sendToAllAdmins("Sản phẩm cập nhật chờ duyệt",
                    "Cửa hàng " + saved.getShop().getShopName() + " vừa cập nhật sản phẩm: " + saved.getProductName());
        }
        
        return productMapper.mapToResponse(saved);
    }

    private boolean isReapprovalRequired(Product product, ProductRequest request, List<MultipartFile> images) {
        if (!Objects.equals(product.getProductName(), request.getProductName())) return true;
        if (!Objects.equals(product.getDescription(), request.getDescription())) return true;
        if (!Objects.equals(product.getBrand(), request.getBrand())) return true;
        if (!Objects.equals(product.getKeywords(), request.getKeywords())) return true;
        if (!Objects.equals(product.getSpecifications(), request.getSpecifications())) return true;

        if (product.getCategory() == null || !Objects.equals(product.getCategory().getCategoryId(), request.getCategoryId())) return true;

        if (product.getPrice() == null && request.getPrice() != null) return true;
        if (product.getPrice() != null && request.getPrice() == null) return true;
        if (product.getPrice() != null && request.getPrice() != null && product.getPrice().compareTo(request.getPrice()) != 0) return true;

        if (images != null && !images.isEmpty() && images.stream().anyMatch(f -> !f.isEmpty())) return true;

        Map<Integer, String> requestAttrs = request.getAttributes() != null ? request.getAttributes() : new HashMap<>();
        Map<Integer, String> currentAttrs = new HashMap<>();
        if (product.getAttributeValues() != null) {
            for (ProductAttributeValue val : product.getAttributeValues()) {
                if (val.getCategoryAttribute() != null) {
                    currentAttrs.put(val.getCategoryAttribute().getAttrId(), val.getValueString());
                }
            }
        }
        if (!currentAttrs.equals(requestAttrs)) return true;

        if (product.getVariants().size() != (request.getVariants() == null ? 0 : request.getVariants().size())) return true;
        // Simple heuristic: if there are variants, just require reapproval. Comparing nested attributes deeply is complex.
        if (request.getVariants() != null && !request.getVariants().isEmpty()) return true;

        return false;
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

    @Override
    public List<ProductResponse> getAllProductsForAdmin(String keyword) {
        List<Product> products;
        if (keyword != null && !keyword.trim().isEmpty()) {
            products = productRepository.searchForAdmin(keyword.trim());
        } else {
            products = productRepository.findAll();
        }
        return products.stream()
                .filter(p -> p.getStatus() != ProductStatus.DELETED)
                .map(productMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse approveProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (product.getStatus() != ProductStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể duyệt sản phẩm đang chờ duyệt");
        }

        product.setStatus(ProductStatus.ACTIVE);
        Product saved = productRepository.save(product);
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByEmail(email).orElse(null);
        if (admin != null) {
            ProductApprovalHistory history = ProductApprovalHistory.builder()
                .product(saved)
                .admin(admin)
                .status(ApprovalStatus.APPROVED)
                .note("Được duyệt thành công")
                .build();
            productApprovalHistoryRepository.save(history);
        }
        
        notificationService.createNotification(product.getShop().getUser(), "Sản phẩm được phê duyệt", "Sản phẩm '" + product.getProductName() + "' của bạn đã được hệ thống phê duyệt thành công.");
        return productMapper.mapToResponse(saved);
    }

    @Override
    public ProductResponse rejectProduct(Integer productId, String reason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (product.getStatus() != ProductStatus.PENDING && product.getStatus() != ProductStatus.ACTIVE) {
            throw new RuntimeException("Chỉ có thể từ chối sản phẩm đang chờ duyệt hoặc đang hoạt động");
        }

        product.setStatus(ProductStatus.REJECTED);
        Product saved = productRepository.save(product);
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByEmail(email).orElse(null);
        if (admin != null) {
            ProductApprovalHistory history = ProductApprovalHistory.builder()
                .product(saved)
                .admin(admin)
                .status(ApprovalStatus.REJECTED)
                .note(reason != null && !reason.trim().isEmpty() ? reason.trim() : "Từ chối/Khóa sản phẩm")
                .build();
            productApprovalHistoryRepository.save(history);
        }
        
        String notifContent = "Sản phẩm '" + product.getProductName() + "' của bạn đã bị từ chối hoặc bị khóa do vi phạm chính sách.";
        if (reason != null && !reason.trim().isEmpty()) {
            notifContent += " Lý do: " + reason.trim();
        }
        
        notificationService.createNotification(product.getShop().getUser(), "Sản phẩm bị từ chối/khóa", notifContent);
        return productMapper.mapToResponse(saved);
    }

    @Override
    public List<ProductResponse> getAllActiveProducts(String keyword, Short categoryId) {
        String kw = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        List<Product> products;
        
        if (categoryId != null) {
            List<Short> categoryIds = new ArrayList<>();
            categoryIds.add(categoryId);
            
            // Find all child categories recursively
            List<Category> allCategories = categoryRepository.findAll();
            java.util.Queue<Short> queue = new java.util.LinkedList<>();
            queue.add(categoryId);
            
            while (!queue.isEmpty()) {
                Short currentId = queue.poll();
                for (Category cat : allCategories) {
                    if (cat.getParentId() != null && cat.getParentId().equals(currentId)) {
                        if (!categoryIds.contains(cat.getCategoryId())) {
                            categoryIds.add(cat.getCategoryId());
                            queue.add(cat.getCategoryId());
                        }
                    }
                }
            }
            
            products = productRepository.searchActiveProductsByCategoryIds(kw, categoryIds);
        } else {
            products = productRepository.searchAllActiveProducts(kw);
        }
        
        return products.stream().map(productMapper::mapToResponse).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public ProductResponse getProductById(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new RuntimeException("Sản phẩm không khả dụng");
        }
        return productMapper.mapToResponse(product);
    }
}
