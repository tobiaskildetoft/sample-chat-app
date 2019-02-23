package com.example.samplechatapp;

public class ChatRoomInfo {
    private String name;
    private long timestamp;

    public ChatRoomInfo(){
    }

    public ChatRoomInfo(String name, long timestamp) {
        this.name = name;
        this.timestamp = timestamp;
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


}
