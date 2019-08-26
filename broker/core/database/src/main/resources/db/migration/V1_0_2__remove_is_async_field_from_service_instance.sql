SET @col_exists = 0;
SELECT 1
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'service_instance'
      AND column_name = 'is_async'
      AND table_schema = database()
INTO @col_exists;

SET @stmt = CASE @col_exists
            WHEN 1
              THEN CONCAT(
                  'ALTER TABLE service_instance'
                  , ' DROP COLUMN is_async'
                  , ';')
            ELSE 'select ''column does not exist, no op'''
            END;

PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


