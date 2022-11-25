package com.spr.selfcheck;

public class tempObject {
    public long count;
    public long baseCount;
    public String tempString;

    public tempObject(long count, String tempString) {
        this.count = 1;
        this.baseCount = count;
        this.tempString = tempString;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }


    public long getBaseCount() {
        return baseCount;
    }

    public void setBaseCount(long baseCount) {
        this.baseCount = baseCount;
    }

    public String getTempString() {
        return tempString;
    }

    public void setTempString(String tempString) {
        this.tempString = tempString;
    }

    public void increCount() {
        count++;
    }
}
