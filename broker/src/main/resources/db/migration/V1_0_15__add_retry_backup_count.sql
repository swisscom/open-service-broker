DROP PROCEDURE IF EXISTS add_retry_backup_count_to_backup_table;
DELIMITER //

CREATE PROCEDURE add_retry_backup_count_to_backup_table()
  BEGIN

    SET @col_exists = 1;
    SELECT 0
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'backup'
          AND column_name = 'retry_backup_count'
          AND table_schema = database()
    INTO @col_exists;

    IF @col_exists = 1 THEN
      ALTER TABLE backup
        ADD COLUMN  retry_backup_count tinyint(1) default 0 not NULL;
    END IF;

  END//

DELIMITER ;
CALL add_retry_backup_count_to_backup_table();