package com.wbc.utilserver.controller;

import com.google.gson.Gson;
import com.wbc.utilserver.helper.CRCHelper;
import com.wbc.utilserver.helper.SwitchProbeHelper;
import com.wbc.utilserver.model.ErrorInfo;
import com.wbc.utilserver.model.SwtichprobeReportInfo;
import com.wbc.utilserver.utility.FilePath;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.URL;

@RequestMapping("/switchProbe")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class SwitchProbeController
{
	private static String UPLOADED_FOLDER = "static/";
	private static final Logger logger = LogManager.getLogger( SwitchProbeController.class);

	@Autowired
	private SwitchProbeHelper switchProbeHelper;

	@GetMapping( "getSwitchData" )
	public String getSwitchData( @RequestParam String reportName, @RequestParam String ip, @RequestParam boolean overWrite, @RequestParam boolean bGetFromSwitch )
	{
		if (false) {
			if (reportName.equals("127.0.0.1")) {
				//reportName = "P:/2019 Java8/ParseSwitchProbeFile/172.23.170.1.prob.txt";
				// reportName = "P:/2019 Java8/ParseSwitchProbeFile/HPSwitch_SwitchProbe.txt";
				// reportName = "P:/2019 Java8/ParseSwitchProbeFile/172.23.133.132-191025.probe.txt";
				reportName = "P:/2019 Java8/ParseSwitchProbeFile/T-L_172.20.116.20-probe.txt";
			}
			ClassLoader classLoader = getClass().getClassLoader();
			//URL resource = classLoader.getResource("static/switchProbeFile.txt");
			URL resource = null;
			if (resource == null) {
				reportName = "P:/2019 Java8/ParseSwitchProbeFile/T-L_172.20.116.20-probe.txt";

			} else {
				reportName = FilePath.getPath(resource.getFile());

			}
		} else {
			// Junaid is having file always uploaded to the same location
			ClassLoader classLoader = getClass().getClassLoader();
			URL resource = classLoader.getResource(UPLOADED_FOLDER );
			String path = resource.getFile();
			if (path != null && !path.isEmpty()) {
				String oldFilename = FilePath.getPath(path);
				StringBuilder sb = new StringBuilder();
				int index = oldFilename.indexOf("%");
				if (index > 1) {
logger.debug(String.format("SwitchProbeController: REMOVE when tested!! filename has a percent must be development on Junaid oldfinename = %s", oldFilename));
					sb.append(oldFilename.substring(0, index));
					sb.append(" ");
					sb.append(oldFilename.substring(index + 3));
				} else {
					sb.append(oldFilename);
				}
				sb.append("switchProbeFile.txt");
				reportName = sb.toString();
			} else {
				SwtichprobeReportInfo reportInfo = new SwtichprobeReportInfo();
				ErrorInfo result = reportInfo.getErrorInfo();
				result.setResult("error");
				result.setErrorText("Failed to find path from resource" );
				Gson gson = new Gson();
				return( gson.toJson( reportInfo ));
			}
		}
		return switchProbeHelper.getSwitchData( reportName, ip, bGetFromSwitch, overWrite );

	}
}

