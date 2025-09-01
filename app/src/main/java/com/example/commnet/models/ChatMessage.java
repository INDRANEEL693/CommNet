package com.example.commnet.models;

import java.util.Date;

public class ChatMessage {
    private String sender;
    private String message;
    private Date timestamp;

    public ChatMessage() {}

    public ChatMessage(String sender, String message, Date timestamp) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
