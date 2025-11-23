package com.example.laundry_service.messaging.event;

import java.util.UUID;

public record UserCreatedEvent(
    UUID userId,
    String email,
    String name,
    String role
) {}