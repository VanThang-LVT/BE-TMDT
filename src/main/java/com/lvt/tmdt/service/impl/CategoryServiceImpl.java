package com.lvt.tmdt.service.impl;

import com.lvt.tmdt.dto.request.CategoryRequest;
import com.lvt.tmdt.dto.request.CategoryAttributeRequest;
import com.lvt.tmdt.dto.response.CategoryResponse;
import com.lvt.tmdt.dto.response.CategoryAttributeResponse;
import com.lvt.tmdt.entity.Category;
import com.lvt.tmdt.entity.CategoryAttribute;
import com.lvt.tmdt.enums.CategoryStatus;
import com.lvt.tmdt.repository.CategoryRepository;
import com.lvt.tmdt.service.intf.CategoryService;
import com.lvt.tmdt.repository.CategoryAttributeRepository;
import com.lvt.tmdt.mapper.CategoryMapper;
import com.lvt.tmdt.mapper.CategoryAttributeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private CategoryAttributeRepository categoryAttributeRepository;

    @Autowired
    private CategoryAttributeMapper categoryAttributeMapper;

    @Override
    public List<CategoryResponse> getAllActiveCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .filter(c -> c.getStatus() == CategoryStatus.ACTIVE)
                .map(categoryMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = categoryMapper.mapToEntity(request);
        return categoryMapper.mapToResponse(categoryRepository.save(category));
    }

    @Override
    public CategoryResponse updateCategory(Short id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        categoryMapper.updateEntityFromRequest(category, request);
        boolean statusChanged = request.getStatus() != null && category.getStatus() != request.getStatus();
        if (statusChanged) {
            category.setStatus(request.getStatus());
        }
        Category savedCategory = categoryRepository.save(category);
        if (statusChanged) {
            List<Category> allCategories = categoryRepository.findAll();
            cascadeStatus(savedCategory.getCategoryId(), request.getStatus(), allCategories);
        }
        return categoryMapper.mapToResponse(savedCategory);
    }

    private void cascadeStatus(Short parentId, CategoryStatus newStatus, List<Category> allCategories) {
        for (Category cat : allCategories) {
            if (cat.getParentId() != null && cat.getParentId().equals(parentId)) {
                if (cat.getStatus() != newStatus) {
                    cat.setStatus(newStatus);
                    categoryRepository.save(cat);
                }
                cascadeStatus(cat.getCategoryId(), newStatus, allCategories);
            }
        }
    }

    @Override
    public List<CategoryAttributeResponse> getAttributesByCategory(Short categoryId) {
        return categoryAttributeRepository.findByCategory_CategoryId(categoryId).stream()
                .map(categoryAttributeMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryAttributeResponse addCategoryAttribute(Short categoryId, CategoryAttributeRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
                
        CategoryAttribute attr = categoryAttributeMapper.mapToEntity(request, category);
        attr = categoryAttributeRepository.save(attr);
        return categoryAttributeMapper.mapToResponse(attr);
    }

    @Override
    public void deleteCategoryAttribute(Integer attrId) {
        if (!categoryAttributeRepository.existsById(attrId)) {
            throw new RuntimeException("Không tìm thấy thuộc tính");
        }
        categoryAttributeRepository.deleteById(attrId);
    }

    @Override
    public CategoryAttributeResponse updateCategoryAttribute(Integer attrId, CategoryAttributeRequest request) {
        CategoryAttribute attr = categoryAttributeRepository.findById(attrId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thuộc tính"));
        
        attr.setAttrName(request.getAttrName());
        attr.setIsRequired(request.getIsRequired());
        
        attr = categoryAttributeRepository.save(attr);
        return categoryAttributeMapper.mapToResponse(attr);
    }
}
