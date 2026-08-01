package com.mediahub.royalty.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "RoyaltyPayout")
public class RoyaltyPayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PayoutID")
    private int payoutID;

    @Column(name = "StatementID", nullable = false)
    private int statementID;

    @Column(name = "CreatorID", nullable = false)
    private int creatorID;

    @Column(name = "Amount", nullable = false)
    private double amount;


    @Column(name = "PayoutDate")
    private Date payoutDate;

    @Column(name = "Method")
    private String method;

    @Column(name = "Status")
    private String status;

    public RoyaltyPayout() {
        this.status = "Pending";
    }

    public int getPayoutID()            { return payoutID; }
    public int getStatementID()         { return statementID; }
    public int getCreatorID()           { return creatorID; }
    public double getAmount()           { return amount; }
    public Date getPayoutDate()         { return payoutDate; }
    public String getMethod()           { return method; }
    public String getStatus()           { return status; }

    public void setPayoutID(int payoutID)
        { this.payoutID = payoutID; }
    public void setStatementID(int statementID)
        { this.statementID = statementID; }
    public void setCreatorID(int creatorID)
        { this.creatorID = creatorID; }
    public void setAmount(double amount)
        { this.amount = amount; }
    public void setPayoutDate(Date payoutDate)
        { this.payoutDate = payoutDate; }
    public void setMethod(String method)
        { this.method = method; }
    public void setStatus(String status)
        { this.status = status; }
}
