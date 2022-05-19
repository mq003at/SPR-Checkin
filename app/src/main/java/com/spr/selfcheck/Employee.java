package com.spr.selfcheck;

public class Employee {
    private String id;
    private EventLog[] eventLogs;

    public Employee(String id, EventLog[] eventLogs) {
        this.id = id;
        this.eventLogs = eventLogs;
    }
}
