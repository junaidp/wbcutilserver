package com.wbc.utilserver.model.switchprobe;

public class MacInfo {

    private String key;
    private int vlanNum;
    private String mac;
    private int port;
    private String portDescription;
    private int descid;
    private String ip;
    private String name;
    private String vendor;

    public MacInfo() {
    }

    public int getVlanNum() {
        return vlanNum;
    }

    public void setVlanNum(int vlanNum) {
        this.vlanNum = vlanNum;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPortDescription() {
        return portDescription;
    }

    public void setPortDescription(String portDescription) {
        this.portDescription = portDescription;
    }

    public int getDescid() {
        return descid;
    }

    public void setDescid(int descid) {
        this.descid = descid;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public void setKey( String key )
    {
        this.key = key;
    }

    public static String convertToSpaceMac( String noSpaceMac ) {
        StringBuilder sb = new StringBuilder();
        sb.append( noSpaceMac.substring(0,2));
        sb.append(" ");
        sb.append( noSpaceMac.substring(2,4));
        sb.append(" ");
        sb.append( noSpaceMac.substring(4,6));
        sb.append(" ");
        sb.append( noSpaceMac.substring(6,8));
        sb.append(" ");
        sb.append( noSpaceMac.substring(8,10));
        sb.append(" ");
        sb.append( noSpaceMac.substring(10));
        return sb.toString();
    }

}
