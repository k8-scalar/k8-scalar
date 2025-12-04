package com.example.mt_api.entity;

public class AuthRequest {
    private String name;
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String username) {
        this.name = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public AuthRequest(String username, String password) {
        this.name = username;
        this.password = password;
    }
}
