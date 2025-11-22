package com.example.laundry_service.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String clothesType;

    @Enumerated(EnumType.STRING)
    private WashType washType; 

    private String notes; 

    private BigDecimal price; 

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    public enum WashType { DRY, NORMAL }
    public enum OrderStatus { PENDING, PRICED, COMPLETED }

    public Order(UUID userId, String clothesType, WashType washType, String notes) {
        this.userId = userId;
        this.clothesType = clothesType;
        this.washType = washType;
        this.notes = notes;
        this.status = OrderStatus.PENDING;
    }
}