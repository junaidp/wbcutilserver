package com.wbc.utilserver.model.switchprobe;

public class CiscoVlanData {
    int mgt;
    int vlan;
    int type;

    public CiscoVlanData() {
    }

    public int getVlan() {
        return vlan;
    }

    public void setVlan(int vlan) {
        this.vlan = vlan;
    }

    public int getMgt() {
        return mgt;
    }

    public void setMgt(int mgt) {
        this.mgt = mgt;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
