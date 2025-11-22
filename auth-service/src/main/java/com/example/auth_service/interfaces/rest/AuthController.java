package com.example.auth_service.interfaces.rest;

import com.example.auth_service.application.auth.PasswordLoginHandler;
import com.example.auth_service.application.auth.RequestMagicLinkHandler;
import com.example.auth_service.application.auth.VerifyMagicLinkHandler;
import com.example.auth_service.application.user.RegisterUserHandler;
import com.example.auth_service.interfaces.rest.dto.auth.MagicLinkRequest;
import com.example.auth_service.interfaces.rest.dto.auth.MagicLinkVerifyRequest;
import com.example.auth_service.interfaces.rest.dto.auth.PasswordLoginRequest;
import com.example.auth_service.interfaces.rest.dto.auth.TokenResponse;
import com.example.auth_service.interfaces.rest.dto.user.UserRequest;
import com.example.auth_service.interfaces.rest.dto.user.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    
    private final PasswordLoginHandler passwordLoginHandler;
    private final RequestMagicLinkHandler requestMagicLinkHandler;
    private final VerifyMagicLinkHandler verifyMagicLinkHandler;
    private final RegisterUserHandler registerUserHandler;

    @PostMapping("/login/password")
    public ResponseEntity<TokenResponse> loginWithPassword(@Valid @RequestBody PasswordLoginRequest request) {
        TokenResponse token = passwordLoginHandler.handle(request.email(), request.password());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login/magic")
    public ResponseEntity<Void> requestMagic(@Valid @RequestBody MagicLinkRequest req) {
        requestMagicLinkHandler.handle(req.email());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("login/magic/verify")
    public ResponseEntity<TokenResponse> verifyMagic(@Valid @RequestBody MagicLinkVerifyRequest request) {
        TokenResponse tokenResponse = verifyMagicLinkHandler.handle(request.token());
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest user) {
        UserResponse created = registerUserHandler.handle(user.name(), user.email(), user.password());
        return ResponseEntity.created(URI.create("/users/" + created.id())).body(created);
    }
}