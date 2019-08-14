DROP PROCEDURE IF EXISTS normalize_service_instance;
DELIMITER //

CREATE PROCEDURE normalize_service_instance()
  BEGIN

    SET @col_exists = 0;
    SELECT 1
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'service_instance'
          AND column_name = 'service_id'
          AND table_schema = database()
    INTO @col_exists;

    IF @col_exists = 1
    THEN
      ALTER TABLE service_instance
        DROP FOREIGN KEY FK_c2o824f4t90lp1v2df5dxp2r8;
      ALTER TABLE service_instance
        DROP COLUMN service_id;
    END IF;

  END//

CALL normalize_service_instance()