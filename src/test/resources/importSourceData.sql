INSERT INTO t_table (_int, _boolean, _long, _decimal, _double, _float, _string, _byte, _date, _time, _timestamp) VALUES(1, true, 5968326496, 99.65, 5.62, 79.6, 'Delete', 0, now(), now(), now());
INSERT INTO t_table (_int, _boolean, _long, _decimal, _double, _float, _string, _byte, _date, _time, _timestamp) VALUES(2, false, 596328794, 45.92, 7.16, 21.2, 'Update', 1, now(), now(), now());
INSERT INTO t_table (_int, _boolean, _long, _decimal, _double, _float, _string, _byte, _date, _time, _timestamp) VALUES(3, true, 7963256489, 75.16, 9.15, 33.9, 'Insert', 2, now(), now(), now());
UPDATE t_table SET _boolean = true, _long = 0, _decimal = 0, _double = 0, _float = 0, _string = 'Update', _byte = 0, _date = now(), _time = now(), _timestamp = now() WHERE _int = 2;
DELETE FROM t_table WHERE _int = 1;
