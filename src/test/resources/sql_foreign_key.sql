INSERT INTO t_table2 (ID, _int, _boolean, _long, _decimal, _double, _float, _string, _byte, _date, _time, _timestamp) VALUES(?, ?, true, 5968326496, 99.65, 5.62, 79.6, 'Insert', 0, now(), now(), now());
--
INSERT INTO t_table3 (ID, _int, _boolean, _long, _decimal, _double, _float, _string, _byte, _date, _time, _timestamp) VALUES(?, ?, true, 5968326496, 99.65, 5.62, 79.6, 'Insert', 0, now(), now(), now());
--
UPDATE t_table2 SET _boolean = true, _long = 0, _decimal = 0, _double = 0, _float = 0, _string = 'Update', _byte = 0, _date = now(), _time = now(), _timestamp = now() WHERE _int = ?;
