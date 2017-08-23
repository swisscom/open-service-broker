DROP PROCEDURE IF EXISTS create_template_version_for_plan;
DELIMITER //

CREATE PROCEDURE create_template_version_for_plan()
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
CALL create_template_version_for_plan();