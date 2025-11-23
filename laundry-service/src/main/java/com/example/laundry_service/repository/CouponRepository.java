package com.example.laundry_service.repository;

import com.example.laundry_service.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    Optional<Coupon> findFirstByUserIdAndValidTrue(UUID userId);
}