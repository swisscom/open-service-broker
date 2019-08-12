ALTER TABLE plan
  ADD COLUMN service_instance_create_schema TEXT;
ALTER TABLE plan
  ADD COLUMN service_instance_update_schema TEXT;
ALTER TABLE plan
  ADD COLUMN service_binding_create_schema TEXT;