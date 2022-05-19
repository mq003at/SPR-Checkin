package com.spr.selfcheck;

public class EventLog {
    private int dateStamp;
    private String direction;
    private String timeStamp;

    public EventLog(int dateStamp, String direction, String timeStamp) {
        this.dateStamp = dateStamp;
        this.direction = direction;
        this.timeStamp = timeStamp;
    }

    public EventLog() {
    }

    public int getDateStamp() {
        return dateStamp;
    }

    public void setDateStamp(int dateStamp) {
        this.dateStamp = dateStamp;
    }

    public String getdirection() {
        return direction;
    }

    public void setdirection(String direction) {
        this.direction = direction;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
