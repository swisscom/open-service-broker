DROP PROCEDURE IF EXISTS create_table_service_context_detail;
DELIMITER //

CREATE PROCEDURE create_table_service_context()
  BEGIN

    CREATE TABLE service_context
    (
      id       BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
      platform VARCHAR(255) NOT NULL
    );

    CREATE TABLE service_context_detail
    (
      id                 BIGINT                      NOT NULL AUTO_INCREMENT PRIMARY KEY,
      `_key`             VARCHAR(255)                NOT NULL,
      `_value`           VARCHAR(255) DEFAULT 'NULL' NULL,
      service_context_id BIGINT                      NOT NULL,
      CONSTRAINT FK_service_context_detail2context
      FOREIGN KEY (service_context_id) REFERENCES service_context (id)
    );

    ALTER TABLE service_instance
      ADD COLUMN service_context_id BIGINT;
    ALTER TABLE service_instance
      ADD CONSTRAINT FK_service_instance2service_context
    FOREIGN KEY (service_context_id) REFERENCES service_context (id);

    ALTER TABLE service_binding
      ADD COLUMN service_context_id BIGINT;
    ALTER TABLE service_binding
      ADD CONSTRAINT FK_service_binding2service_context
    FOREIGN KEY (service_context_id) REFERENCES service_context (id);
    
    ALTER TABLE provision_request
      DROP COLUMN organization_guid,
      DROP COLUMN space_guid,
      ADD COLUMN service_context_id BIGINT;

    ALTER TABLE provision_request
      ADD CONSTRAINT FK_provision_request2service_context
    FOREIGN KEY (service_context_id) REFERENCES service_context (id);

  END//

DELIMITER ;
CALL create_table_service_context();

/*** MIGRATE CLOUD FOUNDRY CONTEXT SCRIPT ***/
DROP PROCEDURE IF EXISTS migrate_cf_context;
DELIMITER //
CREATE PROCEDURE migrate_cf_context()
  BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE service_context_id INT;
    DECLARE service_instance_id INT;
    DECLARE org_id, space_id VARCHAR(255);
    DECLARE cur1 CURSOR FOR SELECT
                              si.id,
                              si.org,
                              si.space
                            FROM service_instance si
                            WHERE si.org IS NOT NULL AND si.service_context_id IS NULL;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur1;

    read_loop: LOOP
      FETCH cur1
      INTO service_instance_id, org_id, space_id;

      IF done
      THEN
        LEAVE read_loop;
      END IF;

      INSERT INTO service_context (platform)
      VALUES ('cloudfoundry');
      SELECT LAST_INSERT_ID()
      INTO service_context_id;

      INSERT INTO service_context_detail (`_key`, `_value`, service_context_id)
      VALUES ('organization_guid', org_id, service_context_id);
      INSERT INTO service_context_detail (`_key`, `_value`, service_context_id)
      VALUES ('space_guid', space_id, service_context_id);

      UPDATE service_instance si
      SET service_context_id = service_context_id
      WHERE si.id = service_instance_id;

    END LOOP;

    COMMIT;
    CLOSE cur1;

  END//

DELIMITER ;
CALL migrate_cf_context();

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
CALL remove_org_space_fields_from_service_instance();
