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
package ru.taximaxim.dbreplicator2.replica.strategies.replication.algorithms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ru.taximaxim.dbreplicator2.jdbc.Jdbc;
import ru.taximaxim.dbreplicator2.jdbc.JdbcMetadata;
import ru.taximaxim.dbreplicator2.model.StrategyModel;
import ru.taximaxim.dbreplicator2.model.TableModel;
import ru.taximaxim.dbreplicator2.replica.Strategy;
import ru.taximaxim.dbreplicator2.replica.strategies.replication.data.GenericDataTypeService;
import ru.taximaxim.dbreplicator2.replica.strategies.replication.workpool.WorkPoolService;
/**
 * Заготовка стратегии репликации
 * 
 * @author volodin_aa
 *
 */
public class IntegrityReplicatedGenericAlgorithm extends GenericAlgorithm implements Strategy {

    private static final Logger LOG = Logger.getLogger(IntegrityReplicatedGenericAlgorithm.class);
    
    private static final String PART_EMAIL = "partEmail";
    private static final String ID_RUNNER = "idRunner";
    
    private GenericDataTypeService sourceDataService;
    private GenericDataTypeService destDataService;
    
    private static Integer partEmail;
    private static Integer idRunner;
    private static int countPart;
    private static StringBuffer rowDumpEmail;
    
    public IntegrityReplicatedGenericAlgorithm(int fetchSize, int batchSize,
            boolean isStrict, WorkPoolService workPoolService,
            GenericDataTypeService sourceDataService, GenericDataTypeService destDataService) {
        super(fetchSize, 1000, false, workPoolService, sourceDataService, destDataService);
        this.sourceDataService = sourceDataService;
        this.destDataService = destDataService;
    }

    /**
     * @return the sourceDataService
     */
    protected GenericDataTypeService getSourceDataService() {
        return sourceDataService;
    }

    /**
     * @return the destDataService
     */
    protected GenericDataTypeService getDestDataService() {
        return destDataService;
    }
    
    /**
     * Установка опций
     * @param cols
     * @param statement
     * @param resultSet
     * @return
     * @throws SQLException
     */
    protected String setOptions(Map<String, Integer> cols, PreparedStatement statement, ResultSet resultSet) throws SQLException {
        int parameterIndex = 1;
        String pri = "";
        for (String colsName : cols.keySet()) {
            JdbcMetadata.setOptionStatementPrimaryColumns(statement, 
                resultSet, cols.get(colsName), parameterIndex++, colsName);
            pri += String.format("%s = [%s]", colsName, resultSet.getObject(colsName));
        }
        return pri;
    }
    
    /**
     * Обнуление списка
     * @param data
     * @return
     */
    protected StringBuffer getStringBuffer(StrategyModel data){
        return new StringBuffer(
                    String.format("Ошибка в целостности реплицированных данных [%s => %s]\n",
                            data.getRunner().getSource().getPoolId(),
                            data.getRunner().getTarget().getPoolId()));
    }
    
    /**
     * Подготовка строки для лога
     * @param rowDumpEmail
     * @param partEmail
     * @return
     */
    protected String createInfoLog(StringBuffer rowDumpEmail, int partEmail) {
        return  rowDumpEmail.toString().replace("###", String.valueOf(partEmail));
    }
    
    /**
     * получение кол-во часте для отправки сообщений
     * @param data
     * @return
     */
    protected int getPartEmail(StrategyModel data) {
        if (partEmail==null) {
            partEmail = 10;
            if(data.getParam(PART_EMAIL)!=null) {
                partEmail = Integer.parseInt(data.getParam(PART_EMAIL));
            }
        }
        return partEmail;
    }

    /**
     * Получение раннера
     * @param data
     * @return
     */
    protected int getRunner(StrategyModel data) {
        if (idRunner==null) {
            if(data.getParam(ID_RUNNER)!=null) {
                idRunner = Integer.parseInt(data.getParam(ID_RUNNER));
            }
        }
        return idRunner;
    }
    
