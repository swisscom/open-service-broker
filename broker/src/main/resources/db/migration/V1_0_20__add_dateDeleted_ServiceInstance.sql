ALTER TABLE provision_request
  ADD application_user VARCHAR(30) DEFAULT NULL NULL;

ALTER TABLE service_instance
  ADD application_user_id INT DEFAULT NULL NULL;
ALTER TABLE service_instance
  ADD CONSTRAINT service_instance_application_user___fk FOREIGN KEY (application_user_id) REFERENCES application_user (id);

ALTER TABLE service_binding
  ADD application_user_id INT DEFAULT NULL NULL;
ALTER TABLE service_binding
  ADD CONSTRAINT service_binding_application_user___fk FOREIGN KEY (application_user_id) REFERENCES application_user (id);

DROP PROCEDURE IF EXISTS migrate_add_application_user_service_instance;
DELIMITER //

CREATE PROCEDURE migrate_add_application_user_service_instance()
  BEGIN
    DECLARE app_user_id INT;

    SELECT id
    INTO app_user_id
    FROM application_user
    WHERE role = 'CF_ADMIN'
    ORDER BY id ASC
    LIMIT 1;

    UPDATE service_instance
    SET application_user_id = app_user_id
    WHERE application_user_id IS NULL;

    UPDATE service_binding
    SET application_user_id = app_user_id
    WHERE application_user_id IS NULL;

    COMMIT;

  END//

DELIMITER ;
CALL migrate_add_application_user_service_instance();
