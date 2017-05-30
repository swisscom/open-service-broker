-- MySQL dump 10.13  Distrib 5.6.24, for osx10.8 (x86_64)
--
-- Host: 127.0.0.1    Database: CFBroker
-- ------------------------------------------------------
-- Server version	5.6.26

/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS = @@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION = @@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE = @@TIME_ZONE */;
/*!40103 SET TIME_ZONE = '+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES = @@SQL_NOTES, SQL_NOTES = 0 */;

--
-- Table structure for table `backup`
--


/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `backup` (
  `id`                    BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `date_requested`        DATETIME     NOT NULL,
  `date_updated`          DATETIME              DEFAULT NULL,
  `external_id`           VARCHAR(255)          DEFAULT NULL,
  `guid`                  VARCHAR(255)          DEFAULT NULL,
  `operation`             VARCHAR(255) NOT NULL,
  `plan_id`               INT(11)               DEFAULT NULL,
  `service_id`            INT(11)               DEFAULT NULL,
  `service_instance_guid` VARCHAR(255)          DEFAULT NULL,
  `status`                VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_c8tfw8w6wjnqxtsuweyphhbd` (`external_id`),
  UNIQUE KEY `UK_n3n3pailar84yx97i5vet6bdg` (`guid`),
  KEY `FK_47ta7ihln5oi8ai5px8sivhff` (`plan_id`),
  KEY `FK_p6rt93rkb5qwh28prqti012nq` (`service_id`),
  CONSTRAINT `FK_47ta7ihln5oi8ai5px8sivhff` FOREIGN KEY (`plan_id`) REFERENCES `plan` (`id`),
  CONSTRAINT `FK_p6rt93rkb5qwh28prqti012nq` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `deprovision_request`
--


/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `deprovision_request` (
  `id`                    BIGINT(20) NOT NULL AUTO_INCREMENT,
  `accepts_incomplete`    BIT(1)     NOT NULL,
  `service_instance_id`   BIGINT(20)          DEFAULT NULL,
  `service_instance_guid` VARCHAR(255)        DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_smbgucibdje5gkhsvu5105lxn` (`service_instance_guid`),
  KEY `FK_op0vfe0sctrn990l3d80o6iys` (`service_instance_id`),
  CONSTRAINT `FK_op0vfe0sctrn990l3d80o6iys` FOREIGN KEY (`service_instance_id`) REFERENCES `service_instance` (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `last_operation`
--


/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `last_operation` (
  `id`             BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `date_creation`  DATETIME     NOT NULL,
  `description`    VARCHAR(255)          DEFAULT NULL,
  `guid`           VARCHAR(255)          DEFAULT NULL,
  `internal_state` VARCHAR(255)          DEFAULT NULL,
  `operation`      VARCHAR(255) NOT NULL,
  `status`         VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_nde2eypvcex7mybbjgjjwcff6` (`guid`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `parameter`
--


/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `parameter` (
  `id`          BIGINT(20) NOT NULL AUTO_INCREMENT,
  `description` VARCHAR(255)        DEFAULT NULL,
  `name`        VARCHAR(255)        DEFAULT NULL,
  `plan_id`     INT(11)             DEFAULT NULL,
  `template`    VARCHAR(255)        DEFAULT NULL,
  `value`       VARCHAR(255)        DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_no67ias3hx8b31buboclaej8l` (`plan_id`),
  CONSTRAINT `FK_no67ias3hx8b31buboclaej8l` FOREIGN KEY (`plan_id`) REFERENCES `plan` (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `plan`
--


/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `plan` (
  `id`                         INT(11) NOT NULL AUTO_INCREMENT,
  `async_required`             BIT(1)           DEFAULT NULL,
  `description`                VARCHAR(255)     DEFAULT NULL,
  `display_index`              INT(11) NOT NULL DEFAULT '0',
  `free`                       BIT(1)           DEFAULT NULL,
  `guid`                       VARCHAR(255)     DEFAULT NULL,
  `internal_name`              VARCHAR(255)     DEFAULT NULL,
  `max_backups`                INT(11)          DEFAULT '0',
  `name`                       VARCHAR(255)     DEFAULT NULL,
  `service_id`                 INT(11)          DEFAULT NULL,
  `template_unique_identifier` VARCHAR(255)     DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ooi1e8un82clmmkjhho1nl68p` (`guid`),
  KEY `FK_8kh5rrk9gv5n0xb9vbe1rhmt1` (`service_id`),
  CONSTRAINT `FK_8kh5rrk9gv5n0xb9vbe1rhmt1` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `plan_metadata`
--


/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `plan_metadata` (
  `id`      BIGINT(20) NOT NULL AUTO_INCREMENT,
  `_key`    VARCHAR(255)        DEFAULT NULL,
  `plan_id` INT(11)             DEFAULT NULL,
  `_type`   VARCHAR(255)        DEFAULT 'String',
  `_value`  TEXT,
  PRIMARY KEY (`id`),
  KEY `FK_kkkbmx3vof738nkybcail2r35` (`plan_id`),
  CONSTRAINT `FK_kkkbmx3vof738nkybcail2r35` FOREIGN KEY (`plan_id`) REFERENCES `plan` (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `provision_request`
--


/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `provision_request` (
  `id`                    BIGINT(20) NOT NULL AUTO_INCREMENT,
  `accepts_incomplete`    BIT(1)     NOT NULL,
  `organization_guid`     VARCHAR(255)        DEFAULT NULL,
  `parameters`            VARCHAR(255)        DEFAULT NULL,
  `plan_id`               INT(11)             DEFAULT NULL,
  `service_instance_guid` VARCHAR(255)        DEFAULT NULL,
  `space_guid`            VARCHAR(255)        DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_1nx7nsgewvf3h4ojbhp0539j5` (`service_instance_guid`),
  KEY `FK_7owj9gh7bhc8vmix1qu22lcs8` (`plan_id`),
  CONSTRAINT `FK_7owj9gh7bhc8vmix1qu22lcs8` FOREIGN KEY (`plan_id`) REFERENCES `plan` (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `restore`
--


/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `restore` (
  `id`             BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `backup_id`      BIGINT(20)            DEFAULT NULL,
  `date_requested` DATETIME     NOT NULL,
  `date_updated`   DATETIME              DEFAULT NULL,
  `external_id`    VARCHAR(255)          DEFAULT NULL,
  `guid`           VARCHAR(255) NOT NULL,
  `status`         VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_p2gw37y6m3p8chfcjeib17j8d` (`guid`),
  UNIQUE KEY `UK_4ox2p95n2exypo0yqnnecpnm5` (`external_id`),
  KEY `FK_27plt4y8ldx99409mh84as95h` (`backup_id`),
  CONSTRAINT `FK_27plt4y8ldx99409mh84as95h` FOREIGN KEY (`backup_id`) REFERENCES `backup` (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service`
--


/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `service` (
  `id`                            INT(11) NOT NULL AUTO_INCREMENT,
  `async_required`                BIT(1)           DEFAULT NULL,
  `bindable`                      BIT(1)           DEFAULT NULL,
  `dashboard_client_id`           VARCHAR(255)     DEFAULT NULL,
  `dashboard_client_redirect_uri` VARCHAR(255)     DEFAULT NULL,
  `dashboard_client_secret`       VARCHAR(255)     DEFAULT NULL,
  `description`                   VARCHAR(255)     DEFAULT NULL,
  `display_index`                 INT(11) NOT NULL DEFAULT '0',
  `guid`                          VARCHAR(255)     DEFAULT NULL,
  `internal_name`                 VARCHAR(255)     DEFAULT NULL,
  `name`                          VARCHAR(255)     DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_1g6gg5ywpt6a7c9hfgwdfupdv` (`guid`),
  UNIQUE KEY `UK_adgojnrwwx9c3y3qa2q08uuqp` (`name`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service_binding`
--


/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `service_binding` (
  `id`                  BIGINT(20) NOT NULL AUTO_INCREMENT,
  `credentials`         LONGTEXT,
  `guid`                VARCHAR(255)        DEFAULT NULL,
  `service_instance_id` BIGINT(20)          DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ek2crlvhawpsavck84drcw1ox` (`guid`),
  KEY `FK_gwcsha18h535wcxe9vto57gqy` (`service_instance_id`),
  CONSTRAINT `FK_gwcsha18h535wcxe9vto57gqy` FOREIGN KEY (`service_instance_id`) REFERENCES `service_instance` (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service_binding_service_detail`
--


/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `service_binding_service_detail` (
  `service_binding_details_id` BIGINT(20) DEFAULT NULL,
  `service_detail_id`          BIGINT(20) DEFAULT NULL,
  KEY `FK_k81wb0ppvdsqkf3aop6tgkmt4` (`service_detail_id`),
  KEY `FK_gcnl9ovguw9yw7dnywdk11n3u` (`service_binding_details_id`),
  CONSTRAINT `FK_gcnl9ovguw9yw7dnywdk11n3u` FOREIGN KEY (`service_binding_details_id`) REFERENCES `service_binding` (`id`),
  CONSTRAINT `FK_k81wb0ppvdsqkf3aop6tgkmt4` FOREIGN KEY (`service_detail_id`) REFERENCES `service_detail` (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service_detail`
--


/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `service_detail` (
  `id`         BIGINT(20) NOT NULL AUTO_INCREMENT,
  `_key`       VARCHAR(255)        DEFAULT NULL,
  `_type`      VARCHAR(255)        DEFAULT NULL,
  `unique_key` BIT(1)     NOT NULL,
  `_value`     VARCHAR(255)        DEFAULT NULL,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service_instance`
--


/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `service_instance` (
  `id`           BIGINT(20) NOT NULL AUTO_INCREMENT,
  `completed`    BIT(1)     NOT NULL DEFAULT b'1',
  `date_created` DATETIME   NOT NULL,
  `deleted`      BIT(1)     NOT NULL,
  `guid`         VARCHAR(255)        DEFAULT NULL,
  `org`          VARCHAR(255)        DEFAULT NULL,
  `plan_id`      INT(11)             DEFAULT NULL,
  `space`        VARCHAR(255)        DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_akblq73put3eowo0lnbsrkadn` (`guid`),
  KEY `FK_6ky4whewrv73u4i4rga8d2ydh` (`plan_id`),
  CONSTRAINT `FK_6ky4whewrv73u4i4rga8d2ydh` FOREIGN KEY (`plan_id`) REFERENCES `plan` (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service_instance_service_detail`
--


/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `service_instance_service_detail` (
  `service_instance_details_id` BIGINT(20) DEFAULT NULL,
  `service_detail_id`           BIGINT(20) DEFAULT NULL,
  KEY `FK_qgp0hat1h92s1rrkfdi3394rh` (`service_detail_id`),
  KEY `FK_j6s49sqxtltuvfrpeklyojco5` (`service_instance_details_id`),
  CONSTRAINT `FK_j6s49sqxtltuvfrpeklyojco5` FOREIGN KEY (`service_instance_details_id`) REFERENCES `service_instance` (`id`),
  CONSTRAINT `FK_qgp0hat1h92s1rrkfdi3394rh` FOREIGN KEY (`service_detail_id`) REFERENCES `service_detail` (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service_metadata`
--


/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `service_metadata` (
  `id`         BIGINT(20) NOT NULL AUTO_INCREMENT,
  `_key`       VARCHAR(255)        DEFAULT NULL,
  `service_id` INT(11)             DEFAULT NULL,
  `_type`      VARCHAR(255)        DEFAULT 'String',
  `_value`     TEXT,
  PRIMARY KEY (`id`),
  KEY `FK_jlhnuxkt63wwp9j7pis0thayj` (`service_id`),
  CONSTRAINT `FK_jlhnuxkt63wwp9j7pis0thayj` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service_permission`
--


/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `service_permission` (
  `id`            BIGINT(20) NOT NULL AUTO_INCREMENT,
  `cf_service_id` INT(11)             DEFAULT NULL,
  `permission`    VARCHAR(255)        DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_nsuobowki55y1mb3ynjn1hk1e` (`cf_service_id`),
  CONSTRAINT `FK_nsuobowki55y1mb3ynjn1hk1e` FOREIGN KEY (`cf_service_id`) REFERENCES `service` (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tag`
--


/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `tag` (
  `id`            BIGINT(20) NOT NULL AUTO_INCREMENT,
  `cf_service_id` INT(11)             DEFAULT NULL,
  `tag`           VARCHAR(255)        DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_jvicn6elucwiedn29xjv13w7o` (`cf_service_id`),
  CONSTRAINT `FK_jvicn6elucwiedn29xjv13w7o` FOREIGN KEY (`cf_service_id`) REFERENCES `service` (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE = @OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE = @OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS = @OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION = @OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES = @OLD_SQL_NOTES */;

-- Dump completed
