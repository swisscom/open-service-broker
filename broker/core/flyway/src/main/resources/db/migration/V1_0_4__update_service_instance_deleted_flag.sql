UPDATE service_instance si
  JOIN last_operation lo ON si.guid = lo.guid
SET si.deleted = TRUE
WHERE lo.operation = 'DEPROVISION' AND lo.status LIKE 'SUCC%'