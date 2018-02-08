SET @col_exists = 0;
SELECT 1
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'last_operation'
      AND column_name = 'status'
      AND table_schema = database()
INTO @col_exists;

SET @stmt = CASE @col_exists
            WHEN 1
              THEN CONCAT(
                  'UPDATE last_operation'
                  , ' SET status=''SUCCESS'''
                  , ' WHERE status=''SUCCEESS'''
                  , ';')
            ELSE 'select ''column does not exist, no op'''
            END;

PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
