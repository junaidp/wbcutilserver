package com.wbc.utilserver.helper;

import com.google.gson.Gson;
import com.wbc.utilserver.model.SwtichprobeReportInfo;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

    // @SpringBootTest add this if like to use annotation(need to then add spring-boot-starter-test in pom.xml as in old project
class SwitchProbeHelperTest {


    SwitchProbeHelper switchProbeHelper = new SwitchProbeHelper();

    static final String developer = "jim";
    @Test
    @Ignore
    void getSwitchData() {
        String testSwitchprobeFile;
        if (developer.equals("jim")) {
            testSwitchprobeFile = "P:\\ParseSwitchProbeFile/172.23.170.1.probe.191126.txt";
        } else {
            testSwitchprobeFile = "/Users/junaidparacha/Documents/DashboardProject/dashboardutilserver/src/main/resources/static/172.23.170.1.prob.txt";
        }
        String jsonData = switchProbeHelper.getSwitchData( testSwitchprobeFile,"127.0.0.1", false, false);
        Gson gson = new Gson();
        SwtichprobeReportInfo data = gson.fromJson( jsonData, SwtichprobeReportInfo.class );
        assertTrue( data.getGeneralData().isComplete() );
        data.getErrorMessages().forEach( s -> System.out.println("ERROR " + s));
        data.getWarningMessages().forEach( s -> System.out.println("WARNING " + s));

    }

    @Test
    @Ignore
    void getSwitchDataForHP() {
        String testSwitchprobeFile;
        if (developer.equals("jim")) {
            testSwitchprobeFile = "P:\\ParseSwitchProbeFile/HPSwitch_SwitchProbe.txt";
        } else {
            testSwitchprobeFile = "c:/user/junaid/???????";
        }

        String jsonData = switchProbeHelper.getSwitchData( testSwitchprobeFile,"127.0.0.1", false, false);
        Gson gson = new Gson();
        SwtichprobeReportInfo data = gson.fromJson( jsonData, SwtichprobeReportInfo.class );
        assertTrue( data.getGeneralData().isComplete() );
        data.getErrorMessages().forEach( s -> System.out.println("ERROR " + s));
        data.getWarningMessages().forEach( s -> System.out.println("WARNING " + s));
    }

    @Test
    @Ignore
    void getSwitchDataForTest2() {
        String testSwitchprobeFile;
        if (developer.equals("jim")) {
            testSwitchprobeFile = "P:/ParseSwitchProbeFile/T-L_172.20.116.20-probe.txt";
        } else {
            testSwitchprobeFile = "c:/user/junaid/???????";
        }

        String jsonData = switchProbeHelper.getSwitchData( testSwitchprobeFile,"127.0.0.1", false, false);
        Gson gson = new Gson();
        SwtichprobeReportInfo data = gson.fromJson( jsonData, SwtichprobeReportInfo.class );
        assertTrue( data.getGeneralData().isComplete() );
        data.getErrorMessages().forEach( s -> System.out.println("ERROR " + s));
        data.getWarningMessages().forEach( s -> System.out.println("WARNING " + s));


    }


}