DROP PROCEDURE IF EXISTS update_application_user_table_username_index;
DELIMITER //

CREATE PROCEDURE update_application_user_table_username_index()
  BEGIN
    ALTER TABLE application_user
       DROP INDEX application_user_name_uindex,
       ADD UNIQUE KEY `application_user_name_uindex` (`platform_guid`,`username`);
  END//

DELIMITER ;
CALL update_application_user_table_username_index();
