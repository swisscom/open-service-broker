DROP PROCEDURE IF EXISTS add_service_provider_to_service_table;
DELIMITER //

CREATE PROCEDURE add_service_provider_to_service_table()
  BEGIN

    SET @col_exists_in_service = 1;
    SELECT 0
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'service'
          AND column_name = 'service_provider_class'
          AND table_schema = database()
    INTO @col_exists_in_service;

    IF @col_exists_in_service = 1 THEN
      ALTER TABLE service
        ADD service_provider_class VARCHAR(255) DEFAULT NULL;
    END IF;

    SET @col_exists_in_plan = 1;
    SELECT 0
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'plan'
          AND column_name = 'service_provider_class'
          AND table_schema = database()
    INTO @col_exists_in_plan;

    IF @col_exists_in_plan = 1 THEN
      ALTER TABLE plan
        ADD service_provider_class VARCHAR(255) DEFAULT NULL;
    END IF;

  END//

DELIMITER ;
CALL add_service_provider_to_service_table();