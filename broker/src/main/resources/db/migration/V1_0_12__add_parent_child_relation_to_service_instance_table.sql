DROP PROCEDURE IF EXISTS add_parent_child_relation_service_instance;
DELIMITER //

CREATE PROCEDURE add_parent_child_relation_service_instance()
  BEGIN

    ALTER TABLE service_instance
      ADD parent_service_instance_id BIGINT DEFAULT NULL NULL;
    ALTER TABLE service_instance
      ADD CONSTRAINT service_instance_parent___fk FOREIGN KEY (parent_service_instance_id) REFERENCES service_instance (id);

  END//

DELIMITER ;
CALL add_parent_child_relation_service_instance();
