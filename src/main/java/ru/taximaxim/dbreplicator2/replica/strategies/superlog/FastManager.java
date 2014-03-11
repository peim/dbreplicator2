/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Technologiya
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ru.taximaxim.dbreplicator2.replica.strategies.superlog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ru.taximaxim.dbreplicator2.jdbc.Jdbc;
import ru.taximaxim.dbreplicator2.model.BoneCPSettingsModel;
import ru.taximaxim.dbreplicator2.model.RunnerModel;
import ru.taximaxim.dbreplicator2.model.TableModel;
import ru.taximaxim.dbreplicator2.model.StrategyModel;
import ru.taximaxim.dbreplicator2.replica.Strategy;
import ru.taximaxim.dbreplicator2.replica.StrategyException;
import ru.taximaxim.dbreplicator2.replica.strategies.replication.workpool.WorkPoolService;
import ru.taximaxim.dbreplicator2.utils.Core;

/**
 * Класс стратегии менеджера записей суперлог таблицы с асинхронным параллельным
 * запуском обработчиков реплик
 * 
 * @author volodin_aa
 * 
 */
public class FastManager implements Strategy {

    private static final Logger LOG = Logger.getLogger(FastManager.class);

    /**
     * Размер выборки данных (строк)
     */
    private int fetchSize = 1000;

    /**
     * Размер сбрасываемых в БД данных (строк)
     */
    private int batchSize = 1000;

    /**
     * Конструктор по умолчанию
     */
    public FastManager() {
    }

    @Override
    public void execute(Connection sourceConnection, Connection targetConnection,
            StrategyModel data) throws StrategyException, SQLException {
        Boolean lastAutoCommit = null;
        Boolean lastTargetAutoCommit = null;
        try {
            lastAutoCommit = sourceConnection.getAutoCommit();
            lastTargetAutoCommit = targetConnection.getAutoCommit();
            // Начинаем транзакцию
            sourceConnection.setAutoCommit(false);
            targetConnection.setAutoCommit(false);
            BoneCPSettingsModel sourcePool = data.getRunner().getSource();
            try {
                sourceConnection
                        .setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                targetConnection
                        .setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                // Строим список обработчиков реплик

                // Переносим данные
                try (PreparedStatement insertRunnerData = targetConnection.prepareStatement("INSERT INTO rep2_workpool_data (id_runner, id_superlog, id_foreign, id_table, c_operation, c_date, id_transaction) VALUES (?, ?, ?, ?, ?, ?, ?)");
                     PreparedStatement deleteSuperLog = targetConnection.prepareStatement("DELETE FROM rep2_superlog WHERE id_superlog=?");
                     PreparedStatement selectSuperLog = sourceConnection.prepareStatement("SELECT * FROM rep2_superlog ORDER BY id_superlog");) {
                    selectSuperLog.setFetchSize(fetchSize);
                    try (ResultSet superLogResult = selectSuperLog.executeQuery();) {
                        List<String> cols = new ArrayList<String>();
                        cols.add(WorkPoolService.ID_SUPERLOG);
                        cols.add(WorkPoolService.ID_POOL);
                        cols.add(WorkPoolService.ID_FOREIGN);
                        cols.add(WorkPoolService.ID_TABLE);
                        cols.add(WorkPoolService.C_OPERATION);
                        cols.add(WorkPoolService.C_DATE);
                        cols.add(WorkPoolService.ID_TRANSACTION);

                        for (int rowsCount = 1; superLogResult.next(); rowsCount++) {
                            // Выводим данные из rep2_superlog_table
                            if (LOG.isDebugEnabled()) {
                                LOG.debug(Jdbc.resultSetToString(superLogResult, cols));
                            }
                            // Копируем записи
                            // Проходим по списку слушателей текущей таблицы
                            for (TableModel table : sourcePool.getTables()) {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug(table.getName() + " id: "
                                            + table.getTableId());
                                }
                                if (table.getName().equalsIgnoreCase(
                                        superLogResult.getString(WorkPoolService.ID_TABLE))) {
                                    for (RunnerModel runner : table.getRunners()) {
                                        if(!superLogResult.getString(WorkPoolService.ID_POOL).equals(runner.getTarget().getPoolId())) {
                                            insertRunnerData.setInt(1, runner.getId());
                                            insertRunnerData.setLong(2, superLogResult.getLong(WorkPoolService.ID_SUPERLOG));
                                            insertRunnerData.setInt(3, superLogResult.getInt(WorkPoolService.ID_FOREIGN));
                                            insertRunnerData.setString(4, superLogResult.getString(WorkPoolService.ID_TABLE));
                                            insertRunnerData.setString(5, superLogResult.getString(WorkPoolService.C_OPERATION));
                                            insertRunnerData.setTimestamp(6, superLogResult.getTimestamp(WorkPoolService.C_DATE));
                                            insertRunnerData.setString(7, superLogResult .getString(WorkPoolService.ID_TRANSACTION));
                                            insertRunnerData.addBatch();
                                            // Выводим данные из rep2_superlog_table
                                            if (LOG.isDebugEnabled()) {
                                                LOG.debug("INSERT");
                                            }
                                        }
                                        // Удаляем исходную запись
                                        deleteSuperLog.setLong(1, superLogResult.getLong(WorkPoolService.ID_SUPERLOG));
                                        deleteSuperLog.addBatch();
                                    }
                                }

                            }

                            // Периодически сбрасываем батч в БД
                            if ((rowsCount % batchSize) == 0) {
                                insertRunnerData.executeBatch();
                                deleteSuperLog.executeBatch();
                                targetConnection.commit();

                                LOG.info(String.format("Обработано %s строк...", rowsCount));
                            }
                        }
                        insertRunnerData.executeBatch();
                        deleteSuperLog.executeBatch();
                        targetConnection.commit();
                    }
                }
            } catch (SQLException e) {
                // Откатываемся
                targetConnection.rollback();
                // Пробрасываем ошибку на уровень выше
                throw e;
            }
            // Асинхронно запускаем обработчики реплик
            for (RunnerModel runner : sourcePool.getRunners()) {
                if (!runner.getTables().isEmpty()) {
                    Core.getThreadPool().start(runner);
                }
            }
        } catch (InterruptedException e) {
            throw new StrategyException(e);
        } finally {
            try {
                if (lastAutoCommit != null) {
                    sourceConnection.setAutoCommit(lastAutoCommit);
                }
            } catch (SQLException e) {
                // Ошибка может возникнуть если во время операции упало
                // соединение к БД
                LOG.warn("Ошибка при возврате автокомита в исходное состояние.", e);
            }

            try {
                if (lastTargetAutoCommit != null) {
                    targetConnection.setAutoCommit(lastTargetAutoCommit);
                }
            } catch (SQLException sqlException) {
                // Ошибка может возникнуть если во время операции упало
                // соединение к БД
                LOG.warn("Ошибка при возврате автокомита в исходное состояние.", sqlException);
            }
        }
    }

}
