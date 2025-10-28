package com.example.gateway_service.domain.user.vo;


public enum RoleType {
    CLIENTE(1),
    FUNCIONARIO(2),
    ADMIN(3);


    private final int level;

    RoleType(int level) {
        this.level = level;
    }

    public boolean covers(RoleType other) {
        return this.level >= other.level;
    }

    public int getLevel() {
        return this.level;
    }
}
