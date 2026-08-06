package com.mediahub.editorial.service;

import com.mediahub.editorial.client.ContentCatalogClient;
import com.mediahub.editorial.client.NotificationClient;
import com.mediahub.editorial.model.PublicationSchedule;
import com.mediahub.editorial.model.EditorialReview;
import com.mediahub.editorial.model.ContentCollection;
import com.mediahub.editorial.repository.PublicationScheduleRepository;
import com.mediahub.editorial.repository.EditorialReviewRepository;
import com.mediahub.editorial.repository.ContentCollectionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PublicationScheduleService {

        @Autowired
        private PublicationScheduleRepository repository;

        @Autowired
        private EditorialReviewRepository reviewRepository;

        @Autowired
        private ContentCollectionRepository collectionRepository;
        @Autowired
        private ContentCatalogClient contentCatalogClient;

        @Autowired
        private NotificationClient notificationClient;

        // API 13 — Create schedule
        public Map<String, Object> createSchedule(
                        PublicationSchedule schedule, Long actorUserId) {

                Map<String, Object> response = new HashMap<>();

                if (schedule.getContentID() == 0) {
                        response.put("error", "ContentID is required");
                        response.put("statusCode", 400);
                        return response;
                }

                if (schedule.getPublishDateTime() == null) {
                        response.put("error", "PublishDateTime is required");
                        response.put("statusCode", 400);
                        return response;
                }

                if (schedule.getExpiryDateTime() == null) {
                        response.put("error", "ExpiryDateTime is required");
                        response.put("statusCode", 400);
                        return response;
                }

                if (schedule.getTerritory() == null
                                || schedule.getTerritory().isEmpty()) {

                        response.put("error", "Territory is required");
                        response.put("statusCode", 400);
                        return response;
                }

                schedule.setStatus("Scheduled");

                PublicationSchedule saved = repository.save(schedule);

                // Notification Module Call — internal record-keeping notice for admin. The
                // subscriber-facing "new content available" notice fires later, when the content
                // actually goes Published (see ContentAssetService.updateContentStatus), not here
                // at mere scheduling time.
                notificationClient.sendNotification(
                                1L,
                                "Content "
                                                + saved.getContentID()
                                                + " scheduled for publication",
                                "EDITORIAL");

                if (actorUserId != null) {
                        notificationClient.sendNotification(
                                        actorUserId,
                                        "You scheduled Content " + saved.getContentID() + " for publication",
                                        "EDITORIAL");
                }

                response.put("scheduleID",
                                saved.getScheduleID());

                response.put("contentID",
                                saved.getContentID());

                response.put("publishDateTime",
                                saved.getPublishDateTime());

                response.put("expiryDateTime",
                                saved.getExpiryDateTime());

                response.put("territory",
                                saved.getTerritory());

                response.put("status",
                                "Scheduled");

                response.put("message",
                                "Content scheduled successfully.");

                response.put("statusCode",
                                201);

                return response;
        }

        // API 14 — Get all schedules
        public List<PublicationSchedule> getAllSchedules() {

                return repository.findAll();
        }

        // API 15 — Get schedule by ID
        public Map<String, Object> getScheduleById(
                        int scheduleID) {

                Map<String, Object> response = new HashMap<>();

                Optional<PublicationSchedule> opt = repository.findById(scheduleID);

                if (opt.isPresent()) {

                        response.put(
                                        "schedule",
                                        opt.get());

                        response.put(
                                        "statusCode",
                                        200);

                } else {

                        response.put(
                                        "error",
                                        "Schedule not found with ID: "
                                                        + scheduleID);

                        response.put(
                                        "statusCode",
                                        404);
                }

                return response;
        }

        // API 16 — Publish manually
        // API 16 — Publish manually
        public Map<String, Object> publishSchedule(
                        int scheduleID, Long actorUserId) {

                Map<String, Object> response = new HashMap<>();

                Optional<PublicationSchedule> opt = repository.findById(scheduleID);

                if (opt.isPresent()) {

                        PublicationSchedule schedule = opt.get();

                        schedule.setStatus("Published");

                        repository.save(schedule);

                        // ✅ Update Content Catalog Module
                        contentCatalogClient.publishContent(
                                        schedule.getContentID());

                        notificationClient.sendNotification(

                                        1L,

                                        "Content "
                                                        + schedule.getContentID()
                                                        + " published successfully",
                                        "EDITORIAL");

                        if (actorUserId != null) {
                                notificationClient.sendNotification(
                                                actorUserId,
                                                "You published Content " + schedule.getContentID(),
                                                "EDITORIAL");
                        }

                        response.put(
                                        "scheduleID",
                                        scheduleID);

                        response.put(
                                        "status",
                                        "Published");

                        response.put(
                                        "message",
                                        "Content published successfully.");

                        response.put(
                                        "statusCode",
                                        200);

                } else {

                        response.put(
                                        "error",
                                        "Schedule not found");

                        response.put(
                                        "statusCode",
                                        404);
                }

                return response;
        }

        // API 17 — Cancel schedule
        public Map<String, Object> cancelSchedule(
                        int scheduleID,
                        String reason, Long actorUserId) {

                Map<String, Object> response = new HashMap<>();

                Optional<PublicationSchedule> opt = repository.findById(scheduleID);

                if (opt.isPresent()) {

                        PublicationSchedule schedule = opt.get();

                        schedule.setStatus("Cancelled");

                        repository.save(schedule);
                        notificationClient.sendNotification(
                                        1L,
                                        "Publication schedule cancelled for content "
                                                        + schedule.getContentID(),
                                        "EDITORIAL");

                        if (actorUserId != null) {
                                notificationClient.sendNotification(
                                                actorUserId,
                                                "You cancelled the publication schedule for Content " + schedule.getContentID(),
                                                "EDITORIAL");
                        }

                        response.put(
                                        "scheduleID",
                                        scheduleID);

                        response.put(
                                        "status",
                                        "Cancelled");

                        response.put(
                                        "reason",
                                        reason);

                        response.put(
                                        "message",
                                        "Schedule cancelled successfully.");

                        response.put(
                                        "statusCode",
                                        200);

                } else {

                        response.put(
                                        "error",
                                        "Schedule not found");

                        response.put(
                                        "statusCode",
                                        404);
                }

                return response;
        }

        // API 18 — Delete schedule
        public Map<String, Object> deleteSchedule(
                        int scheduleID, Long actorUserId) {

                Map<String, Object> response = new HashMap<>();

                Optional<PublicationSchedule> opt = repository.findById(scheduleID);

                if (opt.isPresent()) {

                        PublicationSchedule schedule = opt.get();

                        if ("Published".equals(
                                        schedule.getStatus())) {

                                response.put(
                                                "error",
                                                "Cannot delete Published schedule.");

                                response.put(
                                                "statusCode",
                                                400);

                                return response;
                        }

                        repository.deleteById(scheduleID);

                        if (actorUserId != null) {
                                notificationClient.sendNotification(
                                                actorUserId,
                                                "You deleted the publication schedule for Content " + schedule.getContentID(),
                                                "EDITORIAL");
                        }

                        response.put(
                                        "scheduleID",
                                        scheduleID);

                        response.put(
                                        "message",
                                        "Schedule deleted successfully.");

                        response.put(
                                        "statusCode",
                                        200);

                } else {

                        response.put(
                                        "error",
                                        "Schedule not found");

                        response.put(
                                        "statusCode",
                                        404);
                }

                return response;
        }

        // ✅ ANALYTICS METHOD
        public Map<String, Object> getEditorialAnalytics() {

                List<EditorialReview> reviews = reviewRepository.findAll();

                List<ContentCollection> collections = collectionRepository.findAll();

                List<PublicationSchedule> schedules = repository.findAll();

                Map<String, Object> response = new HashMap<>();

                response.put(
                                "message",
                                "Editorial analytics retrieved successfully");

                response.put(
                                "totalReviews",
                                reviews.size());

                response.put(
                                "totalCollections",
                                collections.size());

                response.put(
                                "totalSchedules",
                                schedules.size());

                long pendingReviews = reviews.stream()
                                .filter(r -> "Pending".equalsIgnoreCase(
                                                r.getStatus()))
                                .count();

                long completedReviews = reviews.stream()
                                .filter(r -> "Completed".equalsIgnoreCase(
                                                r.getStatus()))
                                .count();

                long approvedReviews = reviews.stream()
                                .filter(r -> "Approved".equalsIgnoreCase(
                                                r.getDecision()))
                                .count();

                long rejectedReviews = reviews.stream()
                                .filter(r -> "Rejected".equalsIgnoreCase(
                                                r.getDecision()))
                                .count();

                long scheduledPublications = schedules.stream()
                                .filter(s -> "Scheduled".equalsIgnoreCase(
                                                s.getStatus()))
                                .count();

                long publishedPublications = schedules.stream()
                                .filter(s -> "Published".equalsIgnoreCase(
                                                s.getStatus()))
                                .count();

                long cancelledPublications = schedules.stream()
                                .filter(s -> "Cancelled".equalsIgnoreCase(
                                                s.getStatus()))
                                .count();

                response.put(
                                "pendingReviews",
                                pendingReviews);

                response.put(
                                "completedReviews",
                                completedReviews);

                response.put(
                                "approvedReviews",
                                approvedReviews);

                response.put(
                                "rejectedReviews",
                                rejectedReviews);

                response.put(
                                "scheduledPublications",
                                scheduledPublications);

                response.put(
                                "publishedPublications",
                                publishedPublications);

                response.put(
                                "cancelledPublications",
                                cancelledPublications);

                return response;
        }
}