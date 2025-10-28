package com.example.auth_service.domain.user.vo;

import lombok.Getter;

@Getter
public enum RoleType {
    CLIENTE(1),    
    FUNCIONARIO(2), 
    ADMIN(3);     

    @Getter
    private final int level;

    RoleType(int level) {
        this.level = level;
    }

    public boolean covers(RoleType other) {
        return this.level >= other.level;
    }
}
