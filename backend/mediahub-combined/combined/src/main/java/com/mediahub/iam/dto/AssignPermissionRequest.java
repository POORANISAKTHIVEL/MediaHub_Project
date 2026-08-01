package com.mediahub.iam.dto;

// DTO (Data Transfer Object) carrying the request body for the "assign permission to role" endpoint.
// Spring deserializes the incoming JSON into this object so the controller can read its field.
public class AssignPermissionRequest {
    // Id of the permission to assign to the role; populated from the JSON request body.
    private Long permissionId;

    // Getter and setter for "permissionId" — Spring/Jackson uses these to map the JSON field
    // to the object during deserialization and to read the value back out.
    public Long getPermissionId() { return permissionId; }
    public void setPermissionId(Long permissionId) { this.permissionId = permissionId; }
}