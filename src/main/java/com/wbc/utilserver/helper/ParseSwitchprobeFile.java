package com.wbc.utilserver.helper;

import com.wbc.utilserver.model.switchprobe.*;
import com.wbc.mysql.MysqlConnection;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ParseSwitchprobeFile {

    private MysqlConnection conn;
    private File inFile;
    public SwitchprobeData spData;


    private static final String CISCO_BEFORE_PORTS_FOUND = "2";
    private static final String NO_PORTS_FOUND = "-2";
    private static final String UNEXPECTED = "-3";

    private static final String END_CISCO_BRIDGE_VLAN = "1";
    private static final String NO_CISCO_BRIDGE_VLAN = "2";
    private boolean gDebugGetNextLine = false;

    private static final Logger logger = LogManager.getLogger( SwitchProbeHelper.class);

    public ParseSwitchprobeFile(MysqlConnection conn, File inFile ) {
        this.conn = conn;
        this.inFile = inFile;
        spData = new SwitchprobeData();
    }

    /*
    Order of items in the switch probe
    version
    ip address
    community

    System Name
    Location
    Description
    Default Gateway
    Switch or Router
    IF gateway
    local ip addresses


     */

    public String parse() {
        Scanner scanner = null;
        String rv;
        try {
            scanner = new Scanner( inFile );
            rv = getFirstLines( scanner );
            if (!rv.isEmpty()) {
                spData.setReturnError(rv);
                return rv;
            }
            rv = getIpsAndInterfacesLines( scanner );
            if (!rv.equals("ok"))  {
                spData.setReturnError(rv);
                return rv;
            }
            spData.setNumIfs( spData.getIpMap().size());
            rv = getArpLines( scanner );
            if (!rv.equals("ok") && (!rv.equals("done")))  {
                spData.setReturnError(rv);
                return rv;
            }
            spData.setNumArps( spData.getArpMacMap().size());
            // is there bridge data
            rv = getPortLines( scanner );
            if (rv.equals( UNEXPECTED )) {
                logger.debug("Unexpected line when getting ports, abort");
                spData.setReturnError(rv);
                return rv;
            } else if (rv.equals( NO_PORTS_FOUND )) {
                logger.debug("getPortLines returned NO_PORTS_FOUND");
            } else if (rv.equals( CISCO_BEFORE_PORTS_FOUND )) {
                logger.debug("getPortLines returned CISCO_BEFORE_PORTS_FOUND");
            } else {
                logger.debug("return from getPortLines, handle ? > " + rv );
            }
            if ( !spData.getPortMap().isEmpty()) {
                spData.setHasPortInfo( true );
            } else {
                String msg = "There are no ports found, switch will not be considered a switch if the port does not exist that contains the mac of the top parent" ;
                logger.warn(msg);
                spData.getWarningMessages().add(msg);
            }

            spData.setNumIfs( spData.getIfMap().size());
            spData.setNumPorts( spData.getPortMap().size());
            if (rv.equals(CISCO_BEFORE_PORTS_FOUND)) {
                logger.debug("Next, handle cisco vlans");
                rv = getCiscoVlans( scanner );
                /*
                rv will either be "ok" or "No cisco vlan", no real errors
                 */
                spData.setNumVlans( spData.getVlanMap().size());
                if (spData.getVlanMap().size() > 0)  spData.setCiscoVlan( true );

                // handle return
            }
            // next should be the bridge mib
            // may or may not be a Cisco switch, may or may not have skipeed some previous sections
            gDebugGetNextLine = false;
            rv = getBridgeData( scanner );
            gDebugGetNextLine = false;
            if (!rv.equals("ok")) {
                spData.setReturnError( rv );
                return rv;
            }
            spData.setNumPortForMac( spData.getMacPortMap().size());


            logger.debug("TBD:  Handle BridgeMIB Aging Time ");
            logger.debug("TBD:  Handle Q-Mib ");
            logger.debug("TBD:  Handle LLDP Local");
            logger.debug("TBD:  Handle LLDP Remote");
            logger.debug("TBD:  Handle IfConnectorPresent");
            logger.debug("TBD:  Handle RMON");
            logger.debug("TBD:  Handle Wireless AP");

            spData.setComplete(true);

            logger.debug(  spData.toSimpleString()  );
            // logger.debug( spData.ipsToString()) ;
        }
        catch( FileNotFoundException fnf )  {
            rv = String.format("ParseScannerLogfile: FileNotFoundException %s  %s", inFile.toString(), fnf.getMessage() );
            logger.error(rv);
            return rv;
        }
        catch( Exception e1 )       {
            e1.printStackTrace();
            rv = String.format("ParseScannerLogfile: Exception %s  %s", inFile.toString(), e1.getMessage() );
            logger.error(rv);
            return rv;
        }
        finally {
            //ensure the underlying stream is always closed
            scanner.close();
        }
        return "ok";
    }

    private String getBridgeData(Scanner scanner) {
        String error;
        String line = scanner.nextLine();
        if (spData.isCiscoVlan()) {
            /*
            Lines to process here must be one of 3
                listing all MAC as target known on VLAN 1
                Start of Bridge
                End of Bridge
             */
            while (scanner.hasNext()) {
                line = scanner.nextLine();  //listing all MAC as target known on VLAN 1
                if (line.isEmpty()) {
                    continue;
                }
                if (line.startsWith("listing all MAC")) {
                    int vlanNum = 0;
                    int index = line.indexOf("VLAN");
                    vlanNum =  Integer.parseInt(line.substring(index + 5));
                    String rv = getCiscoBridgeForVlan( scanner, vlanNum) ;
                    if (rv.startsWith("Error")) {
                        logger.debug(rv);
                        return rv;
                    }
                    // ignore all other returns

                }
                // we may be done
                else {
                    break;  // ????
                }

            }
        } else {
            // NOT Cisco
            int vlanNum = 0;  // no vlans info, use 0
            String test;
            test = "listing all MAC";
            line = getNextString( scanner, test, 5);
            if (line.isEmpty()) {
                error = "Failed to find listing all macs" + test ;
                logger.debug(error);
                return error;
            }
            while (scanner.hasNext()) {
                line=scanner.nextLine();  //Start
                if (false) logger.debug("getBridgeData: DEBUG line= " + line );
                if (line.isEmpty()) {
                    // continue;
                } else if (line.startsWith("requesting (getnext)")) {
                    return "ok";
                } else if (line.startsWith("End")) {
                    return "ok";
                } else if (line.startsWith("1.3.6.1.2.1.17.4.3.1.1")) {
                    getPortForMacData(scanner, line, vlanNum);
                }
            }
        }
        return "ok";
    }

    /*
    Gets all bridge data with a vlan number
     */
    private String getCiscoBridgeForVlan ( Scanner scanner, int vlanNum ) {
        //String line=scanner.nextLine();  //Start
        while (scanner.hasNext()) {
            String line=scanner.nextLine();  //Start
            /*
            NOTE:  Cisco will have many Start/End of Bridge sections
             */
            if (line.isEmpty()) {
                // continue;
            } else if (line.startsWith("End Of Bri")) {
                return END_CISCO_BRIDGE_VLAN;
            } else if (line.startsWith("End")) {
                return END_CISCO_BRIDGE_VLAN;    // exasperation, failing above when string clearly is the End of Bridge ....
            } else if (line.startsWith("Start of Bridge")) {
                // normal
                line = scanner.nextLine();  // drop first blank line
                if (line.startsWith("End of Bridge")) {
                    // this vlan is empty
                    return END_CISCO_BRIDGE_VLAN;
                }
                // 1/11/20 this looks like it is to long/specific, entering mac area String test = "1.3.6.1.2.1.17.4.3.1.1.0.0";
                String test = "1.3.6.1.2.1.17.4.3.1";
                line = getNextString( scanner, test, 5);
                if (line.isEmpty()) {
                    String error = "Error at getCiscoBridgeForVlan: Failed to find port for vlan " + vlanNum + "  "  + test ;
                    logger.debug( error );
                    return error;
                }
                else {
                    String error = getPortForMacData(scanner, line, vlanNum);
                    if ( !error.equals("ok")) {
                        logger.debug(error);
                        return error;
                    }
                }
            } else {
                // this must be the next port for mac
                line = scanner.nextLine();
                String error = getPortForMacData(scanner, line, vlanNum);  // we are already on line to process
                if ( !error.equals("ok")) {
                    logger.debug(error);
                    return error;
                }
                //logger.debug("getCiscoBridgeForVlan: line not processed > " + line );
            }
        }
        return "ok";
    }

    /*
    Gets port for mac lines, Cisco or anything else
     */
    private String getPortForMacData (Scanner scanner, String line, int vlanNum ) {
        String error = "ok";
        MacInfo info = new MacInfo();
        info.setVlanNum(vlanNum);
        int index = line.indexOf("HEX:");
        info.setMac( line.substring( index + 4));

        String test = "1.3.6.1.2.1.17.4.3.1.2";
        line = getNextString( scanner, test, 5);
        if (line.isEmpty()) {
            error = ("getPortForMacData: Failed to find port for mac=" + info.toString() + "  test " + test );
            logger.debug(error);
            return error;
        }
        else {
            index = line.indexOf("]");
            info.setPort( Integer.parseInt( line.substring( index +2 )));
        }
        spData.getMacPortMap().put( info.getMac() + "-" + vlanNum, info);
        return error;
    }

    private String  getCiscoVlans(Scanner scanner) {
        String result;
        while ( scanner.hasNext()) {
            String line = scanner.nextLine();
            if (line.startsWith("found target")) {
                // start
                CiscoVlanData vlan = new CiscoVlanData();
                line = scanner.nextLine();  // mgt.vlan 1.1 is type 1
                int index1 = line.indexOf(".", 5);
                String num = line.substring(9,index1);
                vlan.setMgt( Integer.parseInt(num));
                int index2 = line.indexOf(" ", index1);
                num = line.substring( index1+1, index2);
                vlan.setVlan( Integer.parseInt(num));
                index1 = line.indexOf("type");
                vlan.setType( Integer.parseInt( line.substring( index1 +5 )));
                //
                spData.getVlanMap().put( new Integer(vlan.getVlan()), vlan);
            }
            else if (line.startsWith("no CISCO")) {
                return NO_CISCO_BRIDGE_VLAN;
            }
            else if (line.contains("VLANs:")) {
                return "ok";  // end of VLANS
            }
            else if (line.startsWith("switch reports")) {
                continue; // normal second line
            }
            else {
                // lines a sequential and we skipped one, must be end of list  should be like "found 12 vlans"
                break;
            }
        }
        return "ok";
    }

    private String getPortLines(Scanner scanner) {
        // there may or may not be port lines
        // return success or what the neet section is
        // part lines until the first valid 'found port' line and then pass line to parsing method
        boolean portsFound = false;
        String error;
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            if (line.startsWith("found port")) {
                // start
                portsFound = true;
                processPorts( scanner, line );
            }
            else if (line.isEmpty()) {
                continue;
            }
            else if (line.startsWith("checking if")) {
                // this is past ports, cisco vlan starting.
                if (!portsFound) {
                    logger.debug("WARNING: Cisco VLAN list found without finding any ports.  ACL may need to be changed.");
                }
                return CISCO_BEFORE_PORTS_FOUND;
            }
            else if (line.startsWith("unit does not")) {
                // this is past ports, LLDP next
                return NO_PORTS_FOUND;
            }
            else if (line.startsWith("listing all switch ports")) {
                // normal start
                continue;
            }
            else if (line.startsWith("looking for")) {
                // normal start
                continue;
            }
            else if ((line.startsWith("listing")) || (line.startsWith("start"))) {
                // UNEXPECTING END
                logger.debug("Unexpected line, start of new section, when parsing ports > "  + line );
                return UNEXPECTED;
            } else {
                if (false) logger.debug("Unhandled line when parsing ports > "  + line );
            }
        }
        return "ok";
    }

    private String processPorts(Scanner scanner, String line) {
        String error="ok";
        // the first port is passed in.
        PortData portInfo = new PortData();
        portInfo.setPortNum(Integer.parseInt(line.substring(12)));
        String test;
        test = "1.3.6.1.2.1.17.1.4.1.2";
        line = getNextString( scanner, test, 5);
        if (line.isEmpty()) {
            error ="Failed to find if for port" + test ;
            logger.error(error);
            return error;
        } else {
            int index = line.indexOf("]");
            portInfo.setPortIf(Integer.parseInt(line.substring(index+2)));
        }
        spData.getPortMap().put( new Integer(portInfo.getPortNum()), portInfo);
        return error;
    }


    private String getArpLines(Scanner scanner) {
        String error;
        String test = "listing ARP";
        String line = getNextString( scanner, test, 10);
        if (line.isEmpty()) {
            error = "Failed to find ARP data" + test ;
            logger.debug( error );
            return error;
        }
        if ( scanner.hasNext()) {
            String rv = processARP( scanner );
            if (!rv.equals("ok") || !rv.equals("done")) {
                return rv;
            }
        }
        return "ok";
    }

        /*
    -------------------------------------------------
    end of ARP table

    looking for managed switch data
    listing all switch ports

    found port: 26
    -------------------------------------------------
    end of ARP table

    looking for managed switch data
    listing all switch ports
    checking if it has a CISCO VLAN list
    switch reports a CISCO VLAN list as follows
    -------------------------------------------------
    end of ARP table

    unit does not report as a managed switch

    Start of LLDP Local System Data
    -------------------------------------------------

     */

    private String processARP(Scanner scanner) {
        String line;
        String error;
        int tries = 10000;
        int lineno=0;
        while (scanner.hasNext()) {
            line = scanner.nextLine();
            if (line.startsWith("found target")) {
                ArpData arpInfo = new ArpData();
                int index1 = line.indexOf(".");
                arpInfo.setIfNum(Integer.parseInt(line.substring(27,index1)));
                arpInfo.setIp( line.substring(index1+1));
                String test = "1.3.6.1.2.1.4.22.1.2";
                line = getNextString( scanner, test, 5);
                if (line.isEmpty()) {
                    error = "processARP: Failed to find arp data" + test ;
                    logger.debug( error);
                    return error;
                } else {
                    arpInfo.setMac( line.substring( line.indexOf("HEX") + 4));
                    spData.getArpIpMap().put(arpInfo.getIp(), arpInfo);
                    spData.getArpMacMap().put(arpInfo.getMac(), arpInfo);
                }
            } else if (line.startsWith("end of ARP")){
                // we are done
                return "done";
            } else {
                if (lineno > tries) {
                    error = "processARP: exceeded num tries getting arps = " + tries;
                    logger.debug(error);
                    return error;
                }
            }
            lineno++;
        }
        return "ok";
    }

    private String getIpsAndInterfacesLines(Scanner scanner) {
        String error;
        String test = "listing all local IP";
        String line = getNextString( scanner, test, 10);
        if (line.isEmpty()) {
            error = "Failed to find local IPs " + test;
            logger.warn(error);
            return error;
        }
        // --------------------------------- Process IPs ----------------------------------
        while ( scanner.hasNext()) {
            String rv = processIP( scanner );  // pass the line we are on
            if (rv.equals("ok") ) {
                continue;
            } else if ( rv.equals("done")) {
                break;
            }
            else if (!rv.isEmpty()) {
                error = "ProcessIP unexpected result. "  + rv;
                logger.warn(error);
                return error;
            }
        }
        // set num ips
        spData.setNumIPs( spData.getIpMap().size());
        test = "1.3.6.1.2.1.2.1.0";
        line = getNextString( scanner, test, 5);
        if (line.isEmpty()) {
            error = "getIpsAndInterfacesLines failed to find number of interfaces " + test;
            logger.debug(error);
            return error;
        } else {
            test = line.substring(29);
            int value = Integer.parseInt(test);
            spData.setNumIfs( value );
        }
        // --------------------------------- Process Interfaces ----------------------------------
        while ( scanner.hasNext()) {
            String rv = processInterface( scanner );
            if (rv.equals("ok"))  {
                continue;
            } else if ( rv.equals("done")) {
                break;
            }
            else {
                return rv;
            }
        }
        return "ok";
    }

    private String processInterface(Scanner scanner) {
        String error;
        InterfaceData ifInfo = new InterfaceData();
        int tries = 5000;
        int lineno=0;
        String line;
        while (scanner.hasNext()) {
            line = scanner.nextLine();
            if (line.startsWith("found interface")) {
                ifInfo.setIfID(Integer.parseInt(line.substring(17)));
                break;
            } else if (line.startsWith("end of list")){
                // we are done
                return "done";
            } else {
                if (lineno > tries) {
                    error = "processInterface: exceeded artificial num tries getting IFs = " + tries;
                    logger.debug(error);
                    return error;
                }
            }
            lineno++;
        }
        String test;
        /*  Don't use num interfaces, found interface is past this point
        test = "1.3.6.1.2.1.2.1.0";
        line = getNextString( scanner, test, 10);
        if (line.isEmpty()) {
            logger.warn("Failed to find " + test );
            return -1;
        } else {
            test = line.substring(29);
            int value = Integer.parseInt(test);
            spData.setNumIfs( value );
        }
        */

        if ( spData.getVersionMajor() == 2 ) {
            // get IF description
            test="1.3.6.1.2.1.2.2.1.2";
            line = getNextString( scanner, test, 10);
            if (line.isEmpty()) {
                ifInfo.setDescription( "Empty");
            } else {
                int index = line.indexOf("]");
                ifInfo.setDescription( line.substring(index+2));
            }
        }

        test="1.3.6.1.2.1.2.2.1.3";
        line = getNextString( scanner, test, 10);
        if (line.isEmpty()) {
            error = "Failed to find type " + test;
            logger.warn(error);
            return error;
        } else {
            int index = line.indexOf("]");
            ifInfo.setType( Integer.parseInt(line.substring(index+2)));
        }
        test="1.3.6.1.2.1.2.2.1.5";
        line = getNextString( scanner, test, 10);
        if (line.isEmpty()) {
            error = "Failed to find speed " + test;
            logger.warn(error);
            return error;
        } else {
            int index = line.indexOf("]");
            ifInfo.setSpeedBps( Long.parseLong(line.substring(index+2)));
        }

        /*
        mac line may be line
        1.3.6.1.2.1.2.2.1.6.1: [OctetString] HEX:80e86feb8700
        or
        1.3.6.1.2.1.2.2.1.6.2: [OctetString] ""
         */
        test="1.3.6.1.2.1.2.2.1.6";
        line = getNextString( scanner, test, 10);
        if (line.isEmpty()) {
            error = "Failed to find mac " + test;
            logger.warn(error);
            return error;
        } else {
            int index = line.indexOf("HEX");
            if ( index > 0) {
                ifInfo.setMac( line.substring( index+4));
            }
        }
        /*
        Look for new SNMP data,
         */
        test="1.3.6.1.2.1.31.1.1.1.15";
        line = getNextString( scanner, test, 18);
        if (line.isEmpty()) {
            // "Failed to find HI Speed mps, ok not mandatory " ;
        } else {
            int index = line.indexOf("Gau");
            if ( index > 0) {
                ifInfo.setSpeedMps( Long.parseLong(line.substring(index+9)));
            }
        }
        spData.getIfMap().put( new Integer(ifInfo.getIfID()), ifInfo);
        return "ok";
    }

    private String processIP(Scanner scanner ) {
        String error;
        IpData ipInfo = new IpData();
        String line;
        while (scanner.hasNext()) {
            line = scanner.nextLine();
            if (line.startsWith("found local")) {
                ipInfo.setIp(line.substring(24));
                break;
            } else if ( line.startsWith("end of list") ) {
                return "done";
            } else {
                if (!line.startsWith("[response time")) {
                    logger.warn("1 Unexpected line in processIP > " + line);
                    //return -1;   ?? should we set a flag 'foundLocal' or just keep going?
                }
            }
        }
        String test;
        test = "1.3.6.1.2.1.4.20.1.2";
        line = getNextString( scanner, test, 5);
        if (line.isEmpty()) {
            error = "Failed to find interface number " + test;
            logger.error(error);
            return error;
        } else {
            int index = line.indexOf("]");
            ipInfo.setInterfaceNum( Integer.parseInt(line.substring(index+2)));
        }
        test = "1.3.6.1.2.1.4.20.1.3";
        line = getNextString( scanner, test, 5);
        if (line.isEmpty()) {
            error = "Failed to find netmask " + test;
            logger.error(error);
            return error;
        } else {
            int index = line.indexOf("]");
            ipInfo.setNetMask( line.substring(index+2));
        }
        spData.getIpMap().put(ipInfo.getIp(), ipInfo);
        return "ok";
    }


    @SuppressWarnings("DuplicatedCode")
    private String getFirstLines(Scanner scanner) {
        String error;
        String line;
        StringBuilder sb = new StringBuilder();
        boolean found=false;
        int lineno = 1;
        String test;
        test = "Intravue SNMP";
        line = getNextString( scanner, test, 5);
        if (line.isEmpty()) {
            logger.warn("Failed to find " + test );
            error = "Switchprobe Version not found, may not be good file.  Aborting.";
            logger.warn(error);
            return error;
        } else {
            int index = line.indexOf("version");
            spData.setVersion(line.substring(index));
            // now set major and minor    >> Intravue SNMP Query Test Report utility version V2.09 1/11/19
            index = line.indexOf("V");
            int index2 = line.indexOf(".");
            int index3 = line.indexOf(" ", index2);
            spData.setVersionMajor( Integer.parseInt(  line.substring(index+1, index2)));
            spData.setVersionMinor( Integer.parseInt(  line.substring(index2+1, index3)));
        }
        test = "IP Adrs:";
        line = getNextString( scanner, test, 5);
        if (line.isEmpty()) {
            error = "Failed to find " + test;
            logger.warn(error);
            return error;
        } else {
            spData.setSwitchIP( line.substring(9));
        }
        test = "SNMP comm";
        line = getNextString( scanner, test, 5);
        if (line.isEmpty()) {
            error = "Failed to find " + test;
            logger.warn(error);
            return error;
        } else {
            spData.setCommunity( line.substring(16));
        }
        test = "1.3.6.1.2.1.1.5.0";
        line = getNextString( scanner, test, 5);
        if (line.isEmpty()) {
            error = "Failed to find " + test;
            logger.warn(error);
            return error;
        } else {
            spData.setSystemName( line.substring(33));
        }
        // location
        line = getNextString(  scanner,"1.3.6.1.2.1.1.6.0", 5);
        if (line.isEmpty()) {
            error ="Failed to find location ";
            logger.warn(error);
            return error;
        } else {
            spData.setLocation( line.substring(33));
        }
        sb = new StringBuilder();
        found = false;
        while (scanner.hasNext() && !found) {
            line = scanner.nextLine();
            // description may have several lines
            if (line.startsWith("1.3.6.1.2.1.1.1.0")) {
                found = true;
                sb.append( line.substring(33));
                while ( scanner.hasNext()) {
                    line = scanner.nextLine();
                    if ( !line.isEmpty()) {
                        sb.append( " " + line );
                    } else {
                        break;
                    }
                }
                spData.setDescription( sb.toString());
            }
        }
        // gateway
        test = "1.3.6.1.2.1.4.21";
        line = getNextString( scanner, test, 10);
        if (line.isEmpty()) {
            error = "Failed to find gateway " + test ;
            logger.warn(error);
            return error;
        } else {
            String temp = line.substring(30);
            if (temp.startsWith("[No")) {
                temp = "not set";
            }
            spData.setDefaultGateway( temp );
        }
        // router
        test = "1.3.6.1.2.1.4.1.0";
        line = getNextString( scanner, test, 10);
        if (line.isEmpty()) {
            error = "Failed to find router " + test ;
            logger.warn(error);
            return error;
        } else {
            test = line.substring(29);
            if (test.equals("1")) {
                spData.setRouter(true);
            } else {
                spData.setRouter(false);
            }
        }
        // ifto gateway
        test = "1.3.6.1.2.1.4.21.1";
        line = getNextString( scanner, test, 10);
        if (line.isEmpty()) {
            error = "Failed to find interface to Gateway " + test ;
            logger.warn(error);
            return error;
        } else {
            String temp = line.substring(30);
            if (temp.startsWith("[No")) {
                temp = "not set";
            }
            spData.setIfToGateway( temp );
        }
        return "";
    }

    private String getNextString( Scanner scanner, String start, int maxLines ) {
        int tries = 0;
        while( scanner.hasNext()) {
            String line = scanner.nextLine();
            if (gDebugGetNextLine == true) {
                logger.debug( "getNextString Debug > " + line );
            }
            if (line.startsWith(start)) {
                return line;
            } else {
                if (line.startsWith("found")) {
                    // we have gone past boundry, we are never expecting found while looking for an oid
                    // return now so we don't go past (too far) the line
                    return "";
                }
            }
            tries++;
            if (tries > maxLines) return "";
        }
        return "";
    }

}
