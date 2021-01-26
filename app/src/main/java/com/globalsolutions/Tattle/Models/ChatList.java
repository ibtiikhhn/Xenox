package com.globalsolutions.Tattle.Models;

public class ChatList {

    String opponentName;
    String opponentIMG;
    String senderId;
    String receiverId;
    String combinedId;
    String lastMessage;

    public ChatList() {
    }

    public ChatList(String opponentName, String opponentIMG, String senderId, String receiverId, String combinedId, String lastMessage) {
        this.opponentName = opponentName;
        this.opponentIMG = opponentIMG;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.combinedId = combinedId;
        this.lastMessage = lastMessage;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }

    public String getOpponentIMG() {
        return opponentIMG;
    }

    public void setOpponentIMG(String opponentIMG) {
        this.opponentIMG = opponentIMG;
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

    @Override
    public String toString() {
        return "ChatList{" +
                "opponentName='" + opponentName + '\'' +
                ", opponentIMG='" + opponentIMG + '\'' +
                ", senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", combinedId='" + combinedId + '\'' +
                ", lastMessage='" + lastMessage + '\'' +
                '}';
    }
}
