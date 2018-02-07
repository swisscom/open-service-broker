DROP PROCEDURE IF EXISTS create_application_user_table;
DELIMITER //

CREATE PROCEDURE create_application_user_table()
  BEGIN

    CREATE TABLE application_user
    (
      id       INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
      username VARCHAR(30)     NOT NULL,
      password VARCHAR(100)    NOT NULL,
      enabled  BOOL            NOT NULL,
      role     VARCHAR(20)     NOT NULL,
      platform_guid VARCHAR(36)     NOT NULL
    );
    CREATE UNIQUE INDEX application_user_id_uindex
      ON application_user (id);
    CREATE UNIQUE INDEX application_user_name_uindex
      ON application_user (username);
  END//

DELIMITER ;
CALL create_application_user_table();
