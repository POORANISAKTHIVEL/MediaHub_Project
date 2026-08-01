package com.mediahub.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notificationId")
    private Long notificationId;

    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(name = "message", nullable = false,
            length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "createdDate")
    private LocalDateTime createdDate;

    @Column(name = "licenseId")
    private Integer licenseId;

    @Column(name = "contentId")
    private Integer contentId;

    @Column(name = "expiryDate")
    private LocalDate expiryDate;


    public Notification() {}

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

    public Category getCategory() { return category; }
    public void setCategory(Category category) {
        this.category = category; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) {
        this.status = status; }

    public LocalDateTime getCreatedDate() {
        return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate; }

    public enum Category {
        CONTENT, SUBSCRIPTION, ROYALTY,
        LICENSE, EDITORIAL
    }

    public enum Status {
        UNREAD, READ, DISMISSED
    }
    
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