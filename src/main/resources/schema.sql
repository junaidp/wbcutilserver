DROP DATABASE IF EXISTS `intravue1`;
CREATE DATABASE IF NOT EXISTS `intravue1` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `intravue1`;


-- Dumping structure for table intravue.aamarker
DROP TABLE IF EXISTS `aamarker`;
CREATE TABLE IF NOT EXISTS `aamarker` (
  `dummy` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.adjthr
DROP TABLE IF EXISTS `adjthr`;
CREATE TABLE IF NOT EXISTS `adjthr` (
  `threshid` int(11) NOT NULL DEFAULT '0',
  `tpl` tinyint(4) NOT NULL DEFAULT '0',
  `enabled` tinyint(4) NOT NULL DEFAULT '1',
  `sampleno` int(11) NOT NULL DEFAULT '0',
  `v1thr` float NOT NULL DEFAULT '0',
  `v2thr` float NOT NULL DEFAULT '0',
  `currentstate` tinyint(4) NOT NULL DEFAULT '0',
  `reportedstate` tinyint(4) NOT NULL DEFAULT '0',
  `reportedsampleno` int(11) NOT NULL DEFAULT '0',
  `speed` float NOT NULL DEFAULT '0',
  PRIMARY KEY (`threshid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.adjthrsample
DROP TABLE IF EXISTS `adjthrsample`;
CREATE TABLE IF NOT EXISTS `adjthrsample` (
  `threshid` int(11) NOT NULL DEFAULT '0',
  `sampleno` int(11) NOT NULL DEFAULT '0',
  `v1` float NOT NULL DEFAULT '0',
  `v2` float NOT NULL DEFAULT '0',
  `v1pk` float NOT NULL DEFAULT '0',
  `v2pk` float NOT NULL DEFAULT '0',
  PRIMARY KEY (`threshid`,`sampleno`) USING BTREE,
  KEY `sampleno` (`sampleno`) USING BTREE
) ENGINE=MEMORY DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.adjthrsample_arch
DROP TABLE IF EXISTS `adjthrsample_arch`;
CREATE TABLE IF NOT EXISTS `adjthrsample_arch` (
  `threshid` int(11) NOT NULL DEFAULT '0',
  `sampleno` int(11) NOT NULL DEFAULT '0',
  `v1` float NOT NULL DEFAULT '0',
  `v2` float NOT NULL DEFAULT '0',
  `v1pk` float NOT NULL DEFAULT '0',
  `v2pk` float NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.assetevents
DROP TABLE IF EXISTS `assetevents`;
CREATE TABLE IF NOT EXISTS `assetevents` (
  `EventId` int(10) NOT NULL AUTO_INCREMENT,
  `assetid` int(11) NOT NULL DEFAULT '0',
  `satelliteip` char(16) DEFAULT '0.0.0.0',
  `satelliteEventId` int(10) NOT NULL DEFAULT '0',
  `macaddress` char(20) DEFAULT '0.0.0.0',
  `DescId` int(10) NOT NULL DEFAULT '0',
  `Occurred` int(11) NOT NULL DEFAULT '0',
  `satelliteTime` int(11) NOT NULL DEFAULT '0',
  `IpAddress` char(16) DEFAULT '0.0.0.0',
  `Description` char(255) DEFAULT '',
  `Type` tinyint(4) DEFAULT NULL,
  `Class` int(11) NOT NULL DEFAULT '0',
  `networkName` varchar(255) DEFAULT NULL,
  `networkID` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`EventId`),
  KEY `asset` (`assetid`),
  CONSTRAINT `fk_assetid` FOREIGN KEY (`assetid`) REFERENCES `devicelist` (`assetid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.assetinfo
DROP TABLE IF EXISTS `assetinfo`;
CREATE TABLE IF NOT EXISTS `assetinfo` (
  `assetid` int(11) NOT NULL DEFAULT '0',
  `type` int(11) NOT NULL DEFAULT '0',
  `value` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`assetid`,`type`),
  CONSTRAINT `fk_devicelist` FOREIGN KEY (`assetid`) REFERENCES `devicelist` (`assetid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.container
DROP TABLE IF EXISTS `container`;
CREATE TABLE IF NOT EXISTS `container` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `locX` varchar(20) NOT NULL DEFAULT '',
  `locY` varchar(20) NOT NULL DEFAULT '',
  `length` varchar(20) NOT NULL DEFAULT '',
  `width` varchar(20) NOT NULL DEFAULT '',
  `name` varchar(255) NOT NULL DEFAULT '',
  `imageURL` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=695 DEFAULT CHARSET=latin1;

-- Dumping structure for procedure intravue.cull_adjthrsample_archive
DROP PROCEDURE IF EXISTS `cull_adjthrsample_archive`;
DELIMITER //
CREATE DEFINER=`netvue`@`localhost` PROCEDURE `cull_adjthrsample_archive`()
begin
 drop temporary table if exists t;
 create temporary table t(sampleno int, primary key(sampleno)) storage memory;
 insert into t(sampleno) select distinct sa.sampleno from adjthrsample as s
  right join adjthrsample_arch as sa on sa.threshid=s.threshid and sa.sampleno=s.sampleno
  where s.v1!=sa.v1 or s.v2!=sa.v2 or s.v1pk!=sa.v1pk or s.v2pk!=sa.v2pk or s.sampleno is null;
 delete from adjthrsample_arch
  using adjthrsample_arch
  join t on t.sampleno=adjthrsample_arch.sampleno;
 insert into adjthrsample_arch
  select s.* from adjthrsample as s
  join t on t.sampleno=s.sampleno;
 drop temporary table if exists t;
end//
DELIMITER ;

-- Dumping structure for procedure intravue.cull_event_archive
DROP PROCEDURE IF EXISTS `cull_event_archive`;
DELIMITER //
CREATE DEFINER=`netvue`@`localhost` PROCEDURE `cull_event_archive`()
begin
 drop temporary table if exists t;
 create temporary table t(eventid int, primary key(eventid)) storage memory;
 insert ignore into t select distinct ea.eventid
  from event as e
  right join event_arch as ea on ea.EventId=e.eventid
  where e.DescId!=ea.DescId or e.Occurred!=ea.Occurred or e.IpAddress!=ea.IpAddress or e.Description!=ea.Description
  or e.`Type`!=ea.`Type` or e.Class!=ea.Class or e.EventId is null
  order by e.eventid desc;
 delete from event_arch using event_arch
  join t on t.eventid=event_arch.EventId;
 insert into event_arch
  select e.* from event as e
  join t on t.eventid=e.EventId;
 drop temporary table if exists t;
end//
DELIMITER ;


-- Dumping structure for table intravue.desclink
DROP TABLE IF EXISTS `desclink`;
CREATE TABLE IF NOT EXISTS `desclink` (
  `LinkId` int(10) NOT NULL AUTO_INCREMENT,
  `Parent` int(10) NOT NULL DEFAULT '0',
  `Child` int(10) NOT NULL DEFAULT '0',
  PRIMARY KEY (`LinkId`),
  KEY `ParentIdx` (`Parent`),
  KEY `ChildIdx` (`Child`)
) ENGINE=InnoDB AUTO_INCREMENT=1193008 DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.descriptor
DROP TABLE IF EXISTS `descriptor`;
CREATE TABLE IF NOT EXISTS `descriptor` (
  `DescId` int(10) NOT NULL AUTO_INCREMENT,
  `NetworkId` int(10) NOT NULL DEFAULT '0',
  `Type` int(10) unsigned DEFAULT '0',
  `ScanStatus` int(10) unsigned DEFAULT '0',
  `ConnectionState` int(10) unsigned DEFAULT '0',
  `LastUpdate` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `DescType` enum('D','If','Port','??') DEFAULT '??',
  PRIMARY KEY (`DescId`),
  KEY `DescIdx` (`DescId`)
) ENGINE=InnoDB AUTO_INCREMENT=258635 DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.devicedesc
DROP TABLE IF EXISTS `devicedesc`;
CREATE TABLE IF NOT EXISTS `devicedesc` (
  `DeviceId` int(10) NOT NULL AUTO_INCREMENT,
  `DescId` int(11) NOT NULL DEFAULT '0',
  `Level` int(11) DEFAULT '-1',
  `NoOfIfs` int(11) DEFAULT '0',
  `DefaultGw` char(16) DEFAULT '0.0.0.0',
  `Uptime` int(10) unsigned DEFAULT '0',
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `Category` tinyint(4) DEFAULT '0',
  `AutoConnect` tinyint(4) DEFAULT '0',
  `VerifiedMove` tinyint(4) DEFAULT '0',
  `SendAlarms` tinyint(4) DEFAULT '0',
  `SendToDefaultUser` tinyint(4) DEFAULT '0',
  `AutoBOOTP` tinyint(4) DEFAULT '0',
  `IsWireless` tinyint(4) DEFAULT '0',
  `alarmemailaddress` varchar(128) DEFAULT NULL,
  `Verified` tinyint(4) DEFAULT '0',
  `ReadIndex` int(10) unsigned DEFAULT '0',
  `WriteIndex` int(10) unsigned DEFAULT '0',
  `LastUpdate` int(10) DEFAULT '0',
  `AlarmLevel` tinyint(4) DEFAULT '0',
  `ManuallyInserted` tinyint(4) DEFAULT '0',
  `AutoMoving` tinyint(4) DEFAULT '0',
  `IfScannerHost` tinyint(4) DEFAULT '0',
  `ParentIfPort` int(10) NOT NULL DEFAULT '0',
  `NewParentIfPort` int(10) NOT NULL DEFAULT '0',
  PRIMARY KEY (`DeviceId`),
  KEY `DescIdx` (`DescId`)
) ENGINE=InnoDB AUTO_INCREMENT=99800 DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.devicelist
DROP TABLE IF EXISTS `devicelist`;
CREATE TABLE IF NOT EXISTS `devicelist` (
  `assetid` int(11) NOT NULL AUTO_INCREMENT,
  `satellitedescid` int(11) NOT NULL DEFAULT '0',
  `satelliteip` char(16) DEFAULT '0.0.0.0',
  `descid` int(11) NOT NULL DEFAULT '0',
  `ipaddress` char(16) DEFAULT '0.0.0.0',
  `macaddress` char(20) DEFAULT '0.0.0.0',
  `name` varchar(255) DEFAULT NULL,
  `networkID` int(11) NOT NULL DEFAULT '0',
  `networkName` varchar(255) DEFAULT NULL,
  `netGroup` int(11) NOT NULL DEFAULT '0',
  `isSwitch` tinyint(4) NOT NULL DEFAULT '0',
  `verified` tinyint(4) NOT NULL DEFAULT '0',
  `critical` tinyint(4) NOT NULL DEFAULT '0',
  `isportopen` tinyint(4) NOT NULL DEFAULT '0',
  `location` varchar(255) DEFAULT NULL,
  `isGhost` tinyint(4) NOT NULL DEFAULT '0',
  `isWireless` tinyint(4) NOT NULL DEFAULT '0',
  `isOnline` tinyint(4) NOT NULL DEFAULT '0',
  `userDefined1` varchar(255) DEFAULT NULL,
  `userDefined2` varchar(255) DEFAULT NULL,
  `userDefined3` varchar(255) DEFAULT NULL,
  `revision` varchar(255) DEFAULT NULL,
  `vendor` varchar(255) DEFAULT NULL,
  `model` varchar(255) DEFAULT NULL,
  `lastupdate` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`assetid`),
  KEY `fk_satelliteip` (`satelliteip`),
  CONSTRAINT `fk_satelliteip` FOREIGN KEY (`satelliteip`) REFERENCES `supervisorconfig` (`ipaddress`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.deviceview
DROP TABLE IF EXISTS `deviceview`;
CREATE TABLE IF NOT EXISTS `deviceview` (
  `ViewId` int(11) NOT NULL DEFAULT '0',
  `DeviceId` int(10) NOT NULL DEFAULT '0',
  `viewname` varchar(255) NOT NULL DEFAULT '',
  `iconname` varchar(40) DEFAULT NULL,
  `thumbnailname` varchar(40) DEFAULT NULL,
  `weblink1name` varchar(40) DEFAULT NULL,
  `weblink1url` varchar(128) DEFAULT NULL,
  `weblink2name` varchar(40) DEFAULT NULL,
  `weblink2url` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`ViewId`,`DeviceId`),
  KEY `DevViewIdx` (`DeviceId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.event
DROP TABLE IF EXISTS `event`;
CREATE TABLE IF NOT EXISTS `event` (
  `EventId` int(10) NOT NULL AUTO_INCREMENT,
  `DescId` int(10) NOT NULL DEFAULT '0',
  `Occurred` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `IpAddress` char(16) DEFAULT '0.0.0.0',
  `Description` char(255) DEFAULT '',
  `Type` tinyint(4) DEFAULT NULL,
  `Class` int(11) NOT NULL DEFAULT '0',
  `eventtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`EventId`) USING BTREE,
  KEY `Class` (`Class`) USING BTREE
) ENGINE=MEMORY AUTO_INCREMENT=2643148 DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.eventidmax
DROP TABLE IF EXISTS `eventidmax`;
CREATE TABLE IF NOT EXISTS `eventidmax` (
  `emax` int(11) NOT NULL DEFAULT '0'
) ENGINE=MEMORY DEFAULT CHARSET=latin1;

-- Dumping data for table intravue.eventidmax: 1 rows
/*!40000 ALTER TABLE `eventidmax` DISABLE KEYS */;
INSERT INTO `eventidmax` (`emax`) VALUES
	(2643147);
/*!40000 ALTER TABLE `eventidmax` ENABLE KEYS */;


-- Dumping structure for table intravue.event_arch
DROP TABLE IF EXISTS `event_arch`;
CREATE TABLE IF NOT EXISTS `event_arch` (
  `EventId` int(10) NOT NULL DEFAULT '0',
  `DescId` int(10) NOT NULL DEFAULT '0',
  `Occurred` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `IpAddress` varchar(16) DEFAULT '0.0.0.0',
  `Description` varchar(255) DEFAULT '',
  `Type` tinyint(4) DEFAULT NULL,
  `Class` int(11) NOT NULL DEFAULT '0',
  `eventtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.ifdesc
DROP TABLE IF EXISTS `ifdesc`;
CREATE TABLE IF NOT EXISTS `ifdesc` (
  `IfId` int(10) NOT NULL AUTO_INCREMENT,
  `DescId` int(11) NOT NULL DEFAULT '0',
  `IpAddress` char(16) NOT NULL DEFAULT '0.0.0.0',
  `NetMask` char(16) NOT NULL DEFAULT '255.255.255.0',
  `MacAddress` char(20) NOT NULL DEFAULT '00 00 00 00 00 00',
  `IfIndex` int(10) unsigned DEFAULT '0',
  `IfToGw` tinyint(4) DEFAULT '0',
  `IfWasPinged` tinyint(4) DEFAULT '0',
  `Uptime` int(10) unsigned DEFAULT '0',
  `ReadIndex` int(10) unsigned DEFAULT '0',
  `WriteIndex` int(10) unsigned DEFAULT '0',
  `LastUpdate` int(10) DEFAULT '0',
  `AlarmStartDT` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `AlarmStartTime` int(10) unsigned DEFAULT '0',
  `PingTime` int(10) unsigned DEFAULT '0',
  `PingSampleNo` int(11) DEFAULT '0',
  `PingSampleTime` int(10) unsigned DEFAULT '0',
  PRIMARY KEY (`IfId`),
  KEY `DescIdx` (`DescId`)
) ENGINE=InnoDB AUTO_INCREMENT=99876 DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.job
DROP TABLE IF EXISTS `job`;
CREATE TABLE IF NOT EXISTS `job` (
  `JobNo` int(10) NOT NULL AUTO_INCREMENT,
  `Tpl` int(10) unsigned NOT NULL DEFAULT '0',
  `DescId` int(10) unsigned NOT NULL DEFAULT '0',
  `Period` int(10) unsigned NOT NULL DEFAULT '0',
  `NextExec` int(10) unsigned NOT NULL DEFAULT '0',
  `Flags` int(10) unsigned NOT NULL DEFAULT '0',
  `Created` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `Value` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`JobNo`),
  KEY `TplIdx` (`Tpl`),
  KEY `DescIdx` (`DescId`)
) ENGINE=InnoDB AUTO_INCREMENT=1078 DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.jobtemplate
DROP TABLE IF EXISTS `jobtemplate`;
CREATE TABLE IF NOT EXISTS `jobtemplate` (
  `TemplNo` int(10) NOT NULL AUTO_INCREMENT,
  `Tpl` int(10) NOT NULL DEFAULT '0',
  `MinPeriod` int(10) unsigned NOT NULL DEFAULT '0',
  `Type` int(10) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`TemplNo`),
  KEY `TplIdx` (`Tpl`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;

-- Dumping structure for procedure intravue.killSurplusEvents
DROP PROCEDURE IF EXISTS `killSurplusEvents`;
DELIMITER //
CREATE DEFINER=`netvue`@`localhost` PROCEDURE `killSurplusEvents`()
begin
 declare total_events int;
 select count(*) into total_events from event where class in (101,102,103);
 if total_events>100000 then
   drop table if exists kill_events;
   create temporary table kill_events select eventid from event where class in (101,102,103);
   alter table kill_events add primary key (eventid);
   delete from kill_events order by eventid desc limit 100000;
   select count(*) from kill_events;
   delete from event using event,kill_events where event.eventid=kill_events.eventid;
   drop table kill_events;
   insert into event(occurred,description,type) values (now(),"removed old threshold and disconnect events because database too large",0);
 end if;
 select count(*) into total_events from event where class >= 100;
 if total_events>150000 then
   drop table if exists kill_events;
   create temporary table kill_events storage memory select eventid from event where class >= 100;
   alter table kill_events add primary key (eventid);
   delete from kill_events order by eventid desc limit 150000;
   select count(*) from kill_events;
   delete from event using event,kill_events where event.eventid=kill_events.eventid;
   drop table kill_events;
   insert into event(occurred,description,type) values (now(),"removed old repetitive events because database too large",0);
 end if;
end//
DELIMITER ;

-- Dumping structure for table intravue.looplink
DROP TABLE IF EXISTS `looplink`;
CREATE TABLE IF NOT EXISTS `looplink` (
  `descid` int(11) NOT NULL DEFAULT '0',
  `port` int(11) NOT NULL DEFAULT '0',
  `descid2` int(11) NOT NULL DEFAULT '0',
  `port2` int(11) NOT NULL DEFAULT '0',
  `parent` int(11) NOT NULL DEFAULT '0',
  `ifdesc` varchar(255) DEFAULT NULL,
  `ifdesc2` varchar(255) DEFAULT NULL,
  `isloop` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`descid`,`port`),
  KEY `fk_descriptor2` (`descid2`),
  KEY `fk_descriptor3` (`parent`),
  CONSTRAINT `fk_descriptor` FOREIGN KEY (`descid`) REFERENCES `descriptor` (`DescId`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_descriptor2` FOREIGN KEY (`descid2`) REFERENCES `descriptor` (`DescId`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_descriptor3` FOREIGN KEY (`parent`) REFERENCES `descriptor` (`DescId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.modification
DROP TABLE IF EXISTS `modification`;
CREATE TABLE IF NOT EXISTS `modification` (
  `mod_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `command` char(255) NOT NULL DEFAULT '',
  `parent` int(10) unsigned DEFAULT NULL,
  `child` int(10) unsigned DEFAULT NULL,
  `mod_time` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`mod_id`)
) ENGINE=InnoDB AUTO_INCREMENT=115 DEFAULT CHARSET=latin1;

-- Dumping structure for procedure intravue.modify_character_set
DROP PROCEDURE IF EXISTS `modify_character_set`;
DELIMITER //
CREATE DEFINER=`netvue`@`localhost` PROCEDURE `modify_character_set`()
begin
  declare no_more_to_fetch boolean default false;
  declare alter_table_string varchar(1024) default '';
  declare alter_stmts_crsr cursor for
    select concat('alter table ', table_name, ' convert to character set latin1')
	from information_schema.tables
	where table_schema = 'intravue' and table_collation like 'utf8%';

  declare continue handler for not found
      set no_more_to_fetch = true;

  SET foreign_key_checks = 0;

  open alter_stmts_crsr;
  Result_Set: loop
    fetch alter_stmts_crsr into alter_table_string;
    if( no_more_to_fetch ) then
      leave Result_Set;
    end if;

    set @sql := alter_table_string;
    prepare stmt from @sql;
    execute stmt;
    drop prepare stmt;
  end loop Result_Set;
  close alter_stmts_crsr;

  SET foreign_key_checks = 1;
end//
DELIMITER ;


-- Dumping structure for table intravue.network
DROP TABLE IF EXISTS `network`;
CREATE TABLE IF NOT EXISTS `network` (
  `NwId` int(10) NOT NULL AUTO_INCREMENT,
  `NetworkId` int(10) NOT NULL DEFAULT '-1',
  `Name` char(40) NOT NULL DEFAULT '',
  `UseCount` int(11) NOT NULL DEFAULT '-1',
  `NetGroup` int(10) NOT NULL DEFAULT '0',
  `Agent` char(16) DEFAULT NULL,
  PRIMARY KEY (`NwId`),
  KEY `NwidIdx` (`NetworkId`)
) ENGINE=InnoDB AUTO_INCREMENT=248 DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.oidval
DROP TABLE IF EXISTS `oidval`;
CREATE TABLE IF NOT EXISTS `oidval` (
  `ip` char(15) NOT NULL,
  `oid` varchar(100) NOT NULL,
  `disporder` int(4) DEFAULT NULL,
  `text` char(40) DEFAULT NULL,
  `oidtype` varchar(30) DEFAULT NULL,
  `oidval` varchar(100) DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  PRIMARY KEY (`ip`,`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.portdesc
DROP TABLE IF EXISTS `portdesc`;
CREATE TABLE IF NOT EXISTS `portdesc` (
  `PortId` int(10) NOT NULL AUTO_INCREMENT,
  `DescId` int(11) NOT NULL DEFAULT '0',
  `Speed` int(10) unsigned DEFAULT '0',
  `Mode` int(11) DEFAULT '0',
  `IsUplinkPort` tinyint(4) DEFAULT '0',
  `IfIndex` int(10) unsigned DEFAULT '0',
  `PortNo` int(10) unsigned DEFAULT '0',
  `Uptime` int(10) unsigned DEFAULT '0',
  `ReadIndex` int(10) unsigned DEFAULT '0',
  `WriteIndex` int(10) unsigned DEFAULT '0',
  PRIMARY KEY (`PortId`),
  KEY `DescIdx` (`DescId`)
) ENGINE=InnoDB AUTO_INCREMENT=57282 DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.portdetails
DROP TABLE IF EXISTS `portdetails`;
CREATE TABLE IF NOT EXISTS `portdetails` (
  `descid` int(11) NOT NULL DEFAULT '0',
  `ifindex` int(11) NOT NULL DEFAULT '0',
  `ifdesc` varchar(255) DEFAULT NULL,
  `ifinerrors` bigint(20) DEFAULT NULL,
  `etherstatscrcalignerrors` bigint(20) DEFAULT NULL,
  `ifinerrorstime` int(11) DEFAULT NULL,
  `crcalignerrorstime` int(11) DEFAULT NULL,
  `ifinerrorsevent` bigint(20) DEFAULT NULL,
  `ifinerrorseventtime` int(11) DEFAULT NULL,
  `crcalignerrorsevent` bigint(20) DEFAULT NULL,
  `crcalignerrorseventtime` int(11) DEFAULT NULL,
  `ifinerrorsalert` tinyint(4) NOT NULL DEFAULT '0',
  `crcalignerrorsalert` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`descid`,`ifindex`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.properties
DROP TABLE IF EXISTS `properties`;
CREATE TABLE IF NOT EXISTS `properties` (
  `PropId` int(10) NOT NULL AUTO_INCREMENT,
  `RefType` enum('System','Network','Desc','ScanPort','Name','Protected') DEFAULT NULL,
  `RefId` int(10) NOT NULL DEFAULT '0',
  `refname` varchar(10) DEFAULT NULL,
  `name` varchar(30) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`PropId`),
  UNIQUE KEY `idx` (`RefType`,`RefId`,`refname`,`name`)
) ENGINE=InnoDB AUTO_INCREMENT=88347 DEFAULT CHARSET=latin1;

-- Dumping structure for procedure intravue.refineEventLog
DROP PROCEDURE IF EXISTS `refineEventLog`;
DELIMITER //
CREATE DEFINER=`netvue`@`localhost` PROCEDURE `refineEventLog`()
begin
 declare unclassified_events int;

 update event set class=0 where class in (99,108);
 select count(*) into unclassified_events from event where class = 0;
 if unclassified_events>0 then
  update event set class=1 where class=0 and description like 'Restored database%' ;
  update event set class=2 where class=0 and description like 'Scanner version%' ;
  update event set class=3 where class=0 and description like 'Registration%' ;
  update event set class=4 where class=0 and description like 'Device%joined network as top parent' ;
  update event set class=5 where class=0 and description like 'System configuration changes applied.' ;
  update event set class=6 where class=0 and description like 'Device%joined network' ;
  update event set class=7 where class=0 and description like 'Device%moved to subnetparent%' ;
  update event set class=9 where class=0 and description like 'SNMP supported on%' ;
  update event set class=13 where class=0 and description like 'Admin verified %' ;
  update event set class=17 where class=0 and description like 'Switch % reports mac %' ;
  update event set class=19 where class=0 and description like 'Device % changed mac from %' ;
  update event set class=20 where class=0 and description like 'Device % changed ip to %' ;
  update event set class=23 where class=0 and description like 'Deleted child node at %' ;
  update event set class=25 where class=0 and description like 'deleted node %' ;
  update event set class=26 where class=0 and description like 'Admin UN-verified %' ;
  update event set class=27 where class=0 and description like 'Automatic backup file created, %' ;
  update event set class=28 where class=0 and description like 'Automatic backup file deleted, %' ;
  update event set class=29 where class=0 and description like 'Adjusting scanrange to add top parent %' ;
  update event set class=30 where class=0 and description like 'Admin moved child node' ;
  update event set class=31 where class=0 and description like 'Scanner stopped' ;
  update event set class=32 where class=0 and description like 'deleted top parent' ;
  update event set class=33 where class=0 and description like 'added child node %' ;
  update event set class=33 where class=0 and description like 'deleted child node %' ;
  update event set class=34 where class=0 and description like 'merging device %' ;
  update event set class=35 where class=0 and description like 'Device % changed speed %' ;
  update event set class=36 where class=0 and description like 'Device % unverified because %' ;
  update event set class=37 where class=0 and description like 'merging device % into device %' ;
  update event set class=38 where class=0 and description like '% is not under a support contract or does not have devices configured%' ;
  update event set class=39 where class=0 and description like 'Satellite: % IP Address: % Added to supervisor configuration%' ;
  update event set class=40 where class=0 and description like 'Satellite: % IP Address: % Deleted from supervisor configuration%' ;
  update event set class=41 where class=0 and description like 'An error occurred while processing info for %' ;
  update event set class=42 where class=0 and description like '% has an unsupported kpi version.' ;
  update event set class=43 where class=0 and description like '% has an interim kpi version.' ;
  update event set class=44 where class=0 and description like 'Error connecting to:%' ;

  update event set class=101 where class=0 and description like 'Ping Response Threshold Exceeded' ;
  update event set class=101 where class=0 and description like 'Ping response threshold cleared' ;
  update event set class=102 where class=0 and description like 'Bandwidth threshold exceeded port %' ;
  update event set class=102 where class=0 and description like 'Bandwidth threshold cleared port %' ;
  update event set class=102 where class=0 and description like 'Bandwidth threshold exceeded' ;
  update event set class=102 where class=0 and description like 'Bandwidth threshold cleared' ;
  update event set class=103 where class=0 and description like 'Device % disconnected' ;
  update event set class=103 where class=0 and description like 'Device % reconnected' ;
  update event set class=104 where class=0 and description like 'Device % moved from %' ;
  update event set class=104 where class=0 and description like 'Device % moved to %' ;
  update event set class=105 where class=0 and description like 'SNMP lost on %' ;
  update event set class=105 where class=0 and description like 'SNMP returned on %' ;
  update event set class=106 where class=0 and description like 'Trap: %' ;
  update event set class=107 where class=0 and description like 'delete name of %' ;
  update event set class=107 where class=0 and description like 'change name of %' ;

  update event set class=109 where class=0 and description like 'Device % changed ENIP %' ;
  update event set class=110 where class=0 and description like 'Satellite: % IP Address: % Critical Uptime is at %' ;
  update event set class=111 where class=0 and description like 'Satellite: % IP Address: % Device Incidents is at %' ;
  update event set class=111 where class=0 and description like 'Satellite: % IP Address: % No Device Incidents%' ;
  update event set class=112 where class=0 and description like 'Satellite: % IP Address: % Switch Incidents is at %' ;
  update event set class=112 where class=0 and description like 'Satellite: % IP Address: % No Switch Incidents%' ;
  update event set class=113 where class=0 and description like 'LLDP: Device % moved %' ;
  update event set class=114 where class=0 and description like '% counter for IP % has % Current value%' ;

  update event set class=99 where class=0;
 end if;
end//
DELIMITER ;


-- Dumping structure for procedure intravue.restore_memory_tables
DROP PROCEDURE IF EXISTS `restore_memory_tables`;
DELIMITER //
CREATE DEFINER=`netvue`@`localhost` PROCEDURE `restore_memory_tables`()
begin
 start transaction;
 truncate eventidmax;
 truncate event;
 insert ignore into event select * from event_arch;
 select @eventmax:=max(eventid) from event;
 insert into eventidmax(emax) values (@eventmax);
 commit;

 start transaction;
 truncate adjthrsample;
 insert ignore into adjthrsample select * from adjthrsample_arch;
 commit;
end//
DELIMITER ;


-- Dumping structure for table intravue.satelliteinfo
DROP TABLE IF EXISTS `satelliteinfo`;
CREATE TABLE IF NOT EXISTS `satelliteinfo` (
  `ipaddress` char(16) NOT NULL DEFAULT '0.0.0.0',
  `type` int(11) NOT NULL DEFAULT '0',
  `value` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`ipaddress`,`type`),
  CONSTRAINT `fk_supervisorconfig` FOREIGN KEY (`ipaddress`) REFERENCES `supervisorconfig` (`ipaddress`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.scanport
DROP TABLE IF EXISTS `scanport`;
CREATE TABLE IF NOT EXISTS `scanport` (
  `ScanId` int(10) NOT NULL AUTO_INCREMENT,
  `DescId` int(10) NOT NULL DEFAULT '0',
  `Protocol` enum('SNMP','HTTP','FTP','Telnet') DEFAULT 'SNMP',
  `PortNo` int(11) DEFAULT '0',
  `PortNoChanged` tinyint(4) DEFAULT '0',
  `IsPortOpen` tinyint(4) DEFAULT '0',
  `Flags` int(11) DEFAULT '0',
  PRIMARY KEY (`ScanId`),
  KEY `DescIdx` (`DescId`),
  KEY `PortIdx` (`PortNo`)
) ENGINE=InnoDB AUTO_INCREMENT=399437 DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.scanrange
DROP TABLE IF EXISTS `scanrange`;
CREATE TABLE IF NOT EXISTS `scanrange` (
  `NetworkId` int(10) NOT NULL DEFAULT '0',
  `IpAddressFrom` char(16) NOT NULL DEFAULT '0.0.0.0',
  `IpAddressTo` char(16) NOT NULL DEFAULT '0.0.0.0',
  `TopParent` char(16) NOT NULL DEFAULT '0.0.0.0',
  `NetMask` char(16) NOT NULL DEFAULT '255.255.255.0',
  `IsNew` tinyint(4) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.strings
DROP TABLE IF EXISTS `strings`;
CREATE TABLE IF NOT EXISTS `strings` (
  `rowindex` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `string` char(255) DEFAULT NULL,
  UNIQUE KEY `Key_rowindex` (`rowindex`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.supervisorconfig
DROP TABLE IF EXISTS `supervisorconfig`;
CREATE TABLE IF NOT EXISTS `supervisorconfig` (
  `ipaddress` char(16) NOT NULL DEFAULT '0.0.0.0',
  `name` varchar(64) DEFAULT NULL,
  `yellowuptimethr` int(11) DEFAULT NULL,
  `orangeuptimethr` int(11) DEFAULT NULL,
  `reduptimethr` int(11) DEFAULT NULL,
  `uptimenotif` int(11) DEFAULT NULL,
  `uptimenotifaddr` varchar(255) DEFAULT NULL,
  `yellowdevicethr` int(11) DEFAULT NULL,
  `orangedevicethr` int(11) DEFAULT NULL,
  `reddevicethr` int(11) DEFAULT NULL,
  `devicenotif` int(11) DEFAULT NULL,
  `devicenotifaddr` varchar(255) DEFAULT NULL,
  `yellowswitchthr` int(11) DEFAULT NULL,
  `orangeswitchthr` int(11) DEFAULT NULL,
  `redswitchthr` int(11) DEFAULT NULL,
  `switchnotif` int(11) DEFAULT NULL,
  `switchnotifaddr` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ipaddress`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Dumping data for table intravue.supervisorconfig: ~0 rows (approximately)
/*!40000 ALTER TABLE `supervisorconfig` DISABLE KEYS */;
/*!40000 ALTER TABLE `supervisorconfig` ENABLE KEYS */;


-- Dumping structure for table intravue.supervisordata
DROP TABLE IF EXISTS `supervisordata`;
CREATE TABLE IF NOT EXISTS `supervisordata` (
  `ipaddress` char(16) NOT NULL,
  `sampleno` int(11) NOT NULL DEFAULT '0',
  `valtype` int(11) NOT NULL DEFAULT '0',
  `period` int(11) NOT NULL DEFAULT '0',
  `network` int(11) NOT NULL DEFAULT '0',
  `endtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `curuptime` float NOT NULL DEFAULT '0',
  `avguptime` float DEFAULT NULL,
  `maxuptime` float DEFAULT NULL,
  `minuptime` float DEFAULT NULL,
  `sampatmaxuptime` int(11) DEFAULT NULL,
  `sampatminuptime` int(11) DEFAULT NULL,
  `dateatmaxuptime` datetime DEFAULT NULL,
  `dateatminuptime` datetime DEFAULT NULL,
  `curswev` int(11) NOT NULL DEFAULT '0',
  `avgswev` float DEFAULT NULL,
  `maxswev` int(11) DEFAULT NULL,
  `minswev` int(11) DEFAULT NULL,
  `sampatmaxswev` int(11) DEFAULT NULL,
  `sampatminswev` int(11) DEFAULT NULL,
  `dateatmaxswev` datetime DEFAULT NULL,
  `dateatminswev` datetime DEFAULT NULL,
  `curdevev` int(11) NOT NULL DEFAULT '0',
  `avgdevev` float DEFAULT NULL,
  `maxdevev` int(11) DEFAULT NULL,
  `mindevev` int(11) DEFAULT NULL,
  `sampatmaxdevev` int(11) DEFAULT NULL,
  `sampatmindevev` int(11) DEFAULT NULL,
  `dateatmaxdevev` datetime DEFAULT NULL,
  `dateatmindevev` datetime DEFAULT NULL,
  `critDevCount` int(11) NOT NULL DEFAULT '0',
  `critDevCountDev` int(11) NOT NULL DEFAULT '0',
  `critDevCountSw` int(11) NOT NULL DEFAULT '0',
  `nonVerified` int(11) NOT NULL DEFAULT '0',
  `unknownCriticalStatus` int(11) NOT NULL DEFAULT '0',
  `numberofdevices` int(11) NOT NULL DEFAULT '0',
  `timestampatmaxuptime` int(11) NOT NULL DEFAULT '0',
  `timestampatminuptime` int(11) NOT NULL DEFAULT '0',
  `timestampatmaxswev` int(11) NOT NULL DEFAULT '0',
  `timestampatminswev` int(11) NOT NULL DEFAULT '0',
  `timestampatmaxdevev` int(11) NOT NULL DEFAULT '0',
  `timestampatmindevev` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ipaddress`,`sampleno`,`valtype`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.supervisoremailstatus
DROP TABLE IF EXISTS `supervisoremailstatus`;
CREATE TABLE IF NOT EXISTS `supervisoremailstatus` (
  `RefId` int(10) NOT NULL DEFAULT '0',
  `name` varchar(30) NOT NULL DEFAULT '',
  `value` varchar(2) DEFAULT NULL,
  PRIMARY KEY (`RefId`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.threshold
DROP TABLE IF EXISTS `threshold`;
CREATE TABLE IF NOT EXISTS `threshold` (
  `ThreshId` int(10) NOT NULL AUTO_INCREMENT,
  `DescId` int(10) NOT NULL DEFAULT '0',
  `JobNo` int(10) NOT NULL DEFAULT '0',
  `ExecCount` int(10) unsigned DEFAULT '0',
  `Enabled` tinyint(4) DEFAULT '0',
  `ThresholdExceeded` tinyint(4) DEFAULT '0',
  `LastIn` int(10) unsigned DEFAULT '0',
  `LastOut` int(10) unsigned DEFAULT '0',
  `LastExecDT` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `LastExecTime` int(10) unsigned DEFAULT '0',
  `Average` double DEFAULT '0',
  `LastSampleNo` int(11) DEFAULT '-1',
  `AlarmDT` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `AlarmTime` int(10) unsigned DEFAULT '0',
  PRIMARY KEY (`ThreshId`),
  KEY `DescIdx` (`DescId`),
  KEY `JobIdx` (`JobNo`)
) ENGINE=InnoDB AUTO_INCREMENT=1076 DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.thresholdsample
DROP TABLE IF EXISTS `thresholdsample`;
CREATE TABLE IF NOT EXISTS `thresholdsample` (
  `SampleId` int(10) NOT NULL AUTO_INCREMENT,
  `ThreshId` int(10) NOT NULL DEFAULT '0',
  `SampleNo` int(11) DEFAULT '0',
  `Value` double DEFAULT '0',
  `SampleDT` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `SampleTime` int(10) unsigned DEFAULT '0',
  PRIMARY KEY (`SampleId`),
  KEY `SampleIdx` (`SampleId`),
  KEY `ThreshIdx` (`ThreshId`),
  KEY `ValueIdx` (`Value`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Dumping structure for procedure intravue.update_adjthrsample_archive
DROP PROCEDURE IF EXISTS `update_adjthrsample_archive`;
DELIMITER //
CREATE DEFINER=`netvue`@`localhost` PROCEDURE `update_adjthrsample_archive`(sno int)
begin
 insert into adjthrsample_arch select * from adjthrsample where sampleno = sno;
end//
DELIMITER ;


-- Dumping structure for procedure intravue.update_event_archive
DROP PROCEDURE IF EXISTS `update_event_archive`;
DELIMITER //
CREATE DEFINER=`netvue`@`localhost` PROCEDURE `update_event_archive`()
begin
 start transaction;
 select @eventmax:=emax from eventidmax limit 1;
 insert into event_arch select event.* from event where eventid > @eventmax;
 select @eventmax:=max(eventid) from event;
 update eventidmax set emax = @eventmax;
 commit;
end//
DELIMITER ;

-- Dumping structure for table intravue.xdata
DROP TABLE IF EXISTS `xdata`;
CREATE TABLE IF NOT EXISTS `xdata` (
  `name` varchar(30) NOT NULL DEFAULT '',
  `descid` int(11) NOT NULL,
  `sampleno` int(11) NOT NULL DEFAULT '0',
  `data` mediumtext,
  PRIMARY KEY (`name`,`descid`,`sampleno`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Dumping structure for table intravue.zzmarker
DROP TABLE IF EXISTS `zzmarker`;
CREATE TABLE IF NOT EXISTS `zzmarker` (
  `dummy` int(11) NOT NULL,
  PRIMARY KEY (`dummy`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

