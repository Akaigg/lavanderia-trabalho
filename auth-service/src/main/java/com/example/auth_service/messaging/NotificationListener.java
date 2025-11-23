package com.example.auth_service.messaging;

import com.example.auth_service.application.ports.MailSender;
import com.example.auth_service.messaging.events.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final MailSender mailSender;

    @RabbitListener(queues = RabbitConfig.NOTIFICATION_QUEUE)
    public void handleUserCreated(UserCreatedEvent event) {
        mailSender.sendWelcome(event.email(), event.name());
    }
}