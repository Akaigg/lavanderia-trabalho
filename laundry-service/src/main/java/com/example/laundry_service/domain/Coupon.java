package com.example.laundry_service.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    private boolean valid;
    private double value;

    public Coupon(UUID userId, double value) {
        this.userId = userId;
        this.value = value;
        this.valid = true;
    }
}