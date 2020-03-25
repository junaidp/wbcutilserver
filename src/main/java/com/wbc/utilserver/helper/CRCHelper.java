package com.wbc.utilserver.helper;

import com.google.gson.Gson;
import com.wbc.utilserver.Constants;
import com.wbc.utilserver.model.ErrorInfo;
import com.wbc.utilserver.model.SwitchErrorData;
import com.wbc.utilserver.model.SwitchErrorInfo;
//import com.wbc.utilserver.repository.CRCRepository;
import com.wbc.utilserver.utility.WbcUtilities;
import com.wbc.intravue.threshold.ThresholdUtil;
import com.wbc.utils.TimeUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;

@Component
public class CRCHelper
{

	@Autowired
	// private CRCRepository CrcRepository;
	private static final Logger logger = LogManager.getLogger( CRCHelper.class);
	private boolean debugData = false;

	public String getAllData( String period, String type, String ipAddress ) {
		SwitchErrorInfo info;

		boolean testing = false;
		if (type.equals("iferr")) type = "ifInErr";
		if (testing) {
			info = getDummyData(period, type);
		} else {
			info = getErrorData( ipAddress, period, type);
		}
		if (info.getErrorInfo().isOK()) {
			HashMap<String, SwitchErrorData> data = info.getErrorMap();
			if (data.size() == 0) {
				ErrorInfo error = new ErrorInfo();
				error.setResult("warning");
				error.setErrorText("No data found for this period");
				info.setErrorInfo(error);
			}
			//***********************************************************
			boolean showValues = true;
			if (showValues && debugData) {
				data.forEach((k, v) -> {
					StringBuilder sb = new StringBuilder();
					sb.append(k + "  ");
					ArrayList values = v.getValues();
					for (int i = 0; i < values.size(); i++) {
						sb.append(i + "-" + values.get(i) + ", ");
					}
					logger.debug(sb.toString());
				});
			}
			//***********************************************************
			// add the totals
			data.forEach((k, v) -> {
				ArrayList<Integer> values = v.getValues();
				int total = 0;
				for (int i = 0; i < values.size() - 1; i++) {
					total += values.get(i).intValue();
				}
				values.set(values.size() - 1, total);
				v.setValuesTotal(total);
			});
			//***********************************************************
			//
			ErrorInfo methodError = new ErrorInfo();
			ArrayList<Date> headerDates = getSwitchErrorsHeaderDateTimes(period, ipAddress, methodError);
			if (!methodError.isOK()) {
				info.getErrorInfo().setResult("warning");
				info.getErrorInfo().setErrorText(methodError.getErrorText());
			}
			info.setHeader(headerDates);
		}
		//
		Gson gson = new Gson();
		String results = gson.toJson( info );
		return results;
	}

/*
	public String getSwitchErrors(String period, String type) {
		HashMap<String,SwitchErrorData>  errorMap = getDummyData( period, type);

		Gson gson = new Gson();
		String errors = gson.toJson( errorMap );
		return errors;
	}
 */

	/*
	The dummy function will return port info for one port of two switches
	 */
	public SwitchErrorInfo getDummyData(String period, String type)
	{
		SwitchErrorInfo info = new SwitchErrorInfo();
		logger.info("Inside getDummyData");
		HashMap<String,SwitchErrorData> errorMap = info.getErrorMap();
		if (!type.equals("crc") && !type.equals("ifInErr")) {
			// invalid type, handle error
			ErrorInfo errorInfo = info.getErrorInfo();
			errorInfo.setResult("error");
			errorInfo.setResult("Invalid request type = " + type );
			return info;
		}
		SwitchErrorData switchPortErrors = new SwitchErrorData();
		switchPortErrors.setDescid(15);
		switchPortErrors.setIp("192.168.1.200");
		switchPortErrors.setPort("5");
		switchPortErrors.setPortDesc("FastEthernet 1/0/5");
		ArrayList<Integer> values = switchPortErrors.getValues();
		int numValues = 8;
		if (period.equals("24")) numValues=24;
		else if (period.equals("48")) numValues=48;
		else if (period.equals("72")) {
			for (int i=0; i<18; i++) {
				values.add(0);
			}
			errorMap.put( switchPortErrors.getIp() + "+" + switchPortErrors.getPortDesc(), switchPortErrors);
			return info;  // handle later
		}
		else if (period.equals("week")) {
			for (int i=0; i<7; i++) {
				values.add(0);
			}
			errorMap.put( switchPortErrors.getIp() + "+" + switchPortErrors.getPortDesc(), switchPortErrors);
			return info;  // handle later
		}
		for (int i=0, value=50; i<numValues; i++) {
			values.add(value+=50);
		}
		errorMap.put( switchPortErrors.getIp() + "+" + switchPortErrors.getPortDesc(), switchPortErrors);
		switchPortErrors = new SwitchErrorData();
		values = switchPortErrors.getValues();
		switchPortErrors.setDescid(583);
		switchPortErrors.setIp("192.168.2.101");
		switchPortErrors.setPort("24");
		switchPortErrors.setPortDesc("FastEthernet 2/1/6");
		// do not call getSwitchErrorsHeaderDateTimes( period, "127.0.0.1" );
		// removed switchPortErrors.setTimes( times );
		int max = 5000;
		int min = 50;
		for (int i=0; i<numValues; i++) {
			Random r = new Random();
			values.add( r.nextInt((max - min) + 1) + min );
		}
		errorMap.put( switchPortErrors.getIp() + "+" + switchPortErrors.getPortDesc(), switchPortErrors);
		return info;
	}


