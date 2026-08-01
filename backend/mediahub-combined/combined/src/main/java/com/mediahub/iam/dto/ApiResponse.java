package com.mediahub.iam.dto;

// Generic DTO used to wrap API responses in a consistent shape (status + message + payload).
// Serialized to JSON by Spring/Jackson and returned from controllers to the client.
public class ApiResponse {

    // The three response fields: a status indicator (e.g. "success"/"error"), a human-readable
    // message, and a generic "data" payload (Object so it can hold any response body).
    private String status;
    private String message;
    private Object data;

    // Constructor that builds the response in one shot, assigning all three fields.
    // Callers (controllers/services) use this to create a fully-populated response object.
    public ApiResponse(String status, String message, Object data) {
        this.status  = status;
        this.message = message;
        this.data    = data;
    }

    // Read-only getters that Spring/Jackson uses to serialize each field into the JSON output.
    // No setters are exposed, making the response object effectively immutable after construction.
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
}