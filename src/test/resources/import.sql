--Settings


--Connection source
insert into bone_cp_settings (id_pool, driver, url, user, pass, min_connections_per_partition, max_connections_per_partition, partition_count, connection_timeout_in_ms, close_connection_watch_timeout_in_ms ) values ('source', 'org.h2.Driver', 'jdbc:h2:mem://localhost/~/source', 'sa', '', 1, 100, 1, 10000, 0);
--Connection dest
insert into bone_cp_settings (id_pool, driver, url, user, pass, min_connections_per_partition, max_connections_per_partition, partition_count, connection_timeout_in_ms, close_connection_watch_timeout_in_ms ) values ('dest', 'org.h2.Driver', 'jdbc:h2:mem://localhost/~/dest', 'sa', '', 1, 100, 1, 10000, 0);
--Connection stats
insert into bone_cp_settings (id_pool, driver, url, user, pass, min_connections_per_partition, max_connections_per_partition, partition_count, connection_timeout_in_ms, close_connection_watch_timeout_in_ms ) values ('stats', 'org.h2.Driver', 'jdbc:h2:mem://localhost/~/source', 'sa', '', 1, 100, 1, 10000, 0);
--Connection error
insert into bone_cp_settings (id_pool, driver, url, user, pass, min_connections_per_partition, max_connections_per_partition, partition_count, connection_timeout_in_ms, close_connection_watch_timeout_in_ms ) values ('error', 'org.h2.Driver', 'jdbc:h2:mem://localhost/~/source', 'sa', '', 1, 100, 1, 10000, 0);

--application_settings
insert into application_settings (key, value) values ('tp.threads', '10');
insert into application_settings (key, value) values ('stats.dest', 'stats');
insert into application_settings (key, value) values ('error.dest', 'error');


--Tables
insert into tables (id_table, id_pool, name) values (1, 'source', 'T_TABLE');
insert into tables (id_table, id_pool, name) values (2, 'source', 'T_TABLE1');
insert into tables (id_table, id_pool, name) values (3, 'source', 'T_TABLE2');
insert into tables (id_table, id_pool, name) values (4, 'source', 'T_TABLE3');
insert into tables (id_table, id_pool, name) values (5, 'source', 'T_TABLE4');
insert into tables (id_table, id_pool, name) values (6, 'source', 'T_TABLE5');
insert into tables (id_table, id_pool, name) values (8, 'dest',   'T_TABLE');
insert into tables (id_table, id_pool, name) values (9, 'dest', 'T_TABLE1');
insert into tables (id_table, id_pool, name) values (10, 'dest', 'T_TABLE2');
insert into tables (id_table, id_pool, name) values (11, 'dest', 'T_TABLE3');
insert into tables (id_table, id_pool, name) values (12, 'dest', 'T_TABLE4');
insert into tables (id_table, id_pool, name) values (13, 'dest', 'T_TABLE5');
insert into tables (id_table, id_pool, name) values (14, 'source', 'T_TABLE6');
insert into tables (id_table, id_pool, name) values (15, 'source', 'T_TABLE7');
insert into tables (id_table, id_pool, name) values (16, 'source', 'T_TABLE8');

--Runners Super Log
insert into runners (id_runner, source, target, description) values (1, 'source', 'source', 'FastManager');
--Strategies Add Super Log
insert into strategies (id, className, param, isEnabled, priority, id_runner) values (1, 'ru.taximaxim.dbreplicator2.replica.strategies.superlog.Manager', 'key1=value1
key2=''value2''', true, 100, 1);

--Runners Super Log 2
insert into runners (id_runner, source, target, description) values (2, 'dest', 'dest', 'FastManager');
--Strategies Add Super Log 2
insert into strategies (id, className, param, isEnabled, priority, id_runner) values (2, 'ru.taximaxim.dbreplicator2.replica.strategies.superlog.Manager', 'key1=value1
key2=''value2''', true, 100, 2);


--Runners Task

--Runner Table 1
insert into runners (id_runner, source, target, description) values (3, 'source', 'dest', 'Generic');
--Strategy  Table 1
insert into strategies (id, className, param, isEnabled, priority, id_runner) values (3, 'ru.taximaxim.dbreplicator2.replica.strategies.replication.Generic', null, true, 100, 3);

-------

--Runner Table 2
insert into runners (id_runner, source, target, description) values (4, 'source', 'dest', 'Generic');
--Strategy  Table 2
insert into strategies (id, className, param, isEnabled, priority, id_runner) values (4, 'ru.taximaxim.dbreplicator2.replica.strategies.replication.Generic', null, true, 100, 4);

