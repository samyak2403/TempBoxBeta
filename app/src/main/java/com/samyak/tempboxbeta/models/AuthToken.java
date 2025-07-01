package com.samyak.tempboxbeta.models;

public class AuthToken {
    private String id;
    private String token;

    // Constructors
    public AuthToken() {}

    public AuthToken(String id, String token) {
        this.id = id;
        this.token = token;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    @Override
    public String toString() {
        return "AuthToken{" +
                "id='" + id + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
} 