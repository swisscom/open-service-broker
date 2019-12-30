UPDATE service_context_detail SET `_value` = '' WHERE `_value` is NULL;

ALTER TABLE service_context_detail
    MODIFY `_value` VARCHAR(255) NOT NULL;