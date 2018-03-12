DROP PROCEDURE IF EXISTS fix_cf_context_migration;
DELIMITER //
CREATE PROCEDURE fix_cf_context_migration()
  BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE sc_id INT; -- service_context_id
    DECLARE si_id INT; -- service_instance_id
    DECLARE org_id, space_id VARCHAR(255);
    DECLARE cur1 CURSOR FOR SELECT si.id
                            FROM service_instance si
                              JOIN service_context sc2 ON si.service_context_id = sc2.id
                            WHERE si.service_context_id IS NOT NULL AND sc2.platform = 'cloudfoundry';

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur1;

    read_loop: LOOP
      FETCH cur1
      INTO si_id;

      IF done
      THEN
        LEAVE read_loop;
      END IF;

      -- find org_id and space_id
      SELECT
        scd1.`_value` AS org_id,
        scd2.`_value` AS space_id
      INTO org_id, space_id
      FROM service_instance si
        JOIN service_context sc ON si.service_context_id = sc.id
        JOIN service_context_detail scd1 ON sc.id = scd1.service_context_id
        JOIN service_context_detail scd2 ON sc.id = scd2.service_context_id
      WHERE si.id = si_id
            AND scd1.`_key` = 'organization_guid'
            AND scd2.`_key` = 'space_guid';

      -- select first record which matches the same values for platform, org_id, space_id
      SELECT min(sc.id)
      INTO sc_id
      FROM service_context sc
        JOIN service_context_detail scd1 ON sc.id = scd1.service_context_id
        JOIN service_context_detail scd2 ON sc.id = scd2.service_context_id
      WHERE sc.platform = 'cloudfoundry'
            AND scd1.`_key` = 'organization_guid' AND scd1.`_value` = org_id
            AND scd2.`_key` = 'space_guid' AND scd2.`_value` = space_id;

      -- update service_instance
      UPDATE service_instance si
      SET service_context_id = sc_id
      WHERE si.id = si_id;
    END LOOP;

    CLOSE cur1;

    -- delete unused/orphan service_context and its detail
    DELETE FROM service_context_detail
    WHERE service_context_id IN (SELECT sc.id
                                 FROM service_context sc
                                 WHERE id NOT IN (SELECT DISTINCT si.service_context_id
                                                  FROM service_instance si));
    DELETE FROM service_context
    WHERE id NOT IN (SELECT DISTINCT si.service_context_id
                     FROM service_instance si);

    COMMIT;

  END//
DELIMITER ;
CALL fix_cf_context_migration();
