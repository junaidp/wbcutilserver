package com.wbc.utilserver.utility;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;

public class AuthenticationHandler {

    private static final Logger logger = LogManager.getLogger( AuthenticationHandler.class);

    String fullFilename = "";
    private int authCode=-1;
    private String hostPK;
    boolean debug = true;


    public static final int FILE_NOT_EXIST = 101;
    public static final int FILE_HASH_INVALID = 102;
    public static final int PARSE_EXCEPTION = 106;
    public static final int PARSE_PK_NOTFOUND = 107;
    public static final int PK_VALID = 1;
    public static final int PK_VALID_EXPIRED = 110;


    public AuthenticationHandler(String fullFilename, String hostPK ) {
        this.fullFilename = fullFilename;
        this.hostPK = hostPK;
    }

    public int getAuthorization()  {
        File fFile = new File(fullFilename) ;
        if ( !fFile.exists()) {
            logger.error( "getAuthorization: file does not exist > " + fullFilename );
            authCode = FILE_NOT_EXIST;
        } else {
            if ( !isHashcodeValid( fFile )) {
                authCode = FILE_HASH_INVALID;
            } else  {
                // find the pk in the file and return a code telling the result
                authCode = getCodeFromFile( fFile );
            }
        }
        return authCode;
    }


    private boolean isHashcodeValid( File dataFile )  {
        String hashInFile = getHashFromFile( dataFile );
        if (debug) logger.debug( "isHashcodeValid: hashinFile " + hashInFile );
        String tmpString = readDatFileAsString(dataFile);
        String hexHash;
        try {
            hexHash = SHAHelper.SHA1(tmpString);
            if (debug) logger.debug( "isHashcodeValid: hexHash " + hexHash );
            if ( hashInFile.equals( hexHash ))
            {
                return true;
            }
        } catch (NoSuchAlgorithmException ex) {
            logger.error( "isHashcodeValid: " +  ex.getMessage() );
        } catch (UnsupportedEncodingException ex) {
            logger.error( "isHashcodeValid: " +  ex.getMessage() );
        }
        return false;
    }

    private int getCodeFromFile( File datFile )  {
        int code=-1;
        Scanner scanner = null;
        int currentSection = -1;
        try {
            scanner = new Scanner( datFile );
            // parse whole file to end, a BASIC pk may be superceded by a PRO pk
            boolean matchFound = false;
            while ( scanner.hasNextLine() ) {
                String line = scanner.nextLine();
                // if (debug) log (" reading line > " + line );
                if (line.startsWith( hostPK )) {
                    matchFound = true;
                    // now get expiration
                    LocalDate today = LocalDate.now();
                    String dateString = line.substring(26);
                    if (dateString.length() == 10) {
                        LocalDate expires = LocalDate.parse( dateString);
                        if ( expires.isAfter(today)) {
                            code= PK_VALID;
                        } else {
                            // found pk but expired
                            code= PK_VALID_EXPIRED;
                        }
                    }
                    if (debug) logger.debug ("Found match, code " + code );
                }
            }
            if ( !matchFound ) {
                code = PARSE_PK_NOTFOUND;
            }
        }
        catch( FileNotFoundException fnf )  {
            logger.error( "getCodeFromFile: " + datFile + " " + fnf.getMessage() );
            code = FILE_NOT_EXIST;
        }
        catch( Exception e1 )       {
            logger.error( "getCodeFromFile: " + datFile + " " + e1.getMessage() );
            code = PARSE_EXCEPTION;
        }
        finally {
            //ensure the underlying stream is always closed
            if (scanner != null)
                scanner.close();
        }
        return code;
    }


    private String getHashFromFile( File datFile )  {
        Scanner scanner = null;
        String hash = "";
        try {
            scanner = new Scanner( datFile );
            // parse whole file to end, a BASIC pk may be superceded by a PRO pk
            boolean matchFound = false;
            while ( scanner.hasNextLine() && !matchFound ) {
                String line = scanner.nextLine();
                if (debug) logger.debug (" reading line > " + line );
                if ( line.startsWith("hash=") ) {
                    hash = line.substring(5);
                    matchFound = true;
                    break;
                }
            }
        }
        catch( FileNotFoundException fnf )  {
            logger.error( "getCodeFromFile: " + datFile + " " + fnf.getMessage() );
        }
        catch( Exception e1 )       {
            logger.error( "getCodeFromFile: " + datFile + " " + e1.getMessage() );
        }
        finally {
            //ensure the underlying stream is always closed
            if (scanner != null)
                scanner.close();
        }
        return hash;
    }

