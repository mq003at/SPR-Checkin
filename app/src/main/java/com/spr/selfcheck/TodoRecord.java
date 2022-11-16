package com.spr.selfcheck;

import java.util.Date;

public class TodoRecord {
    private String date;
    private String dateStamp;
    private boolean from_employee;
    private String name;
    private String recipient;
    private boolean check;
    private String message;


    public TodoRecord(String date, String dateStamp, String recipient, boolean check, String message) {
        this.date = date;
        this.dateStamp = dateStamp;
        this.recipient = recipient;
        this.check = check;
        this.message = message;
        from_employee = false;
        name = "SPR-Kirppis Android App";
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDateStamp() {
        return dateStamp;
    }

    public void setDateStamp(String dateStamp) {
        this.dateStamp = dateStamp;
    }

    public boolean isFrom_employee() {
        return from_employee;
    }

    public void setFrom_employee(boolean from_employee) {
        this.from_employee = from_employee;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
