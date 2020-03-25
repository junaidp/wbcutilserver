package com.wbc.utilserver.controller;

import com.wbc.utilserver.helper.CreateCleanDatabaseHelper;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CreateCleanDatabaseController {

    private static final Logger logger = LogManager.getLogger(CreateCleanDatabaseController.class);
    @Autowired
    private CreateCleanDatabaseHelper cleanDatabaseHelper;

    @GetMapping(value = "/createCleanDatabase")
    public String createCleanDatabase(@RequestParam String dbName, @RequestParam String ip, @RequestParam boolean overWrite){
        String results;
        logger.debug( String.format("These are the params dbName=%s, ip=%s ", dbName, ip));
        if (overWrite) {
            results = cleanDatabaseHelper.createCleanDatabase(dbName, ip, true);
        } else {

            results = cleanDatabaseHelper.createCleanDatabase(dbName, ip, false);
        }
		return results;

	}
}
