package com.wbc.utilserver.model;

import com.wbc.utilserver.model.switchprobe.*;

import java.util.ArrayList;

public class SwtichprobeReportInfo {

    private ErrorInfo errorInfo;
    private GeneralInfo generalData;
    private ArrayList<String> warningMessages;
    private ArrayList<String> errorMessages;
    private ArrayList<MacInfo> macList;
    private ArrayList<ArpDataExtended> arpList;
    private ArrayList<IpData> ipList;
    private ArrayList<PortDataExtended> portList;
    private ArrayList<CiscoVlanData> vlanList;
    private ArrayList<InterfaceData> ifList;


    public SwtichprobeReportInfo() {
        generalData = new GeneralInfo();
        errorInfo = new ErrorInfo();
        warningMessages = new ArrayList<String>();
        errorMessages = new ArrayList<String>();
        macList = new ArrayList<MacInfo>();
        arpList = new ArrayList<ArpDataExtended>();
        ipList = new ArrayList<IpData>();
        portList = new ArrayList<PortDataExtended>();
        vlanList = new ArrayList<CiscoVlanData>();
        ifList = new ArrayList<InterfaceData>();
    }

    public ArrayList<ArpDataExtended> getArpList() {
        return arpList;
    }

    public void setArpList(ArrayList<ArpDataExtended> arpList) {
        this.arpList = arpList;
    }

    public ArrayList<IpData> getIpList() {
        return ipList;
    }

    public void setIpList(ArrayList<IpData> ipList) {
        this.ipList = ipList;
    }

    public ArrayList<PortDataExtended> getPortList() {
        return portList;
    }

    public void setPortList(ArrayList<PortDataExtended> portList) {
        this.portList = portList;
    }

    public ArrayList<CiscoVlanData> getVlanList() {
        return vlanList;
    }

    public void setVlanList(ArrayList<CiscoVlanData> vlanList) {
        this.vlanList = vlanList;
    }

    public ArrayList<InterfaceData> getIfList() {
        return ifList;
    }

    public void setIfList(ArrayList<InterfaceData> ifList) {
        this.ifList = ifList;
    }

    public ArrayList<MacInfo> getMacList() {
        return macList;
    }

    public void setMacList(ArrayList<MacInfo> macList) {
        this.macList = macList;
    }

    public ErrorInfo getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(ErrorInfo errorInfo) {
        this.errorInfo = errorInfo;
    }

    public GeneralInfo getGeneralData() {
        return generalData;
    }

    public void setGeneralData(GeneralInfo generalData) {
        this.generalData = generalData;
    }

    public ArrayList<String> getWarningMessages() {
        return warningMessages;
    }

    public void setWarningMessages(ArrayList<String> warningMessages) {
        this.warningMessages = warningMessages;
    }

    public ArrayList<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(ArrayList<String> errorMessages) {
        this.errorMessages = errorMessages;
    }
}
