package com.samyak.tempboxbeta.models;

import com.google.gson.annotations.SerializedName;

public class Domain {
    @SerializedName("@id")
    private String atId;
    
    @SerializedName("@type")
    private String atType;
    
    @SerializedName("@context")
    private String atContext;
    
    private String id;
    private String domain;
    private boolean isActive;
    private boolean isPrivate;
    private String createdAt;
    private String updatedAt;

    // Constructors
    public Domain() {}

    public Domain(String id, String domain, boolean isActive, boolean isPrivate) {
        this.id = id;
        this.domain = domain;
        this.isActive = isActive;
        this.isPrivate = isPrivate;
    }

    // Getters and Setters
    public String getAtId() { return atId; }
    public void setAtId(String atId) { this.atId = atId; }

    public String getAtType() { return atType; }
    public void setAtType(String atType) { this.atType = atType; }

    public String getAtContext() { return atContext; }
    public void setAtContext(String atContext) { this.atContext = atContext; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Domain{" +
                "id='" + id + '\'' +
                ", domain='" + domain + '\'' +
                ", isActive=" + isActive +
                ", isPrivate=" + isPrivate +
                '}';
    }
} 