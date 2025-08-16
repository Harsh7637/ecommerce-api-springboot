package com.harsh.ecommerce.dto;

public class LoginResponseDto {
    private String token;
    private UserResponseDto user;
    private String message;
    private boolean success;
    private long expiresIn = 86400; // 24 hours in seconds

    // Constructors
    public LoginResponseDto() {}

    public LoginResponseDto(String token, UserResponseDto user, String message) {
        this.token = token;
        this.user = user;
        this.message = message;
        this.success = true;
    }

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public UserResponseDto getUser() { return user; }
    public void setUser(UserResponseDto user) { this.user = user; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
}
