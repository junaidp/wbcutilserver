package com.wbc.utilserver.helper;

import com.wbc.utilserver.model.ErrorInfo;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ServerUtility {

    private static final Logger logger = LogManager.getLogger( ServerUtility.class);

    public static ErrorInfo getIntravueBase() {
        ErrorInfo errorInfo = new ErrorInfo();
        String base = System.getProperty("catalina.base");

        //TODO: Change server to be apache
        if (base == null ) {
            logger.error("The system property for catalina.base does not exist!.  Temp patch");
            base = "D:\\intravue\\autoip\\tomcat8";
        } else if ( !base.contains("intravue")) {
            logger.error(String.format("The base returned from catalina.properties does not contain intravue. Temp patch set to Jim's Intravue, was %s.", base ));
            base = "D:\\intravue\\autoip\\tomcat8";
        }
        int index = base.indexOf( "intravue");
        if (index < 2) {
            // invalid base
            errorInfo.setResult("error");
            errorInfo.setErrorText("The path to Intravue's home directory was not found.");
            logger.error(errorInfo.getErrorText());
        } else {
            errorInfo.setErrorText( base.substring(0,index+8));
        }
        return( errorInfo );
    }
}
