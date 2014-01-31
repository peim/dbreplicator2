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
 * Функции для конструирования запросов
 * 
 */
package ru.taximaxim.dbreplicator2.jdbc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author volodin_aa
 * 
 */
public final class QueryConstructors {
    
    private static final String INSERT_INTO = "INSERT INTO ";
    private static final String VALUES = ") VALUES (";
    private static final String SELECT = "SELECT ";
    private static final String FROM = " FROM ";
    private static final String WHERE = " WHERE ";
    private static final String ORDER_BY = " ORDER BY ";
    private static final String AND = " AND ";
    private static final String DELETE_FROM = "DELETE FROM ";
    private static final String UPDATE = "UPDATE ";
    private static final String SET = " SET ";
    
    /**
     * Сиглетон
     */
    private QueryConstructors() {

    }

    /**
     * Строит строку из элементов списка, разделенных разделителем delimiter
     * 
     * @param list
     *            список объектов
     * @param delimiter
     *            разделитель
     * @return строка из элементов списка, разделенных разделителем delimiter
     */
    public static String listToString(List<?> list, String delimiter) {
        StringBuffer result = new StringBuffer();

        boolean setComma = false;
        for (Object val : list) {
            if (setComma) {
                result.append(delimiter);
            } else {
                setComma = true;
            }
            result.append(val);
        }

        return result.toString();
    }

    /**
     * Строит строку из элементов списка с добавленным postfix в конце и
     * разделенных разделителем delimiter
     * 
     * @param list
     *            список объектов
     * @param delimiter
     *            разделитель
     * @param postfix
     *            строка для добавления после строки элемента
     * @return строка из элементов списка с добавленным postfix в конце и
     *         разделенных разделителем delimiter
     */
    public static String listToString(List<?> list, String delimiter, String postfix) {
        StringBuffer result = new StringBuffer();

        boolean setComma = false;
        for (Object val : list) {
            if (setComma) {
                result.append(delimiter);
            } else {
                setComma = true;
            }
            result.append(val).append(postfix);
        }

        return result.toString();
    }

    /**
     * Строит список вопрос для передачи экранированых параметров
     * 
     * @param colsList
     *            список колонок
     * @return список вопрос для передачи экранированых параметров
     */
    public static List<String> questionMarks(List<?> colsList) {
        List<String> result = new ArrayList<String>();
        int colsListSize = colsList.size();
        for (int i = 0; i < colsListSize; i++) {
            result.add("?");
        }

        return result;
    }

    /**
     * Генерирует строку запроса для вставки данных
     * 
     * @param tableName
     *            имя целевой таблицы
     * @param colsList
     *            список колонок
     * @return строка запроса для вставки данных
     */
    public static String constructInsertQuery(String tableName, List<String> colsList) {
        StringBuffer insertQuery = new StringBuffer().append(INSERT_INTO)
                .append(tableName).append("(").append(listToString(colsList, ", "))
                .append(VALUES).append(listToString(questionMarks(colsList), ", "))
                .append(")");

        return insertQuery.toString();
    }

    /**
     * Генерирует строку запроса следующего вида: INSERT INTO
     * <table>
     * (<cols>) SELECT (<questionsMarks>) Это позволяет создавать запросы на
     * вставку по условию
     * 
     * @param tableName
     *            имя целевой таблицы
     * @param colsList
     *            список колонок
     * @return строка запроса вставки из запроса выборки
     */
    public static String constructInsertSelectQuery(String tableName,
            List<String> colsList) {
        StringBuffer insertQuery = new StringBuffer().append(INSERT_INTO)
                .append(tableName).append("(").append(listToString(colsList, ", "))
                .append(") ").append(constructSelectQuery(questionMarks(colsList)));

        return insertQuery.toString();
    }

    /**
     * Создает строку запроса на выборку данных
     * 
     * @param colsList
     *            список колонок для вставки
     * @return строкf запроса на выборку данных
     */
    public static String constructSelectQuery(List<String> colsList) {
        StringBuffer query = new StringBuffer().append(SELECT).append(
                listToString(colsList, ", "));

        return query.toString();
    }

    /**
     * Создает строку запроса на выборку данных из таблицы
     * 
     * @param tableName
     *            имя целевой таблицы
     * @param colsList
     *            список колонок
     * @return строка запроса на выборку данных из таблицы
     */
    public static String constructSelectQuery(String tableName, List<String> colsList) {
        StringBuffer query = new StringBuffer(constructSelectQuery(colsList)).append(
                FROM).append(tableName);

        return query.toString();
    }

    /**
     * Создает строку запроса на выборку данных из таблицы с условием
     * 
     * @param tableName
     *            имя целевой таблицы
     * @param colsList
     *            список колонок
     * @param whereList
     *            список колонок условия
     * @return строка запроса на выборку данных из таблицы с условием
     */
    public static String constructSelectQuery(String tableName, List<String> colsList,
            List<String> whereList) {
        StringBuffer query = new StringBuffer(constructSelectQuery(tableName, colsList))
                .append(WHERE).append(listToString(whereList, AND, "=?"));

        return query.toString();
    }
    
    /**
     * Создает строку запроса на выборку данных из таблицы с условием в группировкой по ключевым полям
     * @param tableName
     * @param colsList
     * @param whereList
     * @param orderByList
     * @return
     */
    public static String constructSelectQuery(String tableName, List<String> colsList,
            List<String> whereList, List<String> orderByList) {
        StringBuffer query = new StringBuffer(constructSelectQuery(tableName, colsList));
        if(whereList != null) {
            query.append(WHERE).append(listToString(whereList, AND, "=?"));
        }
        
        query.append(ORDER_BY).append(listToString(orderByList, ", "));

        return query.toString();
    }

    /**
     * Создает строку запроса на выборку данных из таблицы с условием
     * 
     * @param tableName
     *            имя целевой таблицы
     * @param colsList
     *            список колонок
     * @param where
     *            условие
     * @return строка запроса на выборку данных из таблицы с условием
     */
    public static String constructSelectQuery(String tableName, List<String> colsList,
            String where) {
        StringBuffer query = new StringBuffer(constructSelectQuery(tableName, colsList))
                .append(WHERE).append(where);

        return query.toString();
    }

    /**
     * Создает строку запроса на удаление данных из таблицы с условием
     * 
     * @param tableName
     *            имя целевой таблицы
     * @param whereList
     *            список колонок условия
     * @return
     */
    public static String constructDeleteQuery(String tableName, List<String> whereList) {
        StringBuffer query = new StringBuffer().append(DELETE_FROM).append(tableName)
                .append(WHERE).append(listToString(whereList, AND, "=?"));

        return query.toString();
    }

    /**
     * Генерирует строку запроса для обновления данных
     * 
     * @param tableName
     *            имя целевой таблицы
     * @param colsList
     *            список колонок
     * @param whereList
     *            список колонок условия
     * @return строка запроса для обновления данных
     */
    public static String constructUpdateQuery(String tableName, List<String> colsList,
            List<String> whereList) {
        StringBuffer insertQuery = new StringBuffer().append(UPDATE).append(tableName)
                .append(SET).append(listToString(colsList, ", ", "=?"))
                .append(WHERE).append(listToString(whereList, AND, "=?"));

        return insertQuery.toString();
    }
}
