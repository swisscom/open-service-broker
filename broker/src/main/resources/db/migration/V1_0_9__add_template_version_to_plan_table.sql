DROP PROCEDURE IF EXISTS add_template_version_to_plan_table;
DELIMITER //

CREATE PROCEDURE add_template_version_to_plan_table()
  BEGIN

    SET @col_exists = 1;
    SELECT 0
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'plan'
          AND column_name = 'template_version'
          AND table_schema = database()
    INTO @col_exists;

    IF @col_exists = 1 THEN
      ALTER TABLE plan
        ADD COLUMN template_version VARCHAR(255);
    END IF;

  END//

DELIMITER ;
CALL add_template_version_to_plan_table();