DROP PROCEDURE IF EXISTS create_table_service_context;
DELIMITER //

CREATE PROCEDURE create_table_service_context()
  BEGIN
    CREATE TABLE IF NOT EXISTS service_context
    (
      id       BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
      platform VARCHAR(255) NOT NULL
    );

    CREATE TABLE IF NOT EXISTS service_context_detail
    (
      id                 BIGINT                      NOT NULL AUTO_INCREMENT PRIMARY KEY,
      `_key`             VARCHAR(255)                NOT NULL,
      `_value`           VARCHAR(255) DEFAULT 'NULL' NULL,
      service_context_id BIGINT                      NOT NULL,
      CONSTRAINT FK_service_context_detail2context
      FOREIGN KEY (service_context_id) REFERENCES service_context (id)
    );

    SET @index_exists = 1;
    SELECT 0 FROM INFORMATION_SCHEMA.STATISTICS
    WHERE table_schema = database()
          AND table_name = 'service_context'
          AND index_name = 'idx_service_context_platform'
    INTO @index_exists;

    IF @index_exists = 1 THEN
      CREATE INDEX idx_service_context_platform
        ON service_context (`platform`);
    END IF;

    SET @index_exists = 1;
    SELECT 0 FROM INFORMATION_SCHEMA.STATISTICS
    WHERE table_schema = database()
          AND table_name = 'service_context_detail'
          AND index_name = 'idx_service_context_detail_value'
    INTO @index_exists;

    IF @index_exists = 1 THEN
      CREATE INDEX idx_service_context_detail_value
        ON service_context_detail (`_value`);
    END IF;


    SET @index_exists = 1;
    SELECT 0 FROM INFORMATION_SCHEMA.STATISTICS
    WHERE table_schema = database()
          AND table_name = 'service_context_detail'
          AND index_name = 'idx_service_context_detail_key'
    INTO @index_exists;

    IF @index_exists = 1 THEN
      CREATE INDEX idx_service_context_detail_key
        ON service_context_detail (`_key`);
    END IF;


    SET @index_exists = 1;
    SELECT DISTINCT 0 FROM INFORMATION_SCHEMA.STATISTICS
    WHERE table_schema = database()
          AND table_name = 'service_context_detail'
          AND index_name = 'idx_service_context_detail_key_value'
    INTO @index_exists;

    IF @index_exists = 1 THEN
      CREATE INDEX idx_service_context_detail_key_value
        ON service_context_detail (`_key`, `_value`);
    END IF;

    SET @col_exists = 1;
    SELECT 0
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'service_instance'
          AND column_name = 'service_context_id'
          AND table_schema = database()
    INTO @col_exists;

    IF @col_exists = 1 THEN
      ALTER TABLE service_instance
        ADD COLUMN service_context_id BIGINT;
      ALTER TABLE service_instance
        ADD CONSTRAINT FK_service_instance2service_context
      FOREIGN KEY (service_context_id) REFERENCES service_context (id);
    END IF;

    SET @col_exists = 1;
    SELECT 0
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'service_binding'
          AND column_name = 'service_context_id'
          AND table_schema = database()
    INTO @col_exists;

    IF @col_exists = 1 THEN
      ALTER TABLE service_binding
        ADD COLUMN service_context_id BIGINT;
      ALTER TABLE service_binding
        ADD CONSTRAINT FK_service_binding2service_context
      FOREIGN KEY (service_context_id) REFERENCES service_context (id);
    END IF;

    SET @col_exists = 1;
    SELECT 0
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'provision_request'
          AND column_name = 'service_context_id'
          AND table_schema = database()
    INTO @col_exists;

    IF @col_exists = 1 THEN
      ALTER TABLE provision_request
        ADD COLUMN service_context_id BIGINT;
      ALTER TABLE provision_request
        ADD CONSTRAINT FK_provision_request2service_context
      FOREIGN KEY (service_context_id) REFERENCES service_context (id);
    END IF;
  END//

DELIMITER ;

DROP PROCEDURE IF EXISTS migrate_cf_context;
DELIMITER //
CREATE PROCEDURE migrate_cf_context()
  BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE sc_id INT; -- service_context_id
    DECLARE si_id INT; -- service_instance_id
    DECLARE orgid, spaceid VARCHAR(255);
    DECLARE scd_not_found INT DEFAULT FALSE;
    DECLARE cur1 CURSOR FOR SELECT
                              si.id,
                              si.org,
                              si.space
                            FROM service_instance si
                            WHERE si.org IS NOT NULL AND si.service_context_id IS NULL;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur1;

    read_loop: LOOP

      BEGIN
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
        FETCH cur1
        INTO si_id, orgid, spaceid;

        IF done
        THEN
          LEAVE read_loop;
        END IF;
      END;

      scd_search_block: BEGIN
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET scd_not_found = TRUE;

        SET scd_not_found = FALSE;

        SELECT scd1.service_context_id
        INTO sc_id
        FROM service_context sc
          JOIN service_context_detail scd1 ON sc.id = scd1.service_context_id
          JOIN service_context_detail scd2 ON sc.id = scd2.service_context_id
        WHERE sc.platform = 'cloudfoundry '
              AND scd1.`_key` = 'organization_guid' AND scd1.`_value` = orgid
              AND scd2.`_key` = 'space_guid' AND scd2.`_value` = spaceid;

        IF scd_not_found
        THEN
          INSERT INTO service_context (platform)
          VALUES ('cloudfoundry');
          SELECT LAST_INSERT_ID()
          INTO sc_id;

          INSERT INTO service_context_detail (`_key`, `_value`, service_context_id)
          VALUES ('organization_guid', orgid, sc_id);
          INSERT INTO service_context_detail (`_key`, `_value`, service_context_id)
          VALUES ('space_guid', spaceid, sc_id);

          COMMIT;

        END IF;

        LEAVE scd_search_block;
      END;


      UPDATE service_instance si
      SET service_context_id = sc_id
      WHERE si.id = si_id;

      COMMIT;

    END LOOP;

    COMMIT;
    CLOSE cur1;
  END//

DELIMITER ;

DROP PROCEDURE IF EXISTS migrate_provision_request_cf_context;
DELIMITER //
CREATE PROCEDURE migrate_provision_request_cf_context()
  BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE sc_id INT; -- service_context_id
    DECLARE pr_id INT; -- provision_request id
    DECLARE orgid, spaceid VARCHAR(255);
    DECLARE scd_not_found INT DEFAULT FALSE;
    DECLARE cur1 CURSOR FOR SELECT
                              pr.id,
                              pr.organization_guid,
                              pr.space_guid
                            FROM provision_request pr
                            WHERE pr.organization_guid IS NOT NULL AND pr.service_context_id IS NULL;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur1;

    read_loop: LOOP

      BEGIN
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
        FETCH cur1
        INTO pr_id, orgid, spaceid;

        IF done
        THEN
          LEAVE read_loop;
        END IF;
      END;

      scd_search_block: BEGIN
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET scd_not_found = TRUE;

        SET scd_not_found = FALSE;

        SELECT scd1.service_context_id
        INTO sc_id
        FROM service_context sc
          JOIN service_context_detail scd1 ON sc.id = scd1.service_context_id
          JOIN service_context_detail scd2 ON sc.id = scd2.service_context_id
        WHERE sc.platform = 'cloudfoundry '
              AND scd1.`_key` = 'organization_guid' AND scd1.`_value` = orgid
              AND scd2.`_key` = 'space_guid' AND scd2.`_value` = spaceid;

        IF scd_not_found
        THEN
          INSERT INTO service_context (platform)
          VALUES ('cloudfoundry');
          SELECT LAST_INSERT_ID()
          INTO sc_id;

          INSERT INTO service_context_detail (`_key`, `_value`, service_context_id)
          VALUES ('organization_guid', orgid, sc_id);
          INSERT INTO service_context_detail (`_key`, `_value`, service_context_id)
          VALUES ('space_guid', spaceid, sc_id);

          COMMIT;

        END IF;

        LEAVE scd_search_block;
      END;


      UPDATE provision_request pr
      SET service_context_id = sc_id
      WHERE pr.id = pr_id;

      COMMIT;

    END LOOP;

    COMMIT;
    CLOSE cur1;
  END//

DELIMITER ;

/* VERIFY MIGRATION AND DROP COLUMNS 'ORG' AND 'SPACE' FROM SERVICE_INSTANCE */
DROP PROCEDURE IF EXISTS remove_org_space_fields_from_service_instance;
DELIMITER //
CREATE PROCEDURE remove_org_space_fields_from_service_instance()
  BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE ERROR_MSG CHAR(255);
    DECLARE siid INT; -- service_instance_id
    DECLARE cur1 CURSOR FOR SELECT si.id
                            FROM service_instance si
                            WHERE si.org IS NOT NULL AND si.service_context_id IS NULL;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur1;

    read_loop: LOOP
      FETCH cur1
      INTO siid;

      IF NOT done
      THEN
        SELECT CONCAT('SERVICE INSTANCE WAS NOT MIGRATED: ', siid)
        INTO ERROR_MSG;

        SIGNAL SQLSTATE '90000'
        SET MESSAGE_TEXT = ERROR_MSG;

      END IF;

      LEAVE read_loop;

    END LOOP;

    ALTER TABLE service_instance
      DROP COLUMN org;
    ALTER TABLE service_instance
      DROP COLUMN space;

    CLOSE cur1;
  END//

DELIMITER ;

DROP PROCEDURE IF EXISTS remove_org_space_fields_from_provision_request;
DELIMITER //
CREATE PROCEDURE remove_org_space_fields_from_provision_request()
  BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE ERROR_MSG CHAR(255);
    DECLARE pr_id INT; -- provision_request id
    DECLARE cur1 CURSOR FOR SELECT pr.id
                            FROM provision_request pr
                            WHERE pr.organization_guid IS NOT NULL AND pr.service_context_id IS NULL;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur1;

    read_loop: LOOP
      FETCH cur1
      INTO pr_id;

      IF NOT done
      THEN
        SELECT CONCAT('PROVISION_REQUEST WAS NOT MIGRATED: ', pr_id)
        INTO ERROR_MSG;

        SIGNAL SQLSTATE '90001'
        SET MESSAGE_TEXT = ERROR_MSG;

      END IF;

      LEAVE read_loop;

    END LOOP;

    ALTER TABLE provision_request
      DROP COLUMN organization_guid,
      DROP COLUMN space_guid;

    CLOSE cur1;
  END//
DELIMITER ;

DROP PROCEDURE IF EXISTS orchestrate_migration_11;
DELIMITER //
CREATE PROCEDURE orchestrate_migration_11()
  BEGIN
    CALL create_table_service_context();

    SET @col_exists = 0;
    SELECT 1
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'service_instance'
          AND column_name = 'org'
          AND table_schema = database()
    INTO @col_exists;

    IF @col_exists = 1 THEN
      CALL migrate_cf_context();
      CALL migrate_provision_request_cf_context();
      CALL remove_org_space_fields_from_service_instance();
      CALL remove_org_space_fields_from_provision_request();
    END IF;
  END//
DELIMITER ;

CALL orchestrate_migration_11();