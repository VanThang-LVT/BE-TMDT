package com.lvt.tmdt.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "category_attributes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attr_id")
    private Integer attrId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "attr_name", nullable = false)
    private String attrName;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;
}
