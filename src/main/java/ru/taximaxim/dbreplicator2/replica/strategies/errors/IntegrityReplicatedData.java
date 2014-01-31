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

package ru.taximaxim.dbreplicator2.replica.strategies.errors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import ru.taximaxim.dbreplicator2.jdbc.Jdbc;
import ru.taximaxim.dbreplicator2.jdbc.JdbcMetadata;
import ru.taximaxim.dbreplicator2.model.StrategyModel;
import ru.taximaxim.dbreplicator2.model.TableModel;
import ru.taximaxim.dbreplicator2.replica.Strategy;
import ru.taximaxim.dbreplicator2.replica.StrategyException;
import ru.taximaxim.dbreplicator2.replica.strategies.replication.StrategySkeleton;
import ru.taximaxim.dbreplicator2.replica.strategies.replication.data.GenericDataService;
import ru.taximaxim.dbreplicator2.replica.strategies.replication.workpool.GenericWorkPoolService;

/**
 * @author mardanov_rm
 */
public class IntegrityReplicatedData extends StrategySkeleton implements Strategy {
    public static final Logger LOG = Logger.getLogger(IntegrityReplicatedData.class);

    private static final String PART_EMAIL = "partEmail";
    private static final String TABLES = "tables";
    private static final String LOG_PART_TABLES = "logPartTables";
    /**
     * Конструктор по умолчанию
     */
    public IntegrityReplicatedData() {
    }
    
    /**
     * Установка опций
     * @param cols
     * @param statement
     * @param resultSet
     * @return
     * @throws SQLException
     */
    public String setOptions(Map<String, Integer> cols, PreparedStatement statement, ResultSet resultSet) throws SQLException {
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
    private StringBuffer newStringBuffer(StrategyModel data){
        return new StringBuffer(
                String.format("Ошибка в целостности реплицированных данных [%s => %s]\n",
                        data.getRunner().getSource().getPoolId(),
                        data.getRunner().getTarget().getPoolId()));
    }
    
    private String createInfoLog(StringBuffer rowDumpEmail, int partEmail) {
        return  rowDumpEmail.toString().replace("###", String.valueOf(partEmail));
    }
    
    @Override
    public void execute(Connection sourceConnection, Connection targetConnection, StrategyModel data) 
            throws StrategyException, SQLException, ClassNotFoundException {
        
        try (GenericWorkPoolService workPoolService = new GenericWorkPoolService(sourceConnection);
                GenericDataService dataServiceSource = new GenericDataService(sourceConnection);
                GenericDataService dataServiceTarget = new GenericDataService(targetConnection);) {

            
            String tables = null;
            if(data.getParam(TABLES)!=null) {
                tables = data.getParam(TABLES);
            } else {
                throw new StrategyException(new Exception("Не указаны таблицы, проверьте настройки!!!"));
            }
            
            
            int partEmail = 10;
            if(data.getParam(PART_EMAIL)!=null) {
                partEmail = Integer.parseInt(data.getParam(PART_EMAIL));
            }
            
            Boolean logPartTables = true;
            if(data.getParam(LOG_PART_TABLES)!=null) {
                logPartTables = Boolean.parseBoolean(data.getParam(LOG_PART_TABLES));
            }
            
            int count = 1;
            StringBuffer rowDumpEmail = newStringBuffer(data);
            
            // Строим список таблиц
            StringTokenizer tableName = new StringTokenizer(tables, ",");
            while (tableName.hasMoreTokens()) {
                if(logPartTables) {
                    count = 1;
                    rowDumpEmail = newStringBuffer(data);
                }              
                
                TableModel table = data.getRunner().getSource().getTable(tableName.nextToken());                
                PreparedStatement selectSourceStatement = dataServiceSource.getSelectStatementAll(table);
                PreparedStatement selectTargetStatement = dataServiceTarget.getSelectStatement(table);
                Map<String, Integer> colmSourcePri = new HashMap<String, Integer>(dataServiceSource.getPriColsTypes(table));
                Map<String, Integer> colsSource = new HashMap<String, Integer>(dataServiceSource.getAllColsTypes(table));
  
                try (ResultSet sourceResult = selectSourceStatement.executeQuery();) {
                    while(sourceResult.next()) {
                        
                        String prikey = setOptions(colmSourcePri, selectTargetStatement, sourceResult);
                        try (ResultSet targetResult = selectTargetStatement.executeQuery();) {
                            if(count > partEmail) {
                                count = 1;
                                LOG.error(createInfoLog(rowDumpEmail, partEmail));
                                rowDumpEmail = newStringBuffer(data);
                            }
                            if(targetResult.next()) {
                                StringBuffer rowDumpHead = new StringBuffer(String.format(
                                        "Ошибка (%s из ###) в table: %s, данные не равны в row [%s] values: ",
                                        count,
                                        table.getName(),
                                        prikey));
                                        
                                boolean errorRows = false;
                                for (String colsName : colsSource.keySet()) {
                                    if(!JdbcMetadata.isEquals(sourceResult, targetResult, colsName, 
                                            colsSource.get(colsName))) {
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
                                    count++;
                                    rowDumpHead.append("\n==========================================\n");
                                    rowDumpEmail.append(rowDumpHead.toString());
                                    if(count > partEmail) {
                                        count = 1;
                                        LOG.error(createInfoLog(rowDumpEmail, partEmail));
                                        rowDumpEmail = newStringBuffer(data);
                                    }
                                }
                            } else {
                                String rowDump = String.format(
                                        "Ошибка (%s из ###) в table: %s, отсутствует запись row = [%s] %s",
                                        count,
                                        table.getName(),
                                        Jdbc.resultSetToString(sourceResult, new ArrayList<String>(colsSource.keySet())),
                                        "\n==========================================\n");
                                rowDumpEmail.append(rowDump);
                                count++;
                            }
                            
                            
                        }
                    }
                }
                if(count != 1 & logPartTables) {
                    LOG.error(createInfoLog(rowDumpEmail, (count-1)));
                }
            }
            if(count != 1 & !logPartTables) {
                LOG.error(createInfoLog(rowDumpEmail, (count-1)));
            }
        }
    }
}