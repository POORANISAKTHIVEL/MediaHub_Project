package com.mediahub.editorial.service;

import com.mediahub.editorial.client.NotificationClient;
import com.mediahub.editorial.model.EditorialReview;
import com.mediahub.editorial.repository.EditorialReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EditorialReviewService {

    @Autowired
    private EditorialReviewRepository repository;

    @Autowired
    private NotificationClient notificationClient;

    // API 1 — Submit review
    public Map<String, Object> submitReview(EditorialReview review) {
        Map<String, Object> response = new HashMap<>();
        if (review.getContentID() == 0) {
            response.put("error", "ContentID is required");
            response.put("statusCode", 400);
            return response;
        }
        if (review.getReviewerID() == 0) {
            response.put("error", "ReviewerID is required");
            response.put("statusCode", 400);
            return response;
        }
        if (review.getSubmissionDate() == null)
            review.setSubmissionDate(new Date());
        review.setStatus("Pending");
        EditorialReview saved = repository.save(review);
        notificationClient.sendNotification(
                Long.valueOf(saved.getReviewerID()),
                "New content submitted for review. Content ID: "
                        + saved.getContentID(),
                "EDITORIAL");
        response.put("reviewID", saved.getReviewID());
        response.put("contentID", saved.getContentID());
        response.put("reviewerID", saved.getReviewerID());
        response.put("submissionDate", saved.getSubmissionDate());
        response.put("status", "Pending");
        response.put("message",
                "Content submitted for review successfully");
        response.put("statusCode", 201);
        return response;
    }

    // API 2 — Get all reviews
    public List<EditorialReview> getAllReviews() {
        return repository.findAll();
    }

    // ✅ ROYALTY VALIDATION — check if content has an Approved editorial review
    public boolean validateApproval(int contentId) {
        return repository.existsByContentIDAndDecision(contentId, "Approved");
    }

    // API 3 — Get review by ID
    public Map<String, Object> getReviewById(int reviewID) {
        Map<String, Object> response = new HashMap<>();
        Optional<EditorialReview> opt = repository.findById(reviewID);
        if (opt.isPresent()) {
            response.put("review", opt.get());
            response.put("statusCode", 200);
        } else {
            response.put("error",
                    "Review not found with ID: " + reviewID);
            response.put("statusCode", 404);
        }
        return response;
    }

    // API 4 — Approve review
    public Map<String, Object> approveReview(
            int reviewID, String remarks) {
        return applyDecision(reviewID, "Approved",
                remarks, "Completed",
                "Content approved successfully.");
    }

    // API 5 — Reject review
    public Map<String, Object> rejectReview(
            int reviewID, String remarks) {
        return applyDecision(reviewID, "Rejected",
                remarks, "Completed",
                "Content rejected. Creator notified.");
    }

    // API 6 — Request revision
    public Map<String, Object> requestRevision(
            int reviewID, String remarks) {
        return applyDecision(reviewID, "RevisionRequired",
                remarks, "Pending",
                "Revision requested. Creator notified.");
    }

    private Map<String, Object> applyDecision(
            int reviewID, String decision,
            String remarks, String status, String message) {
        Map<String, Object> response = new HashMap<>();
        Optional<EditorialReview> opt = repository.findById(reviewID);

        if (opt.isPresent()) {
            EditorialReview review = opt.get();
            review.setDecision(decision);
            review.setRemarks(remarks);
            review.setReviewDate(new Date());
            review.setStatus(status);
            repository.save(review);

            notificationClient.sendNotification(
                    Long.valueOf(review.getReviewerID()),
                    "Content " + review.getContentID()
                            + " approved successfully",
                    "EDITORIAL");

            if ("Approved".equals(decision)) {

                notificationClient.sendNotification(

                        Long.valueOf(review.getReviewerID()),

                        "Content "
                                + review.getContentID()
                                + " approved successfully",
                        "EDITORIAL");

            }
            if ("Rejected".equals(decision)) {

                notificationClient.sendNotification(

                        Long.valueOf(review.getReviewerID()),

                        "Content "
                                + review.getContentID()
                                + " rejected.",

                        "EDITORIAL");

            }

            if ("RevisionRequired".equals(decision)) {

                notificationClient.sendNotification(

                        Long.valueOf(review.getReviewerID()),

                        "Revision requested for Content "
                                + review.getContentID(),

                        "EDITORIAL");

            }

            response.put("reviewID", reviewID);
            response.put("decision", decision);
            response.put("status", status);
            response.put("message", message);
            response.put("statusCode", 200);
        } else {
            response.put("error", "Review not found");
            response.put("statusCode", 404);
        }
        return response;
    }
}
