package com.wbc.utilserver.model.switchprobe;

import com.wbc.intravue.PortInfo;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jim on 11/26/2019.
 */
public class SwitchprobeData {

    private boolean isComplete;
    private boolean hasPortInfo;
    private boolean isCiscoVlan=false;
    private String version;
    private int versionMajor;
    private int versionMinor;
    private String switchIP;
    private String community;
    private String systemName;
    private String location;
    private String description;
    private String defaultGateway;
    private boolean isRouter;
    private String ifToGateway;
    private int numIPs;
    private int numIfs;
    private int numPorts;
    private int numVlans;
    private int numKnownArps;
    private int numKnownMacs;
    private int numPortForMac;
    private int filesize;
    private int numArps;

    private String returnError;

    private HashMap<String, IpData> ipMap;
    private HashMap<Integer , InterfaceData> ifMap;
    private HashMap<String, ArpData> arpIpMap;
    private HashMap<String, ArpData> arpMacMap;
    private HashMap<Integer , PortData> portMap;
    private HashMap<Integer , CiscoVlanData> vlanMap;
    private HashMap<String ,MacInfo> macPortMap;

//    public HashMap< String, IpInterface > interfaceHashMap;
    private ArrayList<String> warningMessages;
    private ArrayList<String> errorMessages;


    public SwitchprobeData() {
        ipMap = new HashMap<String, IpData>();
        ifMap = new HashMap<Integer, InterfaceData>();
        arpIpMap = new HashMap<String, ArpData>();
        arpMacMap = new HashMap<String, ArpData>();
        portMap = new HashMap<Integer, PortData>();
        vlanMap = new HashMap<Integer , CiscoVlanData>();
        macPortMap = new HashMap<String ,MacInfo>();
        isComplete=false;
        hasPortInfo=false;
        warningMessages = new ArrayList<String>();
        errorMessages = new ArrayList<String>();
    }

    public int getNumArps() {
        return numArps;
    }

    public void setNumArps(int numArps) {
        this.numArps = numArps;
    }

    public String getSwitchIP() {
        return switchIP;
    }

