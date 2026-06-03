package com.lvt.tmdt.repository;

import com.lvt.tmdt.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Short> {
    @Query("SELECT c FROM Category c WHERE :keyword IS NULL OR LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Category> searchCategories(@Param("keyword") String keyword);
}
