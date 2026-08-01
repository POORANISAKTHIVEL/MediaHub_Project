package com.mediahub.subscriptionPlan.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String roles;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;
    private String country;

    @Column(nullable = false)
    @Builder.Default
    private String status = "Active";
}
