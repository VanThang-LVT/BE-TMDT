package com.lvt.tmdt.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id", columnDefinition = "TINYINT")
    private Integer roleId;

    @Column(name = "role_name", length = 20, unique = true, nullable = false)
    private String roleName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
