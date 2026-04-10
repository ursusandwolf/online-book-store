package com.lisu.onlinestore.dao;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message, Exception e) {
        super(message, e);
    }
}
