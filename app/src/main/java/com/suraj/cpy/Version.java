package com.suraj.cpy;

public class Version {
    private String base_version, local_version, website, notification;

    public Version() {}

    public Version(String base_version, String local_version, String website) {
        this.base_version = base_version;
        this.local_version = local_version;
        this.website = website;
        this.notification = "no";
    }

    public String getBase_version() {
        return base_version;
    }

    public void setBase_version(String base_version) {
        this.base_version = base_version;
    }

    public String getLocal_version() {
        return local_version;
    }

    public void setLocal_version(String local_version) {
        this.local_version = local_version;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }
}
