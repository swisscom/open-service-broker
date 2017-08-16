ALTER TABLE service
  ADD service_provider_class_name VARCHAR(255) DEFAULT NULL;
ALTER TABLE plan
  ADD service_provider_class_name VARCHAR(255) DEFAULT NULL;