package com.mediahub.editorial.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "EditorialReview")
public class EditorialReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReviewID")
    private int reviewID;

    @Column(name = "ContentID")
    private int contentID;

    @Column(name = "ReviewerID")
    private int reviewerID;

    @Temporal(TemporalType.DATE)
    @Column(name = "SubmissionDate")
    private Date submissionDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "ReviewDate")
    private Date reviewDate;

    @Column(name = "Decision")
    private String decision;

    @Column(name = "Remarks")
    private String remarks;

    @Column(name = "Status")
    private String status;

    public EditorialReview() {
        this.status = "Pending";
    }

    // Getters
    public int getReviewID()            { return reviewID; }
    public int getContentID()           { return contentID; }
    public int getReviewerID()          { return reviewerID; }
    public Date getSubmissionDate()     { return submissionDate; }
    public Date getReviewDate()         { return reviewDate; }
    public String getDecision()         { return decision; }
    public String getRemarks()          { return remarks; }
    public String getStatus()           { return status; }

    // Setters
    public void setReviewID(int reviewID)               { this.reviewID = reviewID; }
    public void setContentID(int contentID)             { this.contentID = contentID; }
    public void setReviewerID(int reviewerID)           { this.reviewerID = reviewerID; }
    public void setSubmissionDate(Date submissionDate)  { this.submissionDate = submissionDate; }
    public void setReviewDate(Date reviewDate)          { this.reviewDate = reviewDate; }
    public void setDecision(String decision)            { this.decision = decision; }
    public void setRemarks(String remarks)              { this.remarks = remarks; }
    public void setStatus(String status)                { this.status = status; }
}