	private SwitchErrorInfo getErrorData( String ivHostIp, String period, String type ) {
		SwitchErrorInfo info = new SwitchErrorInfo();
		HashMap<String,SwitchErrorData> errorMap = info.getErrorMap();
		if (!type.equals("crc") && !type.equals("ifInErr")) {
			// invalid type, handle error
			ErrorInfo errorInfo = info.getErrorInfo();
			errorInfo.setResult("error");
			errorInfo.setErrorText("Invalid request type = " + type );
			return info;
		}
		int numValues = 8;
		int hoursPerPeriod=1;
		if (period.equals("24")) numValues=24;
		else if (period.equals("48")) numValues=48;
		else if (period.equals("72")) {
			hoursPerPeriod = 4;
			numValues = 18;
		}
		else if (period.equals("week")) {
			hoursPerPeriod =24;
			numValues = 7;
		}
		com.wbc.mysql.MysqlConnection conn = new com.wbc.mysql.MysqlConnection( Constants.DATABASE, ivHostIp,  Constants.USER_NAME, Constants.PASSWORD);
		try {
			if (!conn.isConnected()) {
				if (!conn.getConnection(ivHostIp)) {
					ErrorInfo errorInfo = info.getErrorInfo();
					errorInfo.setResult("error");
					errorInfo.setErrorText("Failed to get connection to database, " + ivHostIp + ",  aborting");
					return info;
				}
			}
			Timestamp lastPing = getLastPingTime( conn );
			// get ending hours from last ping
			int lastFullHour = lastPing.getHours();
			// need to know timezone of host if this is done on stored database
			LocalDateTime ldt = lastPing.toLocalDateTime();
			LocalDateTime lastDtHour = ldt.of(
					ldt.getYear(),
					ldt.getMonth(),
					ldt.getDayOfMonth(),
					ldt.getHour(),
					0);
			LocalDateTime startDt = lastDtHour.minusHours( (hoursPerPeriod  *   numValues) + 1) ;  // add 1 hour to get the first hour's data
			Timestamp start = Timestamp.valueOf( startDt);
			Timestamp  end = Timestamp.valueOf(lastDtHour);
			String description = "";
			try {
				// get the ip's and macids we need.
				Statement stmt = conn.createStatement();
				StringBuilder sb = new StringBuilder();

				sb.append("select descid, eventtime, ipaddress, description from event where class=114 ");
				sb.append("and (occurred >='" );
				sb.append( start );
				sb.append("' and occurred <'");
				sb.append( end );
				sb.append("') ");
				sb.append("order by descid, eventtime ");
				//
				if (debugData) logger.debug( "SQL command >> " + sb.toString());
				ResultSet rs = stmt.executeQuery( sb.toString() );
				while ( rs.next() )
				{
					int descid = rs.getInt(1);
					Timestamp ts = rs.getTimestamp(2);
					String ip = rs.getString(3);
					description = rs.getString(4);
					int rv = addDataToSwitchMap( errorMap, type, numValues, descid, ts, ip, description, lastDtHour,hoursPerPeriod);
					if (rv == -1) {
						ErrorInfo errorInfo = info.getErrorInfo();
						errorInfo.setResult("error");
						errorInfo.setErrorText("Error calculating dates.");
						logger.error(errorInfo.getErrorText());
						return info;
					}
				}
				rs.close();
				// add port numbers
				for (Map.Entry<String, SwitchErrorData> entry : errorMap.entrySet()) {
					String k = entry.getKey();
					SwitchErrorData v = entry.getValue();
					int descid = v.getDescid();
					String query = "select p.PortNo from desclink as d, portdesc as p where p.DescId=d.Parent and d.Child=" + descid;
					ResultSet rs2 = null;
					try {
						rs2 = stmt.executeQuery(query);
						while (rs2.next()) {
							int portno = rs2.getInt(1);
							v.setPort("" + portno);
						}
						rs2.close();
					} catch (SQLException e) {
						ErrorInfo errorInfo = info.getErrorInfo();
						errorInfo.setResult("error");
						errorInfo.setErrorText("CRCRepository:getErrorData SQL Exception. " + e.getMessage());
						e.printStackTrace();
						logger.error(errorInfo.getErrorText());
						return info;
					}
				}
				stmt.close();
			} catch ( SQLException e) {
				ErrorInfo errorInfo = info.getErrorInfo();
				errorInfo.setResult("error");
				errorInfo.setErrorText("CRCRepository:getErrorData SQL Exception. " + e.getMessage());
				logger.error(errorInfo.getErrorText());
				e.printStackTrace();
				return info;
			} catch (Exception ex) {
				ex.printStackTrace();
				ErrorInfo errorInfo = info.getErrorInfo();
				errorInfo.setResult("error");
				errorInfo.setErrorText("CRCRepository:getErrorData Exception " + ex.toString() + " for description " + description);
				logger.error(errorInfo.getErrorText());
				return info;
			}
		} catch ( SQLException e) {
			ErrorInfo errorInfo = info.getErrorInfo();
			errorInfo.setResult("error");
			errorInfo.setErrorText("CRCRepository:getErrorData SQL Exception. " + e.getMessage());
			logger.error(errorInfo.getErrorText());
			e.printStackTrace();
		}
		conn.close();
		return info;
	}

