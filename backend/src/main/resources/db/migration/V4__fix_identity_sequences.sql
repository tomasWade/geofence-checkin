SELECT setval(pg_get_serial_sequence('sys_user','id'), (SELECT COALESCE(MAX(id),0)+1 FROM sys_user), false);
SELECT setval(pg_get_serial_sequence('sys_role','id'), (SELECT COALESCE(MAX(id),0)+1 FROM sys_role), false);
SELECT setval(pg_get_serial_sequence('sys_permission','id'), (SELECT COALESCE(MAX(id),0)+1 FROM sys_permission), false);
SELECT setval(pg_get_serial_sequence('geo_fence','id'), (SELECT COALESCE(MAX(id),0)+1 FROM geo_fence), false);
SELECT setval(pg_get_serial_sequence('checkin_record','id'), (SELECT COALESCE(MAX(id),0)+1 FROM checkin_record), false);
SELECT setval(pg_get_serial_sequence('sys_audit_log','id'), (SELECT COALESCE(MAX(id),0)+1 FROM sys_audit_log), false);
SELECT setval(pg_get_serial_sequence('checkin_rule','id'), (SELECT COALESCE(MAX(id),0)+1 FROM checkin_rule), false);
SELECT setval(pg_get_serial_sequence('daily_report','id'), (SELECT COALESCE(MAX(id),0)+1 FROM daily_report), false);
