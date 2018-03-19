DROP PROCEDURE IF EXISTS add_GET_endpoints_for_fetching_a_service_instance_binding;
DELIMITER //

CREATE PROCEDURE add_GET_endpoints_for_fetching_a_service_instance_binding()
  BEGIN

    ALTER TABLE service
      ADD COLUMN instances_retrievable BIT(1) DEFAULT 0;
    ALTER TABLE service
      ADD COLUMN bindings_retrievable BIT(1) DEFAULT 0;

  END//

DELIMITER ;
CALL add_GET_endpoints_for_fetching_a_service_instance_binding();
