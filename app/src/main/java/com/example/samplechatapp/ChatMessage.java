package com.example.samplechatapp;

public class ChatMessage {

    private String text;
    private String name;
    private long timestamp;
    private String avatarUrl;
    private String photoUrl;

    public ChatMessage() {
    }

    public ChatMessage(String text, String name, long timestamp, String avatarUrl, String photoUrl) {
        this.text = text;
        this.name = name;
        this.timestamp = timestamp;
        this.avatarUrl = avatarUrl;
        this.photoUrl = photoUrl;
    }

    // TODO: negative timestamp for empty message

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
