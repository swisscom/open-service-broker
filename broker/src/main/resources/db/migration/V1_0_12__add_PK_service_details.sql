DROP PROCEDURE IF EXISTS create_IDX_PK_service_details;
DELIMITER //

CREATE PROCEDURE create_IDX_PK_service_details()
  BEGIN

    ALTER TABLE service_instance_service_detail
      ADD PRIMARY KEY (service_instance_details_id, service_detail_id),
      ADD INDEX service_detail_service_instance_index (service_detail_id, service_instance_details_id);

    ALTER TABLE service_binding_service_detail
      ADD PRIMARY KEY (service_binding_details_id, service_detail_id),
      ADD INDEX service_detail_service_binding_index (service_detail_id, service_binding_details_id);

  END//

DELIMITER ;
CALL create_IDX_PK_service_details();

