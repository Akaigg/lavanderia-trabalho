package com.example.gateway_service.infrastructure.config;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.gateway_service.domain.user.vo.RoleType;

import reactor.core.publisher.Mono;

@Component
public class AuthorizationFilter implements WebFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    // Define quais rotas exigem permissão de ADMIN
    private static final Map<String, RoleType> routePermissions = Map.of(
        "/laundry/admin", RoleType.ADMIN,
        "/users", RoleType.ADMIN // A listagem (GET) é protegida, o cadastro (POST) liberamos abaixo
    );

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();
        HttpMethod method = request.getMethod();

        // --- AQUI ESTÁ A CORREÇÃO MÁGICA ---
        // Deixa passar:
        // 1. Login (/auth/login...)
        // 2. Eureka (/eureka...)
        // 3. Cadastro de Usuário (Apenas se for POST em /users)
        boolean isPublicRoute = path.startsWith("/auth/login") || 
                                path.contains("/eureka") ||
                                (path.equals("/users") && method.equals(HttpMethod.POST));

        if (isPublicRoute) {
            return chain.filter(exchange);
        }
        // ----------------------------------

        // Daqui para baixo, verifica o Crachá (Token)
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);
        DecodedJWT jwt;
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret.getBytes(StandardCharsets.UTF_8));
            JWTVerifier verifier = JWT.require(algorithm).build();
            jwt = verifier.verify(token);
        } catch(Exception e) {
            return unauthorized(exchange);
        }

        String roleStr = jwt.getClaim("role").asString();
        String userId = jwt.getSubject();
        RoleType userRole;
        
        try {
            userRole = RoleType.valueOf(roleStr);
        } catch (Exception e) {
            return unauthorized(exchange);
        }

        // Verifica se tem permissão de ADMIN para rotas restritas
        for (Map.Entry<String, RoleType> entry : routePermissions.entrySet()) {
            // Se a rota é protegida (ex: /laundry/admin)
            if (path.startsWith(entry.getKey())) {
                // Se for /users, só bloqueia se NÃO for POST (ou seja, GET para listar)
                if (path.equals("/users") && method.equals(HttpMethod.POST)) {
                    continue; // Deixa passar o cadastro
                }
                
                if (!userRole.covers(entry.getValue())) {
                    return forbidden(exchange);
                }
            }
        }

        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", userId)
                .header("X-User-Role", roleStr)
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }
}