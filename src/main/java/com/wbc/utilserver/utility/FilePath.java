package com.wbc.utilserver.utility;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class FilePath
{
	private static final Logger logger = LogManager.getLogger( FilePath.class);

	public static String getPath(String path)
	{
		String realPath = path.replace( "target/classes", "src/main/resources" );
		// if(realPath.startsWith("/") && SystemUtils.IS_OS_WINDOWS)
		if(realPath.startsWith("/") && realPath.contains(":"))
		{
			realPath = realPath.substring( 1 );
		}

		boolean debug1 = false;
		if (debug1) logger.debug(String.format("DEBUG realPath: file is %s", realPath));
		// remove %20 from name
		StringBuilder sb = new StringBuilder();
		int index = realPath.indexOf("%");
		if (index > 1) {
			if (debug1) logger.debug(String.format("FilePath: REMOVE when tested!! filename has a percent must be development on Junaid oldfinename = %s", realPath));
			sb.append(realPath.substring(0, index));
			sb.append(" ");
			sb.append(realPath.substring(index + 3));
		} else {
			sb.append(realPath);
		}

		/// TEST REMOVE
		/*
		if (MainAccessor.ouiVendorData != null) {
			if (MainAccessor.ouiVendorData.isHasMap()) {
				logger.debug("FilePath ouiData hasMap OK");
			} else {
				logger.debug("FilePath ouiData hasMap FAILED");
			}
		} else {
			logger.debug("FilePath ouiData test = NULL data");
			OuiData ouiVendorMacData = new OuiData();
			String result = ouiVendorMacData.initMap();
			if (!result.isEmpty()) {
				logger.error("FilePath: DashboardUtilServerApplication: init outData failed: " + result );
			} else {
				MainAccessor.ouiVendorData = ouiVendorMacData;
				if (MainAccessor.ouiVendorData.isHasMap()) {
					logger.debug("FilePath ouiData hasMap OK");
				} else {
					logger.debug("FilePath ouiData hasMap FAILED");
				}
			}
		}

		 */
		return sb.toString();
	}
}