-------

--Runner Table 3
insert into runners (id_runner, source, target, description) values (5, 'source', 'dest', 'Generic');
--Strategy  Table 3
insert into strategies (id, className, param, isEnabled, priority, id_runner) values (5, 'ru.taximaxim.dbreplicator2.replica.strategies.replication.Generic', null, true, 100, 5);

-------

--Runner Table 4,5,6
insert into runners (id_runner, source, target, description) values (6, 'source', 'dest', 'Generic');
--Strategy  Table 4,5,6
insert into strategies (id, className, param, isEnabled, priority, id_runner) values (6, 'ru.taximaxim.dbreplicator2.replica.strategies.replication.Generic', null, true, 100, 6);

-------

--Runners Task

--Runner Table 8
insert into runners (id_runner, source, target, description) values (8, 'dest', 'source', 'Generic');
--Strategy  Table 8
insert into strategies (id, className, param, isEnabled, priority, id_runner) values (8, 'ru.taximaxim.dbreplicator2.replica.strategies.replication.Generic', null, true, 100, 8);

-------

--Runners Task

--Runner Table 9,10,11,12,13
insert into runners (id_runner, source, target, description) values (9, 'dest', 'source', 'Generic');
--Strategy  Table 9,10,11,12,13
insert into strategies (id, className, param, isEnabled, priority, id_runner) values (9, 'ru.taximaxim.dbreplicator2.replica.strategies.replication.Generic', null, true, 100, 9);

--Runner CountWatchgdog
insert into runners (id_runner, source, target, description) values (7, 'source', 'source', 'ErrorsCountWatchgdogStrategy');
--Strategy  CountWatchgdog
insert into strategies (id, className, param, isEnabled, priority, id_runner) values (7, 'ru.taximaxim.dbreplicator2.replica.strategies.errors.CountWatchgdog', 'maxErrors=0
partEmail=10', true, 100, 7);
insert into strategies (id, className, param, isEnabled, priority, id_runner) values (10, 'ru.taximaxim.dbreplicator2.replica.strategies.errors.CountWatchgdog', null, true, 100, 7);

--Runner SuperlogWatchgdog
insert into runners (id_runner, source, target, description) values (15, 'source', 'source', 'ErrorsSuperlogWatchgdog');
--Strategy  SuperlogWatchgdog
insert into strategies (id, className, param, isEnabled, priority, id_runner) values (15, 'ru.taximaxim.dbreplicator2.replica.strategies.errors.SuperlogWatchgdog', 'period=1000
partEmail=10', true, 100, 15);

-- Раннер стратегии проверки логирования необработанных исключений в задаче
insert into runners (id_runner, source, target, description) values (16, 'source', 'dest', 'OutOfMemoryErrorStrategy runner');
insert into strategies (id, id_runner, className, param, isEnabled, priority) values (16, 16, 'ru.taximaxim.dbreplicator2.utils.OutOfMemoryErrorStrategy', null, true, 100);

-- Раннер стратегии проверки логирования необработанных исключений в пуле потоков
insert into runners (id_runner, source, target, description) values (17, 'source', 'dest', 'OutOfMemoryErrorStrategy runner');
insert into strategies (id, id_runner, className, param, isEnabled, priority) values (17, 17, 'ru.taximaxim.dbreplicator2.utils.OutOfMemoryErrorStrategy', null, true, 100);

-------
--Runner null
insert into runners (id_runner, source, target, description) values (25, 'source', 'dest', 'Null');
-------
--Runner IntegrityReplicatedData
insert into runners (id_runner, source, target, description) values (18, 'source', 'dest', 'ErrorsIntegrityReplicatedData');
--Strategy  IntegrityReplicatedData
insert into strategies (id, id_runner, className, param, isEnabled, priority) values (18, 18, 'ru.taximaxim.dbreplicator2.replica.strategies.errors.IntegrityReplicatedData', 'period=0
idRunner=25', true, 100);


--Runner tables
--insert into table_observers (id_runner, id_table) values (25, 1);
insert into table_observers (id_runner, id_table) values (25, 2);
insert into table_observers (id_runner, id_table) values (25, 3);
insert into table_observers (id_runner, id_table) values (25, 4);
insert into table_observers (id_runner, id_table) values (25, 5);
insert into table_observers (id_runner, id_table) values (25, 6);
insert into table_observers (id_runner, id_table) values (3, 1);
insert into table_observers (id_runner, id_table) values (4, 2);
insert into table_observers (id_runner, id_table) values (5, 6);
insert into table_observers (id_runner, id_table) values (6, 3);
insert into table_observers (id_runner, id_table) values (6, 4);
insert into table_observers (id_runner, id_table) values (6, 5);
insert into table_observers (id_runner, id_table) values (8, 8);
insert into table_observers (id_runner, id_table) values (9, 9);
insert into table_observers (id_runner, id_table) values (9, 10);
insert into table_observers (id_runner, id_table) values (9, 11);
insert into table_observers (id_runner, id_table) values (9, 12);
insert into table_observers (id_runner, id_table) values (9, 13);
insert into table_observers (id_runner, id_table) values (25, 14);
insert into table_observers (id_runner, id_table) values (25, 15);
insert into table_observers (id_runner, id_table) values (25, 16);

--Runner ReplicationTimeWatchgdog
insert into runners (id_runner, source, target, description) values (10, 'source', 'source', 'ErrorsReplicationTimeWatchgdog');
--Strategy  ReplicationTimeWatchgdog
insert into strategies (id, className, param, isEnabled, priority, id_runner) values (11, 'ru.taximaxim.dbreplicator2.replica.strategies.errors.ReplicationTimeWatchgdog', 'period=0
partEmail=10', true, 100, 10);

-- Ignore Columns Table
insert into ignore_columns_table (id_ignore_columns_table, id_table, column_name) values (1, 1, '_STRING');
insert into ignore_columns_table (id_ignore_columns_table, id_table, column_name) values (2, 2, '_STRING');
insert into ignore_columns_table (id_ignore_columns_table, id_table, column_name) values (3, 3, '_STRING');
insert into ignore_columns_table (id_ignore_columns_table, id_table, column_name) values (4, 4, '_STRING');
insert into ignore_columns_table (id_ignore_columns_table, id_table, column_name) values (5, 5, '_STRING');
insert into ignore_columns_table (id_ignore_columns_table, id_table, column_name) values (6, 6, '_STRING');

insert into ignore_columns_table (id_ignore_columns_table, id_table, column_name) values (8, 8, '_STRING');
insert into ignore_columns_table (id_ignore_columns_table, id_table, column_name) values (9, 9, '_STRING');
insert into ignore_columns_table (id_ignore_columns_table, id_table, column_name) values (10, 10, '_STRING');
insert into ignore_columns_table (id_ignore_columns_table, id_table, column_name) values (11, 11, '_STRING');
insert into ignore_columns_table (id_ignore_columns_table, id_table, column_name) values (12, 12, '_STRING');
insert into ignore_columns_table (id_ignore_columns_table, id_table, column_name) values (13, 13, '_STRING');

-- Required Columns Table
insert into required_columns_table (id_required_columns_table, id_table, column_name) values ( 1, 2, 'ID');
insert into required_columns_table (id_required_columns_table, id_table, column_name) values ( 2, 2, '_INT');
insert into required_columns_table (id_required_columns_table, id_table, column_name) values ( 3, 2, '_BOOLEAN');
insert into required_columns_table (id_required_columns_table, id_table, column_name) values ( 4, 2, '_LONG');
insert into required_columns_table (id_required_columns_table, id_table, column_name) values ( 5, 2, '_DECIMAL');
insert into required_columns_table (id_required_columns_table, id_table, column_name) values ( 6, 2, '_DOUBLE');
insert into required_columns_table (id_required_columns_table, id_table, column_name) values ( 7, 2, '_FLOAT');
insert into required_columns_table (id_required_columns_table, id_table, column_name) values ( 8, 2, '_BYTE');
insert into required_columns_table (id_required_columns_table, id_table, column_name) values ( 9, 2, '_DATE');
insert into required_columns_table (id_required_columns_table, id_table, column_name) values (10, 2, '_TIME');
insert into required_columns_table (id_required_columns_table, id_table, column_name) values (11, 2, '_TIMESTAMP');
insert into required_columns_table (id_required_columns_table, id_table, column_name) values (12, 2, '_NOCOLOMN');
insert into required_columns_table (id_required_columns_table, id_table, column_name) values (13, 2, '_STRING');



-- Задача с ошибочной стратегией
insert into tasks (id_task, id_runner, enabled, success_interval, fail_interval, description) values (16, 16, true, 10000, 300000, 'ru.taximaxim.dbreplicator2.utils.OutOfMemoryErrorStrategy');
