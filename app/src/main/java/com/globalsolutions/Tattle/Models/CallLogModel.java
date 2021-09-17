package com.globalsolutions.Tattle.Models;

public class CallLogModel {
    String number;
    String type;
    String date;
    String duration;
    String name;

    public CallLogModel() {

    }

    public CallLogModel(String number, String type, String date, String duration, String name) {
        this.number = number;
        this.type = type;
        this.date = date;
        this.duration = duration;
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "CallLog{" +
                "number='" + number + '\'' +
                ", type='" + type + '\'' +
                ", date='" + date + '\'' +
                ", duration='" + duration + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
