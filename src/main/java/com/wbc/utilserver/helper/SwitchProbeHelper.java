package com.wbc.utilserver.helper;

import com.google.gson.Gson;
import com.wbc.utilserver.Constants;
import com.wbc.utilserver.model.ErrorInfo;
import com.wbc.utilserver.model.IntravueDeviceInfo;
import com.wbc.utilserver.model.SwtichprobeReportInfo;
import com.wbc.utilserver.model.switchprobe.*;
import com.wbc.mysql.MysqlConnection;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class SwitchProbeHelper
{
	private static final Logger logger = LogManager.getLogger( SwitchProbeHelper.class);

	public String getSwitchData(String reportName, String ivIP, boolean bGetFromSwitch, boolean overwrite )
	{
		SwtichprobeReportInfo reportInfo = new SwtichprobeReportInfo();
		ErrorInfo result = reportInfo.getErrorInfo();
		String base = System.getProperty("catalina.base");
		String response ;
		if ( bGetFromSwitch ) {
			response = getSwitchprobeFromSwitch( ivIP, reportName );  // reportname is the ip
			if (!response.startsWith("ok")) {
				result.setResult("error");
				result.setErrorText( response );
				Gson gson = new Gson();
				return( gson.toJson( reportInfo ));
			}
			// A file has now been saved to the intravue folder /intravue/switchprobes
			// Name will be "switchprobe_switchip_YYMMDD.txt"
		}
		if (bGetFromSwitch) {
			Date date = new Date();
			String pattern = "yyMMdd";
			SimpleDateFormat format = new SimpleDateFormat(pattern);
			reportName = "switchprobe_" + reportName + "_" + format.format(date);
			//TODO: Add path to saved file from switch, need base to work
		}
		//
		File fInFile = new File( reportName);
		if ( !fInFile.exists()) {
			result.setResult("error");
			result.setErrorText("The switchprobe file was not found.");
			Gson gson = new Gson();
			return( gson.toJson( reportInfo ));
		}
// file not open yet		reportInfo.getGeneralData().setFilesize( fInFile.length());

		//
//		MysqlConnection conn = new MysqlConnection( Constants.DATABASE, Constants.USER_NAME, Constants.PASSWORD);
		MysqlConnection conn = new MysqlConnection( Constants.DATABASE, Constants.USER_NAME, Constants.PASSWORD);
		try {
			if ( !conn.getConnection("127.0.0.1") ) {
				result.setResult("error");
				result.setErrorText( "Failed to get connection to database, aborting");
				Gson gson = new Gson();
				return( gson.toJson( reportInfo ));
			}
		} catch (SQLException e) {
			result.setResult("error");
			result.setErrorText( "Exception getting connection to database.  " + e.getMessage() );
			Gson gson = new Gson();
			e.printStackTrace();
			conn.close();
			return( gson.toJson( reportInfo ));
		}

		ParseSwitchprobeFile parser = new ParseSwitchprobeFile( conn, fInFile );
		String parseResult = parser.parse();
		if (!parseResult.equals("ok")) {
			result.setResult("error");
			result.setErrorText( parseResult );
			logger.error("SwitchProbeHelper: parse error > " + parseResult );
			reportInfo.getErrorMessages().add("Error from parsing: " + parseResult );
		}

		parser.spData.getErrorMessages().forEach(s -> {
			final boolean add = reportInfo.getErrorMessages().add(s);
			logger.debug("Error Message from parse > " + s );
		});
		parser.spData.getWarningMessages().forEach(s -> {
			final boolean add = reportInfo.getWarningMessages().add(s);
			logger.debug("Warning Message from parse > " + s );
		});

		if (parser.spData.isComplete()) {
			// get intravue info needed
			HashMap<String, IntravueDeviceInfo> ivMacDeviceMap = getIvDeviceInfo(result, conn);
			if (!result.isOK()) {
				Gson gson = new Gson();
				conn.close();
				return( gson.toJson( reportInfo ));
			}
			//----------------------------------------------------------------------------------------------------
			// Panel 1 - General Information
			//----------------------------------------------------------------------------------------------------
			getGeneralInfoFromData( reportInfo.getGeneralData(), parser.spData );
			//----------------------------------------------------------------------------------------------------
			// Panel 2 - IP Address Information
			//----------------------------------------------------------------------------------------------------
			ArrayList<IpData> sortedIps = reportInfo.getIpList();
			Map<String, IpData> resultMap1 = parser.spData.getIpMap().entrySet().stream()
					.sorted(Map.Entry.comparingByKey())
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
							(oldValue, newValue) -> oldValue, LinkedHashMap::new));
			resultMap1.forEach( (k,v)-> {
				sortedIps.add( v );
			});
			//----------------------------------------------------------------------------------------------------
			// Panel 3 - Interface Information
			//----------------------------------------------------------------------------------------------------
			ArrayList<InterfaceData> sortedIfs = reportInfo.getIfList();
			Map<Integer, InterfaceData> resultMap2 = parser.spData.getIfMap().entrySet().stream()
					.sorted(Map.Entry.comparingByKey())
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
							(oldValue, newValue) -> oldValue, LinkedHashMap::new));
			resultMap2.forEach( (k,v)-> {
				sortedIfs.add( v );
			});
			//----------------------------------------------------------------------------------------------------
			// Panel 4 - ARP information
			//----------------------------------------------------------------------------------------------------
			ArrayList<ArpDataExtended> arpSorted = reportInfo.getArpList();
			parser.spData.getArpIpMap().forEach((k,v) -> {
				ArpDataExtended extended = new ArpDataExtended();
				extended.setIp(v.getIp());
				extended.setMac(v.getMac());
				extended.setIfNum(v.getIfNum());
				extended.setKey(UUID.randomUUID() + ""); //Just to have some unique number which is required for the GXT grid.
				InterfaceData ifdata =  parser.spData.getIfMap().get( v.getIfNum());
				if (ifdata != null ) {
					if (!ifdata.getDescription().isEmpty()) {
						extended.setIfDescription(ifdata.getDescription());
					} else {
						extended.setIfDescription("unknown");
					}
				} else {
					extended.setIfDescription("Unknown");
				}
				if (!v.getMac().isEmpty()) {
					String vendormac = v.getMac().substring(0,6).toUpperCase();
					extended.setVendor(   getVendorNameFromVendorServer( vendormac));
				} else {
					extended.setVendor( "No MAC");
				}

				arpSorted.add( extended);
			});
			//----------------------------------------------------------------------------------------------------
			// Panel 5 - VLAN Information
			//----------------------------------------------------------------------------------------------------
			ArrayList<CiscoVlanData> sortedVlans = reportInfo.getVlanList();
			Map<Integer, CiscoVlanData> resultMap3 = parser.spData.getVlanMap().entrySet().stream()
					.sorted(Map.Entry.comparingByKey())
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
							(oldValue, newValue) -> oldValue, LinkedHashMap::new));
			resultMap3.forEach( (k,v)-> {
				sortedVlans.add( v );
			});
			//----------------------------------------------------------------------------------------------------
			// Panel 6 - Port Information
			//----------------------------------------------------------------------------------------------------
			ArrayList<PortDataExtended> sortedPorts = reportInfo.getPortList();
			parser.spData.getPortMap().forEach((k,v)->{
				int port = v.getPortNum();
				int iface = v.getPortIf();
				PortDataExtended ext = new PortDataExtended();
				ext.setPortNumber(port);

				InterfaceData ifdata = parser.spData.getIfMap().get(iface);
				ext.setDescription( ifdata.getDescription());
				ext.setSpeed( ifdata.getSpeedBps());
				ext.setType( ifdata.getType());
				sortedPorts.add( ext );
			});
			//----------------------------------------------------------------------------------------------------
			// Panel 7 - MAC Information
			//----------------------------------------------------------------------------------------------------
			// put Hashmap of macinfo into arraylist
			ArrayList<MacInfo> sortedMacs = reportInfo.getMacList();
			Map<String, MacInfo> resultMap = parser.spData.getMacPortMap().entrySet().stream()
					.sorted(Map.Entry.comparingByKey())
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
							(oldValue, newValue) -> oldValue, LinkedHashMap::new));
			for (Map.Entry<String, MacInfo> entry : resultMap.entrySet()) {
				String k = entry.getKey();
				MacInfo macInfo = entry.getValue();
				macInfo.setKey(UUID.randomUUID() + ""); //Just to have some unique number which is required for the GXT grid.
				// get port description
				PortData port = parser.spData.getPortMap().get(macInfo.getPort());
				String portdesc = "not defined";
				if (port != null) {
					int ifNum = port.getPortIf();
					InterfaceData ifdata = parser.spData.getIfMap().get(ifNum);
					if (ifdata != null) {
						portdesc = ifdata.getDescription();
					}
				}
				macInfo.setPortDescription(portdesc);
				// if the mac is known to intravue get the ip, name
				String ivMac = MacInfo.convertToSpaceMac( macInfo.getMac()).toUpperCase();
				if (!ivMac.isEmpty()) {
					String vendormac = macInfo.getMac().substring(0,6).toUpperCase();
					macInfo.setVendor(  getVendorNameFromVendorServer( vendormac));
					IntravueDeviceInfo deviceInfo = ivMacDeviceMap.get(ivMac);
					if (deviceInfo != null) {
						macInfo.setIp(deviceInfo.getIpaddress());
						macInfo.setDescid(deviceInfo.getDescid());
						macInfo.setName(deviceInfo.getDevicename());
					} else {
						if ( parser.spData.getArpMacMap().containsKey(macInfo.getMac())) {
							ArpData arpData = parser.spData.getArpMacMap().get( macInfo.getMac());
							macInfo.setIp( arpData.getIp() );
						}
					}
				} else {
					// the ip is not known to Intravue, get if from arp table if present
logger.warn("parsing bridge, ivMac is empty, this should not happen ????");

				}
				sortedMacs.add(macInfo);
			}
			//----------------------------------------------------------------------------------------------------
		} else {
			// handle incomplete data
			logger.error("Handling of incomplete parser data not implemented");
			reportInfo.getWarningMessages().add("Not all sections of the switchprobe report were completed.");
		}
		boolean test_showVendorArpNames = true;
		if ( test_showVendorArpNames) {
			int limit = 10;
			int line = 0;
			for (ArpDataExtended data : reportInfo.getArpList()) {
				logger.debug(String.format("TEST: vendor is %s for mac %s", data.getVendor(), data.getMac()));
				line++;
				if (line > limit) break;
			}
		}
		conn.close();
		Gson gson = new Gson();
		return( gson.toJson( reportInfo ));
	}

	private String getVendorNameFromVendorServer(String vendormac) {
		String vendorNameResponse = "no server";
		String target = "http://127.0.0.1:8765/wbcVendorService/getname?vendor=" + vendormac ;
		URL url = null;
		try {
			url = new URL( target);
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setRequestMethod("GET");
			httpConn.setRequestProperty("Accept", "application/json");
			int responseCode = httpConn.getResponseCode();
			if ( responseCode == 200) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						(httpConn.getInputStream())));
				Gson gson = new Gson();
				ErrorInfo vendorResult = new ErrorInfo();
				vendorResult = gson.fromJson( br, ErrorInfo.class) ;
				if (vendorResult.isOK()) {
					vendorNameResponse = vendorResult.getErrorText();
				} else {
					vendorNameResponse =  vendorResult.getResult() + " - " + vendorResult.getErrorText();
				}
			} else {
				// unexcpected query code
				// is it authentication ?
				if (responseCode == 401) {  // unauthorized
					logger.error("Unauthroized response from " + target );
				} else {

				}
				logger.error("Failed to get connection to vendor name server,  Code " + responseCode + " > " + target );
				vendorNameResponse = "-";
			}
		} catch (MalformedURLException | ProtocolException e ) {
			vendorNameResponse = e.getClass() + " " + e.getMessage();
			logger.error( vendorNameResponse );
			e.printStackTrace();
		} catch (IOException e) {
			vendorNameResponse = e.getClass() + " " + e.getMessage();
			logger.error( vendorNameResponse );
			e.printStackTrace();
		}
		return vendorNameResponse;
	}

	private void getGeneralInfoFromData( GeneralInfo info, SwitchprobeData spData) {
		info.setCommunityUsed( spData.getCommunity());
		info.setComplete(spData.isComplete());
		info.setDefaultGateway( spData.getDefaultGateway());
		info.setDescription(spData.getDescription());
		if (spData.getNumIfs() > 0) info.setHasInterfaces(true);
		if (spData.getMacPortMap().size() > 0) {
			info.setHasMacTable(true);
		}
		info.setLocation( spData.getLocation());
		info.setNumArps(spData.getArpIpMap().size());
		info.setNumIfs(spData.getNumIfs());
		info.setNumIPs(spData.getNumIPs());
		info.setNumPortForMac(spData.getMacPortMap().size());
		info.setNumVLANs(spData.getNumVlans());
		info.setProbeVersion(spData.getVersion());
		info.setSwitchIp(spData.getSwitchIP());
		info.setSystemName(spData.getSystemName());
		info.setNumPorts(spData.getNumPorts());

	}

	private HashMap<String, IntravueDeviceInfo> getIvDeviceInfo( ErrorInfo errorInfo, MysqlConnection ivConn ) {
		HashMap<String, IntravueDeviceInfo> devicemap = new HashMap<String, IntravueDeviceInfo>();
		try {
			// get the ip's and macids we need.
			Statement stmt = ivConn.createStatement();
			StringBuilder sb = new StringBuilder();
			sb.append("select i.DescId, d.Parent, i.IpAddress, i.macaddress, dd.name ");
			sb.append("from ifdesc as i, desclink as d, devicedesc as dd ");
			sb.append("where i.descid=d.Child ");
			sb.append("and i.IfWasPinged=1 ");
			sb.append("and dd.descid=d.parent");
			//
			ResultSet rs = stmt.executeQuery(sb.toString());
			while (rs.next()) {
				IntravueDeviceInfo info = new IntravueDeviceInfo();
				info.setIpaddress( rs.getString(3) );
				info.setDescid(rs.getInt(2));
				info.setMacaddress(rs.getString(4));
				info.setDevicename(rs.getString(5));
				devicemap.put(info.getMacaddress(), info);
			}
			rs.close();
			stmt.close();
		} catch (Exception ex) {
			errorInfo.setResult("error");
			errorInfo.setErrorText( String.format( "getDisconnected Exception " + ex.toString() ));
			logger.error(errorInfo.getErrorText());
		}
		return devicemap;
	}



	//TODO implement getSwitchprobeFromSwitch
	private String getSwitchprobeFromSwitch(String ivIP, String reportName) {
		return "not implemented";
	}

}
