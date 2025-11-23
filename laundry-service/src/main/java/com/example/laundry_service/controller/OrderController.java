package com.example.laundry_service.controller;

import com.example.laundry_service.domain.Order;
import com.example.laundry_service.repository.CouponRepository;
import com.example.laundry_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/laundry")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository repository;
    private final CouponRepository couponRepository;

    public record CreateOrderRequest(String clothesType, Order.WashType washType, String notes) {}
    public record PriceOrderRequest(BigDecimal price) {}

    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody CreateOrderRequest request) {
        
        Order order = new Order(userId, request.clothesType(), request.washType(), request.notes());
        return ResponseEntity.ok(repository.save(order));
    }

    @GetMapping("/orders/my")
    public ResponseEntity<List<Order>> myOrders(@RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(repository.findAllByUserId(userId));
    }

    @GetMapping("/admin/orders")
    public ResponseEntity<List<Order>> allOrders() {
        return ResponseEntity.ok(repository.findAll());
    }
   
    @PutMapping("/admin/orders/{id}/price")
    public ResponseEntity<Order> setPrice(@PathVariable UUID id, @RequestBody PriceOrderRequest request) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));
        
        order.setPrice(request.price());
        order.setStatus(Order.OrderStatus.PRICED);
        
        return ResponseEntity.ok(repository.save(order));
    }

    @PostMapping("/orders/{id}/pay")
    public ResponseEntity<Order> payOrder(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));

        if (!order.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esse pedido não é seu");
        }
        if (order.getStatus() != Order.OrderStatus.PRICED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O pedido precisa estar precificado para ser pago");
        }

        couponRepository.findFirstByUserIdAndValidTrue(userId).ifPresent(coupon -> {
            BigDecimal desconto = BigDecimal.valueOf(coupon.getValue());
            BigDecimal novoPreco = order.getPrice().subtract(desconto);
            

            if (novoPreco.compareTo(BigDecimal.ZERO) < 0) {
                novoPreco = BigDecimal.ZERO;
            }

            order.setPrice(novoPreco);
            
            coupon.setValid(false);
            couponRepository.save(coupon);
        });

        order.setStatus(Order.OrderStatus.PAID);
        return ResponseEntity.ok(repository.save(order));
    }

    @PutMapping("/admin/orders/{id}/complete")
    public ResponseEntity<Order> completeOrder(@PathVariable UUID id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));

        if (order.getStatus() != Order.OrderStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O pedido precisa estar pago para ser finalizado");
        }

        order.setStatus(Order.OrderStatus.COMPLETED);
        return ResponseEntity.ok(repository.save(order));
    }

    @PostMapping("/orders/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));

        if (!order.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado");
        }

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível cancelar um pedido que já está em processamento ou finalizado.");
        }

        repository.delete(order);
        
        return ResponseEntity.noContent().build();
    }
}