package com.example.samplechatapp;

public class ChatRoomInfo {
    private String name;
    private String lastMessage;

    public ChatRoomInfo(){
    }

    public ChatRoomInfo(String name, String lastMessage) {
        this.name = name;
        this.lastMessage = lastMessage;
    }

    public String getName(){
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastMessage(){
        return lastMessage;
    }

    public void setLastMessage(String lastMessage){
        this.lastMessage = lastMessage;
    }


}
