package com.lvt.tmdt.service.intf;

import com.lvt.tmdt.dto.request.ProductRequest;
import com.lvt.tmdt.dto.response.ProductResponse;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request, Integer userId, List<MultipartFile> images);
    List<ProductResponse> getProductsBySeller(Integer userId, String keyword);
    ProductResponse updateProduct(Integer productId, ProductRequest request, Integer userId, List<MultipartFile> images);
    void deleteProduct(Integer productId, Integer userId);
    List<ProductResponse> getAllProductsForAdmin(String keyword);
    ProductResponse approveProduct(Integer productId);
    ProductResponse rejectProduct(Integer productId, String reason);
    List<ProductResponse> getAllActiveProducts(String keyword, Short categoryId);
    ProductResponse getProductById(Integer productId);
}
