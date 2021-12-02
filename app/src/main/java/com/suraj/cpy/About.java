package com.suraj.cpy;

public class About {
    String website, helpLink;

    public About(){}

    public About(String website, String helpLink) {
        this.website = website;
        this.helpLink = helpLink;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getHelpLink() {
        return helpLink;
    }

    public void setHelpLink(String helpLink) {
        this.helpLink = helpLink;
    }
}
