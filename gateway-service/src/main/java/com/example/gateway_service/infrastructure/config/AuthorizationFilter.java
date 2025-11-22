package com.example.gateway_service.infrastructure.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.gateway_service.domain.user.vo.RoleType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class AuthorizationFilter implements GlobalFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    // Define quais rotas exigem permissão de ADMIN
    private static final Map<String, RoleType> protectedRoutes = Map.of(
            "/laundry/admin", RoleType.ADMIN,
            "/auth/users", RoleType.ADMIN
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. Libera rotas públicas (Login e Registro)
        if (path.startsWith("/auth/login") || path.startsWith("/auth/register")) {
            return chain.filter(exchange);
        }

        // 2. Verifica se tem Header de Autorização
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        try {
            // 3. Valida o Token JWT
            String token = authHeader.substring(7);
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret.getBytes(StandardCharsets.UTF_8));
            DecodedJWT jwt = JWT.require(algorithm).build().verify(token);

            String userId = jwt.getSubject();
            String roleString = jwt.getClaim("role").asString();
            RoleType userRole = RoleType.valueOf(roleString);

            // 4. Verifica Permissão de Admin para rotas protegidas
            for (Map.Entry<String, RoleType> entry : protectedRoutes.entrySet()) {
                if (path.startsWith(entry.getKey())) {
                    if (!userRole.covers(entry.getValue())) {
                        return onError(exchange, HttpStatus.FORBIDDEN);
                    }
                }
            }

            // 5. Injeta ID e Role para os microsserviços usarem
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Role", roleString)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}