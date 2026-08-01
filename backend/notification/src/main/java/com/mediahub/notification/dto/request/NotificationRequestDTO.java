package com.mediahub.notification.dto.request;

import com.mediahub.notification.entity.Notification;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class NotificationRequestDTO {

    private Long userId;
    @Size(max = 100, message = "Message cannot exceed 100 characters")
    private String message;
    private Notification.Category category;
    private Integer licenseId;
    private Integer contentId;
    private LocalDate expiryDate;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) {
        this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) {
        this.message = message; }

    public Notification.Category getCategory() {
        return category; }
    public void setCategory(
            Notification.Category category) {
        this.category = category; }
    
    public Integer getLicenseId() {
        return licenseId;
   }

    public void setLicenseId(Integer licenseId) {
        this.licenseId = licenseId;
    }

    public Integer getContentId() {
        return contentId;
    }

    public void setContentId(Integer contentId) {
        this.contentId = contentId;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }    
}