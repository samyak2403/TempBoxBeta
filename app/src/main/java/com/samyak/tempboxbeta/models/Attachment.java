package com.samyak.tempboxbeta.models;

public class Attachment {
    private String id;
    private String filename;
    private String contentType;
    private String disposition;
    private String transferEncoding;
    private boolean related;
    private int size;
    private String downloadUrl;

    // Constructors
    public Attachment() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getDisposition() { return disposition; }
    public void setDisposition(String disposition) { this.disposition = disposition; }

    public String getTransferEncoding() { return transferEncoding; }
    public void setTransferEncoding(String transferEncoding) { this.transferEncoding = transferEncoding; }

    public boolean isRelated() { return related; }
    public void setRelated(boolean related) { this.related = related; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    @Override
    public String toString() {
        return "Attachment{" +
                "filename='" + filename + '\'' +
                ", contentType='" + contentType + '\'' +
                ", size=" + size +
                '}';
    }
} 