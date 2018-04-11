DROP PROCEDURE IF EXISTS create_IDX_PK_service_details;
DELIMITER //

CREATE PROCEDURE create_IDX_PK_service_details()
  BEGIN

  CREATE TEMPORARY TABLE `tmp_service_instance_service_detail` (
      `service_instance_details_id` BIGINT(20) NULL DEFAULT NULL,
      `service_detail_id` BIGINT(20) NULL DEFAULT NULL
  );

  INSERT INTO tmp_service_instance_service_detail(service_instance_details_id, service_detail_id)
      SELECT DISTINCT service_instance_details_id, service_detail_id
      FROM service_instance_service_detail;

  TRUNCATE TABLE service_instance_service_detail;

  ALTER TABLE service_instance_service_detail
      ADD PRIMARY KEY (service_instance_details_id, service_detail_id),
      ADD INDEX service_detail_service_instance_index (service_detail_id, service_instance_details_id);

  INSERT INTO service_instance_service_detail(service_instance_details_id, service_detail_id)
      SELECT DISTINCT service_instance_details_id, service_detail_id
      FROM tmp_service_instance_service_detail;

  DROP TABLE tmp_service_instance_service_detail;

  ALTER TABLE service_binding_service_detail
    ADD PRIMARY KEY (service_binding_details_id, service_detail_id),
    ADD INDEX service_detail_service_binding_index (service_detail_id, service_binding_details_id);

  END//

DELIMITER ;
CALL create_IDX_PK_service_details();

