package com.samyak.tempboxbeta.models;

import com.google.gson.annotations.SerializedName;

public class Account {
    @SerializedName("@context")
    private String atContext;
    
    @SerializedName("@id")
    private String atId;
    
    @SerializedName("@type")
    private String atType;
    
    private String id;
    private String address;
    private String password; // Only used for account creation
    private int quota;
    private int used;
    private boolean isDisabled;
    private boolean isDeleted;
    private String createdAt;
    private String updatedAt;

    // Constructors
    public Account() {}

    public Account(String address, String password) {
        this.address = address;
        this.password = password;
    }

    // Getters and Setters
    public String getAtContext() { return atContext; }
    public void setAtContext(String atContext) { this.atContext = atContext; }

    public String getAtId() { return atId; }
    public void setAtId(String atId) { this.atId = atId; }

    public String getAtType() { return atType; }
    public void setAtType(String atType) { this.atType = atType; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getQuota() { return quota; }
    public void setQuota(int quota) { this.quota = quota; }

    public int getUsed() { return used; }
    public void setUsed(int used) { this.used = used; }

    public boolean isDisabled() { return isDisabled; }
    public void setDisabled(boolean disabled) { isDisabled = disabled; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Account{" +
                "id='" + id + '\'' +
                ", address='" + address + '\'' +
                ", quota=" + quota +
                ", used=" + used +
                ", isDisabled=" + isDisabled +
                ", isDeleted=" + isDeleted +
                '}';
    }
} 