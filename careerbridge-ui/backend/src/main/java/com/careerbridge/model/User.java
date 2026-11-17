package com.careerbridge.model;

public abstract class User {
    private String name, email, passwordHash, role;
    public User(String name, String email, String passwordHash, String role) {
        this.name=name; this.email=email; this.passwordHash=passwordHash; this.role=role;
    }
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getEmail(){return email;} public String getPasswordHash(){return passwordHash;}
    public String getRole(){return role;}
    public abstract String getDashboardType();
}
