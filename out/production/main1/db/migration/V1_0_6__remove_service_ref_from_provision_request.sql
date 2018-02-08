DROP PROCEDURE IF EXISTS normalize_provision_request;

DELIMITER //

CREATE PROCEDURE normalize_provision_request()
  BEGIN

    SET @col_exists = 0;
    SELECT 1
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'provision_request'
          AND column_name = 'service_id'
          AND table_schema = database()
    INTO @col_exists;

    IF @col_exists = 1
    THEN
      ALTER TABLE provision_request
        DROP FOREIGN KEY FK_44ouay4bvmd924qs9rx6wa3wg;
      ALTER TABLE provision_request
        DROP COLUMN service_id;
    END IF;

  END//

CALL normalize_provision_request()