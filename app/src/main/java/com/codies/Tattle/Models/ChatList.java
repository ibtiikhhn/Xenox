package com.codies.Tattle.Models;

public class ChatList {
    String senderName;
    String senderIMG;
    String combinedId;
    String lastMessage;
    String senderId;
    String receiverId;

    public ChatList() {

    }

    public ChatList(String senderName, String senderIMG, String combinedId, String lastMessage, String senderId, String receiverId) {
        this.senderName = senderName;
        this.senderIMG = senderIMG;
        this.combinedId = combinedId;
        this.lastMessage = lastMessage;
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderIMG() {
        return senderIMG;
    }

    public void setSenderIMG(String senderIMG) {
        this.senderIMG = senderIMG;
    }

    public String getCombinedId() {
        return combinedId;
    }

    public void setCombinedId(String combinedId) {
        this.combinedId = combinedId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    @Override
    public String toString() {
        return "ChatList{" +
                "senderName='" + senderName + '\'' +
                ", senderIMG='" + senderIMG + '\'' +
                ", combinedId='" + combinedId + '\'' +
                ", lastMessage='" + lastMessage + '\'' +
                ", senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                '}';
    }
}
