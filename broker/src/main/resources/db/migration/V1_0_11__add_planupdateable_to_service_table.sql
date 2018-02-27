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