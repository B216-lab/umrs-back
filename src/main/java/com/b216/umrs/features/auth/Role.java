package com.b216.umrs.features.auth;

/**
 * Role authority
 */
public enum Role {
    /**
     * Администратор - полный контроль
     */
    ADMIN,
    /**
     * Менеджер - контроль над данными
     */
    MANAGER,
    /**
     * Обычный пользователь
     */
    USER,
    /**
     * Разработчик - пользователь API
     */
    DEVELOPER,
}
