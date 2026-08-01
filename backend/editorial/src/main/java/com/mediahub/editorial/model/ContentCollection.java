package com.mediahub.editorial.model;

import com.mediahub.editorial.converter.IntegerListConverter;
import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "ContentCollection")
public class ContentCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CollectionID")
    private int collectionID;

    @Column(name = "Name")
    private String name;

    @Column(name = "Category")
    private String category;

    @Convert(converter = IntegerListConverter.class)
    @Column(name = "ContentIDs")
    private List<Integer> contentIDs;

    @Temporal(TemporalType.DATE)
    @Column(name = "PublishDate")
    private Date publishDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "ExpiryDate")
    private Date expiryDate;

    @Column(name = "Status")
    private String status;

    public ContentCollection() {
        this.status = "Scheduled";
    }

    // Getters
    public int getCollectionID()        { return collectionID; }
    public String getName()             { return name; }
    public String getCategory()         { return category; }
    public List<Integer> getContentIDs(){ return contentIDs; }
    public Date getPublishDate()        { return publishDate; }
    public Date getExpiryDate()         { return expiryDate; }
    public String getStatus()           { return status; }

    // Setters
    public void setCollectionID(int collectionID)       { this.collectionID = collectionID; }
    public void setName(String name)                    { this.name = name; }
    public void setCategory(String category)            { this.category = category; }
    public void setContentIDs(List<Integer> contentIDs) { this.contentIDs = contentIDs; }
    public void setPublishDate(Date publishDate)         { this.publishDate = publishDate; }
    public void setExpiryDate(Date expiryDate)           { this.expiryDate = expiryDate; }
    public void setStatus(String status)                 { this.status = status; }
}
