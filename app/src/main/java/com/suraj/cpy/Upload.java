package com.suraj.cpy;

public class Upload {
    private String filename, img_url, img_text, senderName;

    public Upload() {}

    public Upload(String filename, String img_url, String img_text, String senderName) {
        this.filename = filename;
        this.img_url = img_url;
        this.img_text = img_text;
        this.senderName = senderName;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getImg_text() {
        return img_text;
    }

    public void setImg_text(String img_text) {
        this.img_text = img_text;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}
