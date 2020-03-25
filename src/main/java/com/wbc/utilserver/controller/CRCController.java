package com.wbc.utilserver.controller;

import com.wbc.utilserver.helper.CRCHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/crc")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class CRCController
{
	@Autowired
	private CRCHelper crcHelper;

	@GetMapping("getSwitchErrors")
	public String getSwitchErrors(@RequestParam String period, @RequestParam String type,  @RequestParam String ip) {
		String  errors = crcHelper.getAllData( period, type, ip);
		return errors;
	}

}
