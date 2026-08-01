package com.mediahub.notification.dto.response;

import com.mediahub.notification.entity.Notification;
import java.time.LocalDateTime;
import java.time.LocalDate;
public class NotificationResponseDTO {

    private Long notificationId;
    private Long userId;
    private String message;
    private Notification.Category category;
    private Notification.Status status;
    private LocalDateTime createdDate;

    private Integer licenseId;
    private Integer contentId;
    private LocalDate expiryDate;

    public Long getNotificationId() {
        return notificationId; }
    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId; }

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

    public Notification.Status getStatus() {
        return status; }
    public void setStatus(Notification.Status status) {
        this.status = status; }

    public LocalDateTime getCreatedDate() {
        return createdDate; }
    public void setCreatedDate(
            LocalDateTime createdDate) {
        this.createdDate = createdDate; }
    
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