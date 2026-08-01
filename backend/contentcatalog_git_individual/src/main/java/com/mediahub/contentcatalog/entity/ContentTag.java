package com.mediahub.contentcatalog.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "content_tag")
public class ContentTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tagId")
    private int tagId;

    @Column(name = "contentId")
    private int contentId;

    @Column(name = "tagName")
    private String tagName;

    @Column(name = "tagCategory")
    private String tagCategory;

    public int getTagId() { return tagId; }
    public void setTagId(int tagId) { this.tagId = tagId; }

    public int getContentId() { return contentId; }
    public void setContentId(int contentId) { this.contentId = contentId; }

    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }

    public String getTagCategory() { return tagCategory; }
    public void setTagCategory(String tagCategory) { this.tagCategory = tagCategory; }
}