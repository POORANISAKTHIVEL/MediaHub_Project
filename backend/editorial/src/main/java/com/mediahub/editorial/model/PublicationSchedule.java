package com.mediahub.editorial.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "PublicationSchedule")
public class PublicationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ScheduleID")
    private int scheduleID;

    @Column(name = "ContentID")
    private int contentID;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "PublishDateTime")
    private Date publishDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ExpiryDateTime")
    private Date expiryDateTime;

    @Column(name = "Territory")
    private String territory;

    @Column(name = "Status")
    private String status;

    public PublicationSchedule() {
        this.status = "Scheduled";
    }

    // Getters
    public int getScheduleID()          { return scheduleID; }
    public int getContentID()           { return contentID; }
    public Date getPublishDateTime()    { return publishDateTime; }
    public Date getExpiryDateTime()     { return expiryDateTime; }
    public String getTerritory()        { return territory; }
    public String getStatus()           { return status; }

    // Setters
    public void setScheduleID(int scheduleID)               { this.scheduleID = scheduleID; }
    public void setContentID(int contentID)                 { this.contentID = contentID; }
    public void setPublishDateTime(Date publishDateTime)    { this.publishDateTime = publishDateTime; }
    public void setExpiryDateTime(Date expiryDateTime)      { this.expiryDateTime = expiryDateTime; }
    public void setTerritory(String territory)              { this.territory = territory; }
    public void setStatus(String status)                    { this.status = status; }
}
