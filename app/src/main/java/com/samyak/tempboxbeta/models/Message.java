package com.samyak.tempboxbeta.models;

import com.google.gson.annotations.SerializedName;
import com.google.gson.JsonElement;
import java.util.List;

public class Message {
    @SerializedName("@id")
    private String atId;
    
    @SerializedName("@type")
    private String atType;
    
    @SerializedName("@context")
    private String atContext;
    
    private String id;
    private String accountId;
    private String msgid;
    private EmailAddress from;
    private List<EmailAddress> to;
    private List<String> cc;
    private List<String> bcc;
    private String subject;
    private String intro; // Short preview text
    private String text; // Plain text content
    private List<String> html; // HTML content
    private boolean seen;
    private boolean flagged;
    private boolean isDeleted;
    private JsonElement verifications; // Flexible to handle both object and array
    private boolean retention;
    private String retentionDate;
    private boolean hasAttachments;
    private List<Attachment> attachments;
    private int size;
    private String downloadUrl;
    private String createdAt;
    private String updatedAt;

    // Constructors
    public Message() {}

    // Getters and Setters
    public String getAtId() { return atId; }
    public void setAtId(String atId) { this.atId = atId; }

    public String getAtType() { return atType; }
    public void setAtType(String atType) { this.atType = atType; }

    public String getAtContext() { return atContext; }
    public void setAtContext(String atContext) { this.atContext = atContext; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getMsgid() { return msgid; }
    public void setMsgid(String msgid) { this.msgid = msgid; }

    public EmailAddress getFrom() { return from; }
    public void setFrom(EmailAddress from) { this.from = from; }

    public List<EmailAddress> getTo() { return to; }
    public void setTo(List<EmailAddress> to) { this.to = to; }

    public List<String> getCc() { return cc; }
    public void setCc(List<String> cc) { this.cc = cc; }

    public List<String> getBcc() { return bcc; }
    public void setBcc(List<String> bcc) { this.bcc = bcc; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public List<String> getHtml() { return html; }
    public void setHtml(List<String> html) { this.html = html; }

    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }

    public boolean isFlagged() { return flagged; }
    public void setFlagged(boolean flagged) { this.flagged = flagged; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public JsonElement getVerifications() { return verifications; }
    public void setVerifications(JsonElement verifications) { this.verifications = verifications; }
    
    // Helper method to get verifications as a string representation
    public String getVerificationsAsString() {
        if (verifications == null) return "";
        if (verifications.isJsonNull()) return "";
        if (verifications.isJsonPrimitive()) return verifications.getAsString();
        if (verifications.isJsonArray()) {
            return verifications.getAsJsonArray().toString();
        }
        if (verifications.isJsonObject()) {
            return verifications.getAsJsonObject().toString();
        }
        return verifications.toString();
    }

    public boolean isRetention() { return retention; }
    public void setRetention(boolean retention) { this.retention = retention; }

    public String getRetentionDate() { return retentionDate; }
    public void setRetentionDate(String retentionDate) { this.retentionDate = retentionDate; }

    public boolean hasAttachments() { return hasAttachments; }
    public void setHasAttachments(boolean hasAttachments) { this.hasAttachments = hasAttachments; }

    public List<Attachment> getAttachments() { return attachments; }
    public void setAttachments(List<Attachment> attachments) { this.attachments = attachments; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", subject='" + subject + '\'' +
                ", from=" + from +
                ", seen=" + seen +
                ", hasAttachments=" + hasAttachments +
                '}';
    }
} 