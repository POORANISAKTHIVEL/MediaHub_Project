package com.mediahub.royalty.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "RoyaltyRule")
public class RoyaltyRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RuleID")
    private int ruleID;

    @Column(name = "CreatorTier", nullable = false)
    private String creatorTier;

    @Column(name = "RevenueSharePercent", nullable = false)
    private double revenueSharePercent;

    @Column(name = "MinimumPayoutThreshold")
    private double minimumPayoutThreshold;

    @Column(name = "PayoutFrequency")
    private String payoutFrequency;


    @Column(name = "EffectiveDate")
    private Date effectiveDate;

    @Column(name = "Status")
    private String status;

    public RoyaltyRule() {
        this.status = "Active";
    }

    public int getRuleID()                      { return ruleID; }
    public String getCreatorTier()              { return creatorTier; }
    public double getRevenueSharePercent()      { return revenueSharePercent; }
    public double getMinimumPayoutThreshold()   { return minimumPayoutThreshold; }
    public String getPayoutFrequency()          { return payoutFrequency; }
    public Date getEffectiveDate()              { return effectiveDate; }
    public String getStatus()                   { return status; }

    public void setRuleID(int ruleID)
        { this.ruleID = ruleID; }
    public void setCreatorTier(String creatorTier)
        { this.creatorTier = creatorTier; }
    public void setRevenueSharePercent(double revenueSharePercent)
        { this.revenueSharePercent = revenueSharePercent; }
    public void setMinimumPayoutThreshold(double minimumPayoutThreshold)
        { this.minimumPayoutThreshold = minimumPayoutThreshold; }
    public void setPayoutFrequency(String payoutFrequency)
        { this.payoutFrequency = payoutFrequency; }
    public void setEffectiveDate(Date effectiveDate)
        { this.effectiveDate = effectiveDate; }
    public void setStatus(String status)
        { this.status = status; }
}
