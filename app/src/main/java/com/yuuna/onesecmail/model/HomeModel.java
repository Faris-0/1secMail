package com.yuuna.onesecmail.model;

import com.google.gson.annotations.SerializedName;

public class HomeModel {

    @SerializedName("username")
    private String username;

    @SerializedName("domain")
    private String domain;

    public HomeModel(String username, String domain) {
        this.username = username;
        this.domain = domain;
    }

    public String getUsername() {
        return username;
    }

    public String getDomain() {
        return domain;
    }
}
