package com.example.first_demo.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> { // <-- Added generic <T> parameter
    private boolean success;
    private String message;
    private String token;
    private T data;

    // Constructor 1: Simple message response (e.g., errors or simple success)
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Constructor 2: Success/Error with Data Payload
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Constructor 3: Success with Token AND Data (optional)
    public ApiResponse(boolean success, String message, String token, T data) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.data = data;
    }

    // Static Helpers
    public static ApiResponse<Map<String, Object>> success(String message, Map<String, Object> mapData) {
        return new ApiResponse<>(true, message, mapData);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message);
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}