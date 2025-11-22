package com.example.laundry_service.controller;

import com.example.laundry_service.domain.Order;
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
}