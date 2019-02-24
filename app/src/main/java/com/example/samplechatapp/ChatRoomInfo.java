package com.example.samplechatapp;

public class ChatRoomInfo {
    private String name;
    private long timestamp;
    private String description;

    public ChatRoomInfo(){
    }

    public ChatRoomInfo(String name, long timestamp, String description) {
        this.name = name;
        this.timestamp = timestamp;
        this.description = description;
    }

    public String getName(){
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimestamp(){
        return timestamp;
    }

    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
