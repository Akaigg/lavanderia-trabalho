package com.example.laundry_service.messaging.listener;

import com.example.laundry_service.domain.Coupon;
import com.example.laundry_service.messaging.event.UserCreatedEvent;
import com.example.laundry_service.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCreatedListener {

    private final CouponRepository couponRepository;

    @RabbitListener(queues = "cupom-queue")
    public void onUserCreated(UserCreatedEvent event) {
        Coupon coupon = new Coupon(event.userId(), 20.00);
        couponRepository.save(coupon);
        System.out.println("--- Cupom criado para o usuário: " + event.email() + " ---");
    }
}