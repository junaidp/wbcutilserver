package com.wbc.utilserver.model.switchprobe;

public class IpData {
    private String ip;
    private int interfaceNum;
    private String netMask;

    public IpData() {
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getInterfaceNum() {
        return interfaceNum;
    }

    public void setInterfaceNum(int interfaceNum) {
        this.interfaceNum = interfaceNum;
    }

    public String getNetMask() {
        return netMask;
    }

    public void setNetMask(String netMask) {
        this.netMask = netMask;
    }
}