	private int addDataToSwitchMap(HashMap<String, SwitchErrorData> errorMap, String type, int numValues, int descid, Timestamp ts, String ip, String description, LocalDateTime lastPing, int hoursPerPeriod) {
		if (!description.startsWith( type )) {
			return 1;  // not the right type and not an error
		}
		// get portDesc
		int index1 = description.indexOf(":") + 2;
		int index2 = description.indexOf("has", index1) - 1;
		String portdesc = description.substring(index1, index2);
		String key = ip + "+" + portdesc;
		SwitchErrorData switchErrorData = new SwitchErrorData();
		//ArrayList<Integer> values;
		if (errorMap.containsKey(key)) {
			switchErrorData = errorMap.get(key);
		} else {
		    if (debugData) logger.debug("First time, init 0 values for " + ip + "  " + portdesc);
			ArrayList<Integer> values = new ArrayList<Integer>();
			for (int i = 0; i < numValues+1; i++) {   // +1 to add the total column
				values.add(i, 0);
			}
			switchErrorData.setDescid(descid);
			switchErrorData.setIp(ip);
			switchErrorData.setPortDesc(portdesc);
			switchErrorData.setPort("0");
			switchErrorData.setValues( values);
			errorMap.put(key, switchErrorData);
		}
		// which hour is this ?
		LocalDateTime ldt = ts.toLocalDateTime();
		long seconds = Duration.between(ldt, lastPing).getSeconds();
		int hoursAgo = (int) (seconds / 3600);

		// check to see how this effects times in the last hour or first hour,
		// should it be a float and drop decimal to round up / down

		/*
		if (debugX) {
			logger.log("hoursAgo " + hoursAgo + "  >  " + ldt.toString());
		}
		*/
		// most recent goes in last value
		if (hoursAgo > numValues  && hoursPerPeriod==1) {
			 logger.error("BAD hoursAgo > hours ");
			return -1;
		}
		if (description.contains("reset")) {
			if (hoursPerPeriod ==1) {
				switchErrorData.getValues().set(numValues - hoursAgo - 1, -1);  // -1 for reset, -1 for 0th box.
			} else {
				// there may be data in the multi-hour period, if there is make it -1
				int dataOffset = hoursAgo/hoursPerPeriod;
				int valueIndex = numValues - dataOffset -1 ;   // ?? debug if there should be a -1
				switchErrorData.getValues().set(valueIndex, -1);
				if (debugData) logger.debug( "RESET found for multi-hour data " + switchErrorData.getDescid() + ",  " + switchErrorData.getIp() + ", portdesc " + switchErrorData.getPortDesc() + "  dataOffset " + dataOffset  + ", setting period " + valueIndex +  ",  " + ldt.toString());

			}
		} else {
			index1 = description.indexOf("by") + 3;
			index2 = description.indexOf(" ", index1);
			int value = Integer.parseInt(description.substring(index1, index2));
			if (hoursPerPeriod ==1) {
				//  ?? on -1 switchErrorData.getValues().set(numValues - hoursAgo - 1, value);  // -1 for 0th box.
				switchErrorData.getValues().set(numValues - hoursAgo -1, value);  // -1 for 0th box.
			} else {
				double devisor = (double)hoursAgo / (double)hoursPerPeriod;
				int dataOffset = hoursAgo/hoursPerPeriod;
				// fails at 18 -18 -1 >>int valueIndex = numValues - dataOffset -1 ;   // ?? debug if there should be a -1
				int valueIndex = numValues - dataOffset -1 ;   // ?? debug if there should be a -1
                ArrayList<Integer> values = switchErrorData.getValues();
				int previous = values.get(valueIndex);
				if (debugData) logger.debug( "CALC " + switchErrorData.getDescid() + ",  " + switchErrorData.getIp() + ", portdesc " + switchErrorData.getPortDesc() + "  dataOffset " + dataOffset + "  previous " + previous + ", now " + value + ", setting period " + valueIndex +  ",  " + ldt.toString());
				values.set(valueIndex, previous + value);
                switchErrorData.setValues(values);
			}
		}
		return 0;
	}

