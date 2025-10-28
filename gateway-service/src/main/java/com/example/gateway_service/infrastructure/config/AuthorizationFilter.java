package com.example.gateway_service.infrastructure.config;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.gateway_service.domain.user.vo.RoleType;

import reactor.core.publisher.Mono;

public class AuthorizationFilter implements WebFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public static final Map<String, RoleType> routeRole = Map.of(
    "/cliente-service/", RoleType.FUNCIONARIO,
    "/roupa-service/", RoleType.FUNCIONARIO,
    "/servico-service/", RoleType.FUNCIONARIO,
    "/pedido-service/", RoleType.CLIENTE
);

    private boolean isAuthorized(String path, RoleType role) {
        for (Map.Entry<String, RoleType> entry: routeRole.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                return role.covers(entry.getValue());
            }
        }

        return true;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        // Se a rota não exige autenticação, segue
        if (routeRole.entrySet().stream().noneMatch(entry -> path.startsWith(entry.getKey()))) {
            return chain.filter(exchange);
        }

        // Verifica se o token está no header da req como "Authorization" e inicia com "Bearer "
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }


        // decodifica e valida o jwt
        String token = authHeader.substring(7);
        DecodedJWT jwt;
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret.getBytes(StandardCharsets.UTF_8));
            JWTVerifier verifier = JWT.require(algorithm).build();
            jwt = verifier.verify(token);
        } catch(Exception e) {
            return unauthorized(exchange);
        }

        // verifica se o jwt é access
        String tokenType = jwt.getClaim("type").asString();
        if (!tokenType.equals("access")) {
            return unauthorized(exchange);
        }

        // verifica se está com uma role valida
        String userRole = jwt.getClaim("role").asString();
        RoleType roleType = null;
        try {
            roleType = RoleType.valueOf(userRole);
        } catch (Exception e) {
            return unauthorized(exchange);
        }

        // verifica a permissão com base na role 
        if (!isAuthorized(path, roleType)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }


        return chain.filter(exchange);
    }
}
