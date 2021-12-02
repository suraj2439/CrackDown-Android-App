package com.suraj.cpy;

import java.util.ArrayList;
import java.util.List;

public class Group {
    Long timestamp;
    List<String> users= new ArrayList<>();
    String admin;

    public Group() {};

    public Group(Long timestamp, String admin) {
        this.timestamp = timestamp;
        this.users.add(admin);
        this.admin = admin;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getUsers() {
        return users;
    }

    public void addUsers(String user) {
        this.users.add((user));
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }
}
