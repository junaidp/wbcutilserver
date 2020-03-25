package com.wbc.utilserver.model.switchprobe;

public class ArpData {
    private int ifNum;
    private String ip;
    private String mac;

    public ArpData() {
    }

    public int getIfNum() {
        return ifNum;
    }

    public void setIfNum(int ifNum) {
        this.ifNum = ifNum;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