    private String readDatFileAsString( File file )
    {
        Scanner scanner = null;
        StringBuilder filecontents = new StringBuilder();
        try {
            scanner = new Scanner( file );
            // parse whole file to end, a BASIC pk may be superceded by a PRO pk
            while ( scanner.hasNextLine() ) {
                String line = scanner.nextLine();
                if ( line.startsWith("hash") ) {
                    continue;
                }
                filecontents.append( line );
            }
        }
        catch( Exception e1 )       {
            logger.error( "readDatFileAsString: Exception " + file + " " + e1.getMessage() );
            filecontents.setLength(0);
        }
        finally {
            //ensure the underlying stream is always closed
            if (scanner != null)
                scanner.close();
        }
        return filecontents.toString();
    }

    public static String getMessageForCode(Locale locale, int code )  {
        String msg = "";
        /*
        try  {
            ResourceBundle resourceMessages = ResourceBundle.getBundle(  "DashboardAuthenticator_bundle", locale, new UTF8Control() );
            if ( code == FILE_NOT_EXIST ) {
                msg = ResourceMessageUtil.getResourceString( resourceMessages, "FILE_NOT_EXIST");
            }
            else if ( code == FILE_HASH_INVALID ) {
                msg = ResourceMessageUtil.getResourceString( resourceMessages, "FILE_HASH_INVALID");
            }
            else if ( code == PK_NO_HOST_RESPONSE ) {
                msg = ResourceMessageUtil.getResourceString( resourceMessages, "PK_NO_HOST_RESPONSE");
            }
            else if ( code == PK_NO_PK ) {
                msg = ResourceMessageUtil.getResourceString( resourceMessages, "PK_NO_PK");
            }
            else if ( code == PK_URL_EXCEPTION ) {
                msg = ResourceMessageUtil.getResourceString( resourceMessages, "PK_URL_EXCEPTION");
            }
            else if ( code == PARSE_EXCEPTION ) {
                msg = ResourceMessageUtil.getResourceString( resourceMessages, "PARSE_EXCEPTION");
            }
            else if ( code == PARSE_PK_NOTFOUND ) {
                msg = ResourceMessageUtil.getResourceString( resourceMessages, "PARSE_PK_NOTFOUND");
            }
            else if ( code == NO_RESPONSE_AUTHENTICATOR ) {
                msg = ResourceMessageUtil.getResourceString( resourceMessages, "NO_RESPONSE_AUTHENTICATOR");
            }
            else if ( code == NO_ELEMENTS_AUTHENTICATOR ) {
                msg = ResourceMessageUtil.getResourceString( resourceMessages, "NO_ELEMENTS_AUTHENTICATOR");
            }
            else if ( code == BASIC ) {
                msg = ResourceMessageUtil.getResourceString( resourceMessages, "BASIC");
            }
            else if ( code == BASIC_DEMO ) {
                msg = ResourceMessageUtil.getResourceString( resourceMessages, "BASIC_DEMO");
            }
            else if ( code == PRO ) {
                msg = ResourceMessageUtil.getResourceString( resourceMessages, "PRO");
            }
            else if ( code == PRO_DEMO ) {
                msg = ResourceMessageUtil.getResourceString( resourceMessages, "PRO_DEMO");
            }
            else  {
                msg = ResourceMessageUtil.getResourceString( resourceMessages, "UNKNOWN_CODE");
            }
        } catch ( Exception ex )  {

         */
            if ( code == FILE_NOT_EXIST ) {
                msg = "The Authentication File could not be found";
            }
            else if ( code == FILE_HASH_INVALID ) {
                msg = "The Authentication File hash code is not valid";
            }
            else if ( code == PARSE_EXCEPTION ) {
                msg = "Exception: parsing the Authentication file";
            }
            else if ( code == PARSE_PK_NOTFOUND ) {
                msg =  "The product key was not found in the Authentication File";
            }
            else  {
                msg = "UNKNOWN_CODE";
            }
        /*
        }
        */
        return msg;
    }

}
