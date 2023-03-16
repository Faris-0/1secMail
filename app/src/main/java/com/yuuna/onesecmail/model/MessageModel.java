package com.yuuna.onesecmail.model;

import com.google.gson.annotations.SerializedName;

public class MessageModel {

    @SerializedName("id")
    private Integer id;

    @SerializedName("from")
    private String from;

    @SerializedName("subject")
    private String subject;

    @SerializedName("date")
    private String date;

    @SerializedName("htmlBody")
    private String htmlBody;

    public Integer getId() {
        return id;
    }

    public String getFrom() {
        return from;
    }

    public String getSubject() {
        return subject;
    }

    public String getDate() {
        return date;
    }

    public String getHtmlBody() {
        return htmlBody;
    }
}