    public void setSwitchIP(String switchIP) {
        this.switchIP = switchIP;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDefaultGateway() {
        return defaultGateway;
    }

    public void setDefaultGateway(String defaultGateway) {
        this.defaultGateway = defaultGateway;
    }

    public boolean isRouter() {
        return isRouter;
    }

    public void setRouter(boolean router) {
        isRouter = router;
    }

    public String getIfToGateway() {
        return ifToGateway;
    }

    public void setIfToGateway(String ifToGateway) {
        this.ifToGateway = ifToGateway;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getNumIPs() {
        return numIPs;
    }

    public void setNumIPs(int numIPs) {
        this.numIPs = numIPs;
    }

    public int getNumIfs() {
        return numIfs;
    }

    public void setNumIfs(int numIfs) {
        this.numIfs = numIfs;
    }

    public int getNumPorts() {
        return numPorts;
    }

    public void setNumPorts(int numPorts) {
        this.numPorts = numPorts;
    }

    public int getNumVlans() {
        return numVlans;
    }

    public void setNumVlans(int numVlans) {
        this.numVlans = numVlans;
    }

    public int getNumKnownArps() {
        return numKnownArps;
    }

    public void setNumKnownArps(int numKnownArps) {
        this.numKnownArps = numKnownArps;
    }

    public int getNumKnownMacs() {
        return numKnownMacs;
    }

    public void setNumKnownMacs(int numKnownMacs) {
        this.numKnownMacs = numKnownMacs;
    }

    public int getVersionMajor() {
        return versionMajor;
    }

    public void setVersionMajor(int versionMajor) {
        this.versionMajor = versionMajor;
    }

    public int getVersionMinor() {
        return versionMinor;
    }

    public void setVersionMinor(int versionMinor) {
        this.versionMinor = versionMinor;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public boolean isHasPortInfo() {
        return hasPortInfo;
    }

    public void setHasPortInfo(boolean hasPortInfo) {
        this.hasPortInfo = hasPortInfo;
    }

    public boolean isCiscoVlan() {
        return isCiscoVlan;
    }

    public void setCiscoVlan(boolean ciscoVlan) {
        isCiscoVlan = ciscoVlan;
    }

    public int getNumPortForMac() {
        return numPortForMac;
    }

    public void setNumPortForMac(int numPortForMac) {
        this.numPortForMac = numPortForMac;
    }

    public String getReturnError() {
        return returnError;
    }

    public void setReturnError(String returnError) {
        this.returnError = returnError;
    }

    public HashMap<String, IpData> getIpMap() {
        return ipMap;
    }

    public void setIpMap(HashMap<String, IpData> ipMap) {
        this.ipMap = ipMap;
    }

    public HashMap<Integer, InterfaceData> getIfMap() {
        return ifMap;
    }

    public void setIfMap(HashMap<Integer, InterfaceData> ifMap) {
        this.ifMap = ifMap;
    }

    public HashMap<String, ArpData> getArpIpMap() {
        return arpIpMap;
    }

    public void setArpIpMap(HashMap<String, ArpData> arpIpMap) {
        this.arpIpMap = arpIpMap;
    }

    public HashMap<String, ArpData> getArpMacMap() {
        return arpMacMap;
    }

    public void setArpMacMap(HashMap<String, ArpData> arpMacMap) {
        this.arpMacMap = arpMacMap;
    }

    public HashMap<Integer, PortData> getPortMap() {
        return portMap;
    }

    public void setPortMap(HashMap<Integer, PortData> portMap) {
        this.portMap = portMap;
    }

    public HashMap<Integer, CiscoVlanData> getVlanMap() {
        return vlanMap;
    }

    public void setVlanMap(HashMap<Integer, CiscoVlanData> vlanMap) {
        this.vlanMap = vlanMap;
    }

    public HashMap<String, MacInfo> getMacPortMap() {
        return macPortMap;
    }

    public void setMacPortMap(HashMap<String, MacInfo> macPortMap) {
        this.macPortMap = macPortMap;
    }

    public int getFilesize() {
        return filesize;
    }

    public void setFilesize(int filesize) {
        this.filesize = filesize;
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

    public String toSimpleString() {
        return "SwitchprobeData{" +
                "isComplete=" + isComplete +
                "\n" + " hasPortInfo=" + hasPortInfo +
                "\n" + " version='" + version + '\'' +
                "\n" + " switchIP='" + switchIP + '\'' +
                "\n" + " community='" + community + '\'' +
                "\n" + " systemName='" + systemName + '\'' +
                "\n" + " location='" + location + '\'' +
                "\n" + " description='" + description + '\'' +
                "\n" + " defaultGateway='" + defaultGateway + '\'' +
                "\n" + " isRouter=" + isRouter +
                "\n" + " ifToGateway='" + ifToGateway + '\'' +
                "\n" + " numIPs=" + numIPs +
                "\n" + " numIfs=" + numIfs +
                "\n" + " numPorts=" + numPorts +
                "\n" + " numVlans=" + numVlans +
                "\n" + " numKnownArps=" + numKnownArps +
                /*
                "\n" + " numKnownMacs=" + numKnownMacs +
                 */
                "\n" + " numPortForMac=" + numPortForMac +
                '}';

        /*
                "\n" + " ipMap=" + ipMap +
                "\n" + " ifMap=" + ifMap +
                "\n" + " arpMap=" + arpMap +
                "\n" + " portMap=" + portMap +

         */


    }

    public String ipsToString() {
        StringBuilder sb = new StringBuilder();
        ipMap.forEach((k,v)->{
            sb.append( "IP " + v.getIp() + "  " + v.getNetMask() +  " interface " + v.getInterfaceNum());
            sb.append("\n");

        });
        return sb.toString();
    }
}
