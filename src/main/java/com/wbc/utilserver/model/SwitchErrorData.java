package com.wbc.utilserver.model;

import java.time.ZonedDateTime;
import java.util.ArrayList;

public class SwitchErrorData {
    private String ip;
    private int descid;
    private String  port;
    private String portDesc;
    private ArrayList<Integer> values;
    // removed private ArrayList<String> times;
    private long valuesTotal; // just to make compatible with client model class which is then linked with the ui Grid.


    // public void setTimes( ArrayList<String> times ) { this.times = times; }

    public SwitchErrorData() {
        values = new ArrayList<Integer>();
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPortDesc() {
        return portDesc;
    }

    public void setPortDesc(String portDesc) {
        this.portDesc = portDesc;
    }

    public ArrayList<Integer> getValues() {
        return values;
    }

    public void setValues(ArrayList<Integer> values) {
        this.values = values;
    }

    public int getDescid() {
        return descid;
    }

    public void setDescid(int descid) {
        this.descid = descid;
    }

    public long getValuesTotal() {
        return valuesTotal;
    }

    public void setValuesTotal(long valuesTotal) {
        this.valuesTotal = valuesTotal;
    }
}

