package com.lvt.tmdt.controller;

import com.lvt.tmdt.entity.ProductImage;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/images")
public class ImageController {

    @Autowired
    private EntityManager entityManager;

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Integer id) {
        ProductImage image = entityManager.find(ProductImage.class, id);
        if (image == null || image.getImageData() == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        HttpHeaders headers = new HttpHeaders();
        String contentType = image.getContentType();
        if (contentType == null) {
            contentType = "image/jpeg";
        }
        headers.setContentType(MediaType.parseMediaType(contentType));
        
        return new ResponseEntity<>(image.getImageData(), headers, HttpStatus.OK);
    }
}