    /**
     * Функция отбора обрабатываемых операций из очереди операций.
     * Для каждой операции вызывается функция replicateOperation(...).
     * 
     * 
     * @param sourceConnection  - соединение к источнику данных
     * @param targetConnection  - целевое соединение
     * @param data              - данные стратегии
     * 
     * @throws SQLException
     * @throws ClassNotFoundException 
     */
    protected void selectLastOperations(Connection sourceConnection, 
            Connection targetConnection, StrategyModel data) throws SQLException, ClassNotFoundException {
        // Задаем первоначальное смещение выборки равное 0.
        // При появлении ошибочных записей будем его увеличивать на 1.
        int offset = 0;
        // Извлекаем список последних операций по измененым записям
        PreparedStatement deleteWorkPoolData = 
                getWorkPoolService().getClearWorkPoolDataStatement();
        ResultSet operationsResult = 
                getWorkPoolService().getLastOperations(getRunner(data), getFetchSize(), offset);
        try {
            countPart = 1;
            rowDumpEmail = getStringBuffer(data);
            // Проходим по списку измененных записей
            for (int rowsCount = 1; operationsResult.next(); rowsCount++) {
                // Реплицируем операцию
                if (!replicateOperation(data, operationsResult)) {
                    if (isStrict()) {
                        break;
                    } else {
                        offset++;
                    }
                }

                // Периодически сбрасываем батч в БД
                if ((rowsCount % getBatchSize()) == 0) {
                    deleteWorkPoolData.executeBatch();
                    sourceConnection.commit();

                    // Извлекаем новую порцию данных
                    operationsResult.close();
                    operationsResult = getWorkPoolService().getLastOperations(getRunner(data), getFetchSize(), offset);

                    LOG.info(String.format("Раннер [id_runner = %s, %s] Стратегия [id = %s]: Обработано %s строк...", 
                            data.getRunner().getId(), data.getRunner().getDescription(), data.getId(), rowsCount));
                }
            }
            if(countPart != 1) {
                LOG.error(createInfoLog(rowDumpEmail, (countPart-1)));
            }
        } finally {
            operationsResult.close();
        }
        // Подтверждаем транзакцию
        deleteWorkPoolData.executeBatch();
        sourceConnection.commit();
    }
    
    /* (non-Javadoc)
     * @see ru.taximaxim.dbreplicator2.replica.strategies.replication.GenericAlgorithm#replicateOperation(ru.taximaxim.dbreplicator2.model.StrategyModel, ru.taximaxim.dbreplicator2.replica.strategies.replication.workpool.WorkPoolService, ru.taximaxim.dbreplicator2.replica.strategies.replication.data.DataService, ru.taximaxim.dbreplicator2.replica.strategies.replication.data.DataService, java.sql.ResultSet)
     */
    @Override
    protected boolean replicateOperation(StrategyModel data, ResultSet operationsResult) throws SQLException {
        boolean result = true;
        TableModel table = data.getRunner().getSource().getTable(getWorkPoolService().getTable(operationsResult));
        
        Map<String, Integer> colmSourcePri = new HashMap<String, Integer>(getSourceDataService().getPriColsTypes(table));
        Map<String, Integer> colsSource = new HashMap<String, Integer>(getSourceDataService().getAllColsTypes(table));
        
        // Извлекаем данные из исходной таблицы
        PreparedStatement selectSourceStatement = getSourceDataService().getSelectStatement(table);
        selectSourceStatement.setLong(1, getWorkPoolService().getForeign(operationsResult));
        
        PreparedStatement selectTargetStatement = getDestDataService().getSelectStatement(table);
        
        try (ResultSet sourceResult = selectSourceStatement.executeQuery();) {
            while(sourceResult.next()) {
                String prikey = setOptions(colmSourcePri, selectTargetStatement, sourceResult);
                try (ResultSet targetResult = selectTargetStatement.executeQuery();) {
                    if(countPart > getPartEmail(data)) {
                        countPart = 1;
                        LOG.error(createInfoLog(rowDumpEmail, getPartEmail(data)));
                        rowDumpEmail = getStringBuffer(data);
                    }
                    if(targetResult.next()) {
                        StringBuffer rowDumpHead = new StringBuffer(String.format(
                            "Ошибка (%s из ###) в table: %s, данные не равны в row [%s] values: ",
                            countPart,
                            table.getName(),
                            prikey));
                            
                        boolean errorRows = false;
                        for (String colsName : colsSource.keySet()) {
                            if(!JdbcMetadata.isEquals(sourceResult, targetResult, colsName, colsSource.get(colsName))) {
                                String rowDump = String.format(
                                        "[ col %s => [%s != %s] ] ",
                                        colsName,
                                        sourceResult.getObject(colsName),
                                        targetResult.getObject(colsName));
                                rowDumpHead.append(rowDump);
                                errorRows = true;
                             }
                        }
                        if(errorRows) {
                            countPart++;
                            result = false;
                            rowDumpHead.append("\n==========================================\n");
                            rowDumpEmail.append(rowDumpHead.toString());
                            if(countPart > getPartEmail(data)) {
                                countPart = 1;
                                LOG.error(createInfoLog(rowDumpEmail, getPartEmail(data)));
                                rowDumpEmail = getStringBuffer(data);
                            }
                        }
                    } else {
                        String rowDump = String.format(
                            "Ошибка (%s из ###) в table: %s, отсутствует запись row = [%s] %s",
                            countPart,
                            table.getName(),
                            Jdbc.resultSetToString(sourceResult, new ArrayList<String>(colsSource.keySet())),
                            "\n==========================================\n");
                        rowDumpEmail.append(rowDump);
                        countPart++;
                        result = false;
                    }
                }
            }
        }
        return result;
    }
}