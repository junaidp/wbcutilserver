package com.wbc.utilserver.utility;

import com.wbc.utilserver.model.ErrorInfo;
import com.wbc.mysql.MysqlConnection;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class WbcUtilities {

    private static final Logger logger = LogManager.getLogger( WbcUtilities.class);

    public static ErrorInfo getTimezoneFromHost(String hostip, String hostPort ) {
        ErrorInfo responseInfo = new ErrorInfo();
        try {
            String target = "http://".concat(hostip)
                    .concat(":")
                    .concat( hostPort )
                    .concat( "/logread?file=autoip/time.out");
            URL url = new URL( target);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/text");

            if (conn.getResponseCode() != 200) {
                responseInfo.setResult("error");
                responseInfo.setErrorText("Failed : HTTP error code : " + conn.getResponseCode());
                return responseInfo;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            String response = "";
            boolean found = false;
            while ((output = br.readLine()) != null) {
//                System.out.println(output);
                if (output.contains("GMT")) {
                    response = output.substring( output.indexOf("GMT"));
                    found = true;
                }
//                response.concat( output );
            }
            if ( found ) {
                responseInfo.setErrorText(response);
            } else {
                responseInfo.setResult("error");
                responseInfo.setErrorText("GMT not found in result. Is Intravue version 3.1+");
            }
            conn.disconnect();

        } catch (MalformedURLException e) {
            responseInfo.setResult("error");
            responseInfo.setErrorText( String.format("getTimezoneFromHost MalformedURLException %s", e.getMessage()));
            logger.error(responseInfo.getErrorText());
            e.printStackTrace();
        } catch (IOException e) {
            responseInfo.setResult("error");
            responseInfo.setErrorText( String.format("getTimezoneFromHost IOException %s", e.getMessage()));
            logger.error(responseInfo.getErrorText());
            e.printStackTrace();
        }
        return responseInfo;
    }

    public static ErrorInfo getNetworkNameFromNetworkId(MysqlConnection conn, int networkid) {

        StringBuilder sb = new StringBuilder();
        sb.append( "SELECT ");
        sb.append( "ifd.DescId, ");           // 1
        sb.append( "dl1.parent, ");           // 2
        sb.append( "dl1.parent, ");           // 3   // no longer using param 3, but putting dummy in so order does not change
        sb.append( "ifd.ipaddress, ");        // 4
        sb.append( "d.desctype, ");           // 5
        sb.append( "d.networkid, ");          // 6
        sb.append( "ifd.macAddress, ");       // 7
        sb.append( "ifd.netmask, " );         // 8
        sb.append( "ifd.pingtime " );         // 9
        sb.append( "from ifdesc as ifd, desclink as dl1, descriptor as d " );
        sb.append( " where ifd.IfWasPinged=1 " );
        sb.append( " and ifd.DescId = dl1.child " );
        sb.append( " and dl1.parent = d.DescId  " );
        // System.out.println( sb.toString());
        ErrorInfo result = new ErrorInfo();
        String query = String.format("Select name from network where networkid=%d", networkid );
        Statement stmt = conn.createStatement();
        try {
            ResultSet rs = stmt.executeQuery( query );
            if (rs.next()) {
                result.setErrorText( rs.getString(1));
            } else {
                result.setResult("error");
                result.setErrorText("Not Found");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            result.setResult("error");
            result.setErrorText( String.format("getNetworkNameFromNetworkId SQL Exception %s", e.getMessage()));
            logger.error(result.getErrorText());
        }
        return result;
    }

    public static ErrorInfo getProductKey(String hostip ) {
        ErrorInfo responseInfo = new ErrorInfo();
        try {
            String target = "http://".concat(hostip)
                    .concat(":8765/iv2/license");
            URL url = new URL( target);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/text");

            if (conn.getResponseCode() != 200) {
                responseInfo.setResult("error");
                responseInfo.setErrorText("Failed to get PK: HTTP error code : " + conn.getResponseCode());
                return responseInfo;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            String pk = "";
            boolean found = false;
            while ((output = br.readLine()) != null) {
                if (output.startsWith("{\"keyCode")) {
                    int index = output.indexOf("productKey") + 13;
                    pk = output.substring( index, index + 25 );
                    found = true;
                }
            }
            if ( found ) {
                responseInfo.setErrorText(pk);
            } else {
                responseInfo.setResult("error");
                responseInfo.setErrorText("Product key not found.");
            }
            conn.disconnect();

        } catch (MalformedURLException e) {
            responseInfo.setResult("error");
            responseInfo.setErrorText( String.format("getProductKey MalformedURLException %s", e.getMessage()));
            logger.error(responseInfo.getErrorText());
            e.printStackTrace();
        } catch (IOException e) {
            responseInfo.setResult("error");
            responseInfo.setErrorText( String.format("getProductKey IOException %s", e.getMessage()));
            logger.error(responseInfo.getErrorText());
            e.printStackTrace();
        }
        return responseInfo;
    }

    public static ErrorInfo validateProductKey(String ip, String userEmail, String productKey, String authenticationFilename) {
        ErrorInfo responseInfo = new ErrorInfo();
        if (ip.isEmpty()) {
            responseInfo.setResult("error");
            responseInfo.setErrorText("validateProductKey: IP of host missing");
            return responseInfo;
        }
        try {
            String target = "http://".concat(ip)
                    .concat(":8765/iv2/license");
            URL url = new URL( target);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/text");

            if (conn.getResponseCode() != 200) {
                responseInfo.setResult("error");
                responseInfo.setErrorText("Failed to get PK: HTTP error code : " + conn.getResponseCode());
                logger.debug( String.format("validateProductKey: the url that failed was %s", url.toString()) );
                return responseInfo;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            String pk = "";
            String valid= "";
            boolean pkFound = false;
            boolean vFound = false;
            while ((output = br.readLine()) != null) {
                if (output.startsWith("{\"keyCode")) {
                    int index = output.indexOf("productKey") + 13;
                    pk = output.substring( index, index + 25 );
                    if (pk.startsWith("3AL")) pkFound = true;
                    if (pkFound) {
                        index = output.indexOf("verifies") + 10;
                        valid = output.substring(index, index + 1);
                    }
                }
            }
            if ( pkFound ) {
                if ( !pk.equals(productKey)) {
                    responseInfo.setResult("error");
                    responseInfo.setErrorText(String.format("Product key found, %s, does not match product key requested %s.", pk, productKey));
                } else if ( !valid.equals("1")) {
                    responseInfo.setResult("error");
                    responseInfo.setErrorText("Product key found but it is not a valid registration.");
                } else {
                    // check the PK in the authorized list
                    AuthenticationHandler handler = new AuthenticationHandler( authenticationFilename, "3ALU4XX03EDA000S541698990");
                    int result = handler.getAuthorization();
                    if (result != 1) {
                        responseInfo.setResult("error");
                        responseInfo.setErrorText(String.format( "Product key authentication error for PK %s, %s. Please contract support", pk, handler.getMessageForCode(null, result) ));
                    }
                }
            } else {
                responseInfo.setResult("error");
                responseInfo.setErrorText("Product key not found.");
            }
            conn.disconnect();
        } catch (MalformedURLException e) {
            responseInfo.setResult("error");
            responseInfo.setErrorText( String.format("getProductKey MalformedURLException %s", e.getMessage()));
            logger.error(responseInfo.getErrorText());
            e.printStackTrace();
        } catch (IOException e) {
            responseInfo.setResult("error");
            responseInfo.setErrorText( String.format("getProductKey IOException %s", e.getMessage()));
            logger.error(responseInfo.getErrorText());
            e.printStackTrace();
        }
        return responseInfo;
    }

    private static boolean isPkAuthorized(String pk) {
        // open the authorized list file
        // check its crc value
        // look up pk in list

        // for now we will just return true;
        return true;
    }
}
