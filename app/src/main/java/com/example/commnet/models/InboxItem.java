package com.example.commnet.models;

import java.util.Date;

public class InboxItem {
    private String senderEmail;
    private String message;
    private Date timestamp;

    public InboxItem() {
        // Required for Firebase
    }

    public InboxItem(String senderEmail, String message, Date timestamp) {
        this.senderEmail = senderEmail;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
