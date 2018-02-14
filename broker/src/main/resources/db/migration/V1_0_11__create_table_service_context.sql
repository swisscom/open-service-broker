DROP PROCEDURE IF EXISTS create_table_service_context;
DELIMITER //

CREATE PROCEDURE create_table_service_context()
  BEGIN

    CREATE TABLE service_context
    (
      id       BIGINT                      NOT NULL AUTO_INCREMENT PRIMARY KEY,
      `_key`   VARCHAR(255)                NOT NULL,
      `_value` VARCHAR(255) DEFAULT 'NULL' NULL
    );

    CREATE INDEX FK_CONTEXT_SERVICE_INSTANCE_KEY_VALUE
      ON service_context (`_key`, `_value`);

    -- table service_instance 2 service_context
    CREATE TABLE service_instance_service_context
    (
      service_instance_id BIGINT NOT NULL,
      service_context_id  BIGINT NOT NULL,
      CONSTRAINT FK_service_instance_context2instance
      FOREIGN KEY (service_instance_id) REFERENCES service_instance (id),
      CONSTRAINT FK_service_instance_context2context
      FOREIGN KEY (service_context_id) REFERENCES service_context (id)
    );

    CREATE INDEX FK_service_instance_context2instance
      ON service_instance_service_context (service_instance_id);

    CREATE INDEX FK_service_instance_context2context
      ON service_instance_service_context (service_context_id);

    -- table service_binding 2 service_context
    CREATE TABLE service_binding_service_context
    (
      service_binding_id BIGINT NOT NULL,
      service_context_id BIGINT NOT NULL,
      CONSTRAINT FK_service_binding_service_context2binding
      FOREIGN KEY (service_binding_id) REFERENCES service_binding (id),
      CONSTRAINT FK_service_binding_service_context2context
      FOREIGN KEY (service_context_id) REFERENCES service_context (id)
    );

    CREATE INDEX FK_service_binding_service_context2binding
      ON service_binding_service_context (service_binding_id);

    CREATE INDEX FK_service_binding_service_context2context
      ON service_binding_service_context (service_context_id);

  END//

DELIMITER ;
CALL create_table_service_context();

/*** MIGRATE CLOUD FOUNDRY CONTEXT SCRIPT ***/
DROP PROCEDURE IF EXISTS migrate_cf_context;
DELIMITER //
CREATE PROCEDURE migrate_cf_context()
  BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE service_instance_id INT;
    DECLARE org_id, space_id VARCHAR(255);
    DECLARE cur1 CURSOR FOR SELECT
                              si.id,
                              si.org,
                              si.space
                            FROM service_instance si
                            WHERE si.org IS NOT NULL AND si.id NOT IN (SELECT sisc.service_instance_id
                                                                       FROM service_instance_service_context sisc);
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur1;

    read_loop: LOOP
      FETCH cur1
      INTO service_instance_id, org_id, space_id;

      IF done
      THEN
        LEAVE read_loop;
      END IF;

      INSERT INTO service_context (`_key`, `_value`)
      VALUES ('platform', 'cloudfoundry');
      INSERT INTO service_instance_service_context (service_instance_id, service_context_id)
      VALUES (service_instance_id, LAST_INSERT_ID());

      INSERT INTO service_context (`_key`, `_value`)
      VALUES ('organization_guid', org_id);
      INSERT INTO service_instance_service_context (service_instance_id, service_context_id)
      VALUES (service_instance_id, LAST_INSERT_ID());

      INSERT INTO service_context (`_key`, `_value`)
      VALUES ('space_guid', space_id);
      INSERT INTO service_instance_service_context (service_instance_id, service_context_id)
      VALUES (service_instance_id, LAST_INSERT_ID());

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
    DECLARE sc_count INT DEFAULT 0; -- service_context_count
    DECLARE cur1 CURSOR FOR SELECT si.id
                            FROM service_instance si
                            WHERE si.org IS NOT NULL;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur1;

    read_loop: LOOP
      FETCH cur1
      INTO siid;

      IF done
      THEN
        LEAVE read_loop;
      END IF;

      -- check if all service_instance have migrated service_context records
      SELECT count(1)
      INTO sc_count
      FROM service_context
        JOIN service_instance_service_context sisc ON service_context.id = sisc.service_context_id
      WHERE sisc.service_instance_id = siid AND `_key` IN ('platform', 'organization_guid', 'space_guid');

      IF sc_count != 3
      THEN
        SELECT CONCAT('SERVICE INSTANCE WAS NOT MIGRATED: ', siid)
        INTO ERROR_MSG;

        SIGNAL SQLSTATE '90000'
        SET MESSAGE_TEXT = ERROR_MSG;
      END IF;

    END LOOP;

    ALTER TABLE service_instance
      DROP COLUMN org;
    ALTER TABLE service_instance
      DROP COLUMN space;

    CLOSE cur1;

  END//

DELIMITER ;
CALL remove_org_space_fields_from_service_instance();