	public ArrayList<Date>  getSwitchErrorsHeaderDateTimes(String period, String ivHostIp, ErrorInfo methodError) {
		boolean bDebug = true;
		ArrayList<Date> dateList = new ArrayList<Date>();
		int numValues = 8;
		int hoursPerPeriod=1;
		if (period.equals("24")) numValues=24;
		else if (period.equals("48")) numValues=48;
		else if (period.equals("72")) {
			// UI should check size of zdt list
			hoursPerPeriod = 4;
			numValues = 18;
		}
		else if (period.equals("week")) {
			hoursPerPeriod = 24;
			numValues = 7;
		}

		com.wbc.mysql.MysqlConnection conn = new com.wbc.mysql.MysqlConnection( Constants.DATABASE, ivHostIp,  Constants.USER_NAME, Constants.PASSWORD);
		try {
			Timestamp lastPing = getLastPingTime(conn);
			conn.close();
			String timezoneGMT = "";
			// ZoneId zid = ZoneId.ofOffset("GMT", "-05:00" );
			ErrorInfo result = WbcUtilities.getTimezoneFromHost( ivHostIp, "8765" );
			if ( result.isOK()) {
				timezoneGMT = result.getErrorText();
			} else {
				logger.error("CRCHelper:getSwitchErrorsHeaderDateTimes failed to get timezone, using GMT-00:00");
				timezoneGMT = "GMT-00:00";
			}


			// get ending hours from last ping
			// need to know timezone of host if this is done on stored database
			LocalDateTime ldt = lastPing.toLocalDateTime();
			// get a time with an even hour
			LocalDateTime lastDtHour;
			if ( !period.equals("week")) {
				lastDtHour = ldt.of(
						ldt.getYear(),
						ldt.getMonth(),
						ldt.getDayOfMonth(),
						ldt.getHour(),
						0);
			} else {
				lastDtHour = ldt.of(
						ldt.getYear(),
						ldt.getMonth(),
						ldt.getDayOfMonth(),
						0,
						0);

			}
			// calculate the start time
			LocalDateTime startDt;
			if ( !period.equals("week")) {
				startDt = lastDtHour.minusHours((hoursPerPeriod * numValues) + 1);  // add 1 hour to get the first hour's data
			} else {
				startDt = lastDtHour.minusHours((hoursPerPeriod * numValues) );
			}
			if (debugData) {
				logger.debug( "StartDt " + startDt.toString());
			}
			Timestamp start = Timestamp.valueOf( startDt);

			for (int i=0; i<numValues; i++) {
				LocalDateTime newLdt = startDt.plusHours( i * hoursPerPeriod );
				ZonedDateTime zdt =  newLdt.atZone( ZoneId.of( timezoneGMT ));
				Date date = Date.from( zdt.toInstant() );
				if (debugData) logger.debug( "Period " + i + "  " + date.toString());
				dateList.add( date );
			}
		} catch (Exception e) {
			methodError.setResult("error");
			methodError.setErrorText( String.format("getSwitchErrorsHeaderDateTimes: Exception. %s", e.getMessage() ));
			e.printStackTrace();
		}
		return dateList;
	}

	private Timestamp getLastPingTime( com.wbc.mysql.MysqlConnection conn ) {
		int sampleno = ThresholdUtil.getLastSampleno( conn, ThresholdUtil.TYPE_IV);
		Timestamp ts = TimeUtil.convertSamplenoToTimestamp(sampleno);
		return ts;
	}

}
