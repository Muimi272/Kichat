package com.muimi.kichat.entity;

public enum Type {
    JOIN, LEAVE, CHAT, ERROR, PRIVATE, QUERY;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean equals(Type type) {
        return this.name().equals(type.name());
    }
}