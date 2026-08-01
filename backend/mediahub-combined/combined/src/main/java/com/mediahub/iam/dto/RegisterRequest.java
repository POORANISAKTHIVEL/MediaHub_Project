package com.mediahub.iam.dto;

// DTO (Data Transfer Object) carrying the request body for the user-registration endpoint.
// Spring deserializes the incoming JSON into this object so the controller can read the new-account details.
public class RegisterRequest {

    // The new-account fields supplied by the client, all populated from the JSON request body.
    private String name;
    private String email;
    private String password;
    private String phone;
    private String country;

    // Getters and setters for each field — Spring/Jackson uses the setters to map JSON fields onto
    // the object during deserialization, and the getters to read the values back out in the controller.
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}