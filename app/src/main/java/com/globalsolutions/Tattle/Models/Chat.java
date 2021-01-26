package com.globalsolutions.Tattle.Models;

public class Chat {
    String sender;
    String receiver;
    String message;
    boolean isImage;

    public Chat() {
    }

    public Chat(String sender, String receiver, String message, boolean isImage) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isImage = isImage;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isImage() {
        return isImage;
    }

    public void setImage(boolean image) {
        isImage = image;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", message='" + message + '\'' +
                ", isImage=" + isImage +
                '}';
    }
}
