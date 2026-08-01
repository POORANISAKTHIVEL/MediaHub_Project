package com.mediahub.royalty.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "RoyaltyStatement")
public class RoyaltyStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "StatementID")
    private int statementID;

    @Column(name = "CreatorID", nullable = false)
    private int creatorID;

    @Column(name = "Period")
    private String period;

    @Column(name = "TotalViews")
    private long totalViews;

    @Column(name = "TotalRevenue")
    private double totalRevenue;

    @Column(name = "RoyaltyAmount")
    private double royaltyAmount;

    @Column(name = "Status")
    private String status;

    public RoyaltyStatement() {
        this.status = "Draft";
    }

    public int getStatementID()         { return statementID; }
    public int getCreatorID()           { return creatorID; }
    public String getPeriod()           { return period; }
    public long getTotalViews()         { return totalViews; }
    public double getTotalRevenue()     { return totalRevenue; }
    public double getRoyaltyAmount()    { return royaltyAmount; }
    public String getStatus()           { return status; }

    public void setStatementID(int statementID)
        { this.statementID = statementID; }
    public void setCreatorID(int creatorID)
        { this.creatorID = creatorID; }
    public void setPeriod(String period)
        { this.period = period; }
    public void setTotalViews(long totalViews)
        { this.totalViews = totalViews; }
    public void setTotalRevenue(double totalRevenue)
        { this.totalRevenue = totalRevenue; }
    public void setRoyaltyAmount(double royaltyAmount)
        { this.royaltyAmount = royaltyAmount; }
    public void setStatus(String status)
        { this.status = status; }
}
