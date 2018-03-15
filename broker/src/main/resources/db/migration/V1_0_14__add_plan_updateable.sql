DROP PROCEDURE IF EXISTS add_planupdateable_to_service_table;
DELIMITER //

CREATE PROCEDURE add_planupdateable_to_service_table()
  BEGIN

    SET @col_exists = 1;
    SELECT 0
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'service'
          AND column_name = 'plan_updateable'
          AND table_schema = database()
    INTO @col_exists;

    IF @col_exists = 1 THEN
      ALTER TABLE service
        ADD COLUMN plan_updateable tinyint(1) default 0;
    END IF;

  END//

DELIMITER ;
CALL add_planupdateable_to_service_table();


DROP PROCEDURE IF EXISTS add_updaterequest_table;
DELIMITER //

CREATE PROCEDURE add_updaterequest_table()
  BEGIN

    /*!40101 SET @saved_cs_client = @@character_set_client */;
    /*!40101 SET character_set_client = utf8 */;
    CREATE TABLE IF NOT EXISTS `update_request` (
      `id`                    BIGINT(20)         NOT NULL AUTO_INCREMENT,
      `accepts_incomplete`    BIT(1)             NOT NULL,
      `service_instance_guid` VARCHAR(36)        DEFAULT NULL,
      `plan_id`               INT(11)            NOT NULL,
      `previous_plan_id`      INT(11)            NOT NULL,
      `parameters`            VARCHAR(255)       DEFAULT NULL,
	    `date_created`          DATETIME           NOT NULL,
      PRIMARY KEY (`id`),
      CONSTRAINT `FK_plan` FOREIGN KEY (`plan_id`) REFERENCES `plan` (`id`),
      CONSTRAINT `FK_previous_plan` FOREIGN KEY (`previous_plan_id`) REFERENCES `plan` (`id`)
    )
      ENGINE = InnoDB
      DEFAULT CHARSET = utf8;
    /*!40101 SET character_set_client = @saved_cs_client */;

  END//

DELIMITER ;
CALL add_updaterequest_table();


DROP PROCEDURE IF EXISTS update_parameters_on_tables;
DELIMITER //

CREATE PROCEDURE update_parameters_on_tables()
  BEGIN

  ALTER TABLE service_instance
        ADD COLUMN parameters TEXT;
  ALTER TABLE service_binding
        ADD COLUMN parameters TEXT;
  ALTER TABLE provision_request
        MODIFY COLUMN parameters TEXT;
  ALTER TABLE provision_request
        MODIFY COLUMN parameters TEXT;


  END//

DELIMITER ;
CALL update_parameters_on_tables();

