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

/**
 * Функции для работы с метаданными
 * 
 */
package ru.taximaxim.dbreplicator2.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author volodin_aa
 * 
 */
public final class JdbcMetadata {

    /**
     * Сиглетон
     */
    private JdbcMetadata() {

    }

    /**
     * Функция получения списка колонок таблицы на основе метаданных БД
     * 
     * @param connection
     *            соединение к целевой БД
     * @param tableName
     *            имя таблицы
     * @return список колонок таблицы
     * @throws SQLException
     */
    public static Set<String> getColumns(Connection connection, String tableName)
            throws SQLException {
        // Получаем список колонок
        Set<String> colsList = new HashSet<String>();
        DatabaseMetaData metaData = connection.getMetaData();
        
        try (ResultSet colsResultSet = metaData.getColumns(null, null, tableName, null);) {
            while (colsResultSet.next()) {
                colsList.add(colsResultSet.getString("COLUMN_NAME").toUpperCase());
            }
        }

        return colsList;
    }

    /**
     * Функция получения списка колонок таблицы на основе метаданных ResultSet
     * 
     * @param result
     *            набор результатов
     * @param tableName
     *            имя таблицы
     * @return список колонок таблицы
     * @throws SQLException
     */
    public static Set<String> getColumns(ResultSet result)
            throws SQLException {
        // Получаем список колонок
        Set<String> colsList = new HashSet<String>();
        ResultSetMetaData metaData = result.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            colsList.add(metaData.getColumnName(i).toUpperCase());
        }

        return colsList;
    }

    /**
     * Функция получения списка ключевых колонок таблицы на основе метаданных БД
     * 
     * @param connection
     *            соединение к целевой БД
     * @param tableName
     *            имя таблицы
     * @return список ключевых колонок таблицы
     * @throws SQLException
     */
    public static Set<String> getPrimaryColumns(Connection connection,
            String tableName) throws SQLException {
        // Получаем список ключевых колонок
        Set<String> primaryKeyColsList = new HashSet<String>();
        DatabaseMetaData metaData = connection.getMetaData();
        
        try (ResultSet primaryKeysResultSet = metaData.getPrimaryKeys(null, null, tableName);) {
            while (primaryKeysResultSet.next()) {
                primaryKeyColsList.add(primaryKeysResultSet.getString("COLUMN_NAME").toUpperCase());
            }
        }

        return primaryKeyColsList;
    }
    
    /**
     * Функция получения списка AUTO INCREMENT колонок таблицы на основе метаданных БД
     * 
     * @param connection
     *            соединение к целевой БД
     * @param tableName
     *            имя таблицы
     * @return список AUTO INCREMENT колонок таблицы
     * @throws SQLException
     */
    public static Set<String> getIdentityColumns(Connection connection,
            String tableName) throws SQLException {
        // Получаем список колонок
        Set<String> colsList = new HashSet<String>();
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet colsResultSet = metaData.getColumns(null, null, tableName, null);) {
            while (colsResultSet.next()) {
                if (colsResultSet.getString("IS_AUTOINCREMENT").equalsIgnoreCase("YES")) {
                    colsList.add(colsResultSet.getString("COLUMN_NAME").toUpperCase());
                }
            }
        }

        return colsList;
    }
    
    /**
     * Функция получения набора колонок таблицы с возможностью вставки значений 
     * NULL на основе метаданных БД
     * 
     * @param connection
     *            соединение к целевой БД
     * @param tableName
     *            имя таблицы
     * @return набор колонок таблицы с возможностью вставки значений NULL
     * @throws SQLException
     */
    public static Set<String> getNullableColumns(Connection connection,
            String tableName) throws SQLException {
        // Получаем список колонок
        Set<String> cols = new HashSet<String>();
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet colsResultSet = metaData.getColumns(null, null, tableName, null);) {
            while (colsResultSet.next()) {
                if (colsResultSet.getString("IS_NULLABLE").equalsIgnoreCase("YES")) {
                    cols.add(colsResultSet.getString("COLUMN_NAME").toUpperCase());
                }
            }
        }

        return cols;
    }

}
