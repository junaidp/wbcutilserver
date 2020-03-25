package com.wbc.utilserver.helper;

import com.google.gson.Gson;
import com.wbc.utilserver.model.ErrorInfo;
import com.wbc.mysql.MysqlConnection;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

@Component
public class CreateCleanDatabaseHelper
{
	@Autowired
//
	private static final Logger logger = LogManager.getLogger( CreateCleanDatabaseHelper.class);


	public String createCleanDatabase(String dbName, String ivIP, boolean overwrite )
	{
		ErrorInfo result = new ErrorInfo();

		String ivHome;
		ErrorInfo baseInfo = ServerUtility.getIntravueBase();
		logger.debug("hello from helper");
		if (!baseInfo.getResult().equals("ok")) {
			result.setErrorText(baseInfo.getErrorText());
			logger.error(result.getErrorText());
			Gson gson = new Gson();
			return( gson.toJson( result ));
		} else {
			ivHome = baseInfo.getErrorText();
		}
		String path = ivHome + "/dbBackup";
		if (!dbName.endsWith(".dmp")) {
			dbName = dbName.concat(".dmp");
		}
		File fOutputFile = new File( path + "/" + dbName );
		if (fOutputFile.exists() && !overwrite) {
			result.setResult("warning");
			result.setErrorText("The database backup file exist, " + fOutputFile.toString() + ". Please change the name or enable the overwrite option.");
			logger.error(result.getErrorText());
			Gson gson = new Gson();
			return( gson.toJson( result ));
		}

		File fInFile = new File( ivHome + "/cleandbbackup/cleandb.dmp");
		if ( !fInFile.exists()) {
			result.setResult("error");
			result.setErrorText("The IntraVUE clean database was not found.  Unexpected error");
			logger.error(result.getErrorText());
			Gson gson = new Gson();
			return( gson.toJson( result ));
		}

// get a sql connection
		MysqlConnection conn = new MysqlConnection( "intravue", "netvue", "netvue");
		try {
			if (!conn.isConnected()) {
				if (!conn.getConnection(ivIP)) {
					result.setResult("error");
					result.setErrorText("Failed to get connection to IntraVUE database");
					logger.error(result.getErrorText());
					Gson gson = new Gson();
					return( gson.toJson( result ));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			result.setResult("error");
			result.setErrorText("Unexpected Exception: " + e.getMessage() );
			logger.error(result.getErrorText());
			Gson gson = new Gson();
			return( gson.toJson( result ));
		}
		result = create( conn, fInFile, fOutputFile);
		conn.close();
		Gson gson = new Gson();
		return( gson.toJson( result ));
	}

	private ErrorInfo create(MysqlConnection conn, File fCleandb, File fOut) {
		OutputStream os = null;
		ErrorInfo result = new ErrorInfo();
		logger.debug("create: fOut name = " + fOut.getAbsolutePath() );
			//Incase if directory not exist, create it
			fOut.getParentFile().mkdirs();

		try (Scanner scanner = new Scanner(fCleandb)) {
			os = new FileOutputStream(fOut);
			byte[] buffer = new byte[1024];
			int length;
			String lf = "\n";
			while (scanner.hasNext()){
				String line = scanner.nextLine();
				os.write(line.getBytes());
				os.write(lf.getBytes());
				if ( line.startsWith("/*!40000 ALTER TABLE `network` DISABLE")) {
					String networks = getNetworks( result, conn );
					if (!result.isOK()) {
						return result;
					}
					os.write( networks.getBytes());
					os.write(lf.getBytes());
				}
				if ( line.startsWith("/*!40000 ALTER TABLE `scanrange` DISABLE")) {
					String ranges = getRanges( result, conn );
					if (!result.isOK()) {
						return result;
					}
					os.write( ranges.getBytes());
					os.write(lf.getBytes());
				}
			}
			os.close();
		} catch (IOException e) {
			result.setResult("error");
			result.setErrorText("IOException: for file " + fOut.getAbsolutePath() + "  >> " + e.getMessage());
			e.printStackTrace();
			logger.error(result.getErrorText());
		}
		return result;
	}

	/*
            CREATE TABLE `network` (
              `NwId` int(10) NOT NULL AUTO_INCREMENT,
              `NetworkId` int(10) NOT NULL DEFAULT '-1',
              `Name` char(40) NOT NULL DEFAULT '',
              `UseCount` int(11) NOT NULL DEFAULT '-1',
              `NetGroup` int(10) NOT NULL DEFAULT '0',
              `Agent` char(16) DEFAULT NULL,
              PRIMARY KEY (`NwId`),
              KEY `NwidIdx` (`NetworkId`)
            ) ENGINE=InnoDB DEFAULT CHARSET=latin1;
     */
	private String getNetworks(ErrorInfo errorInfo, MysqlConnection conn) {
		StringBuilder sb = new StringBuilder();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery( "select * from network" );
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();

			sb.append("INSERT INTO `network` VALUES ");
			while ( rs.next() ) {
				sb.append("(");
				sb.append( rs.getObject(1));
				sb.append(",");
				sb.append( rs.getObject(2));
				sb.append(",'");
				sb.append( rs.getObject(3));
				sb.append("',");
				sb.append( rs.getObject(4));
				sb.append(",");
				sb.append( rs.getObject(5));
				sb.append(",''),");
			}
			sb.deleteCharAt( sb.length()-1);  // get rid of last comma
			sb.append(";");
			sb.append("\n");
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			errorInfo.setResult("error");
			errorInfo.setErrorText( String.format("getNetworks: SQLEsception. %s", e.getMessage()));
		}
		return sb.toString();
	}
	private String getRanges(ErrorInfo errorInfo, MysqlConnection conn) {
		StringBuilder sb = new StringBuilder();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery( "select * from scanrange" );
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();

			sb.append("INSERT INTO `scanrange` VALUES ");
			while ( rs.next() ) {
				sb.append("(");
				sb.append( rs.getObject(1));
				sb.append(",'");
				sb.append( rs.getObject(2));
				sb.append("','");
				sb.append( rs.getObject(3));
				sb.append("','");
				sb.append( rs.getObject(4));
				sb.append("','");
				sb.append( rs.getObject(5));
				if (columnsNumber > 5) {
					sb.append("',");
					sb.append(rs.getObject(6));
				}
				sb.append("),");
			}
			sb.deleteCharAt( sb.length()-1);  // get rid of last comma
			sb.append(";");
			sb.append("\n");
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			e.printStackTrace();
			errorInfo.setResult("error");
			errorInfo.setErrorText( String.format("getRanges: SQLEsception. %s", e.getMessage()));
		}
		return sb.toString();
	}







}
