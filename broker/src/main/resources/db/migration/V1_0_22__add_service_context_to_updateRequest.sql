ALTER TABLE update_request
  ADD COLUMN service_context_id BIGINT;

ALTER TABLE update_request
  ADD CONSTRAINT FK_update_request2service_context
FOREIGN KEY (service_context_id) REFERENCES service_context (id);