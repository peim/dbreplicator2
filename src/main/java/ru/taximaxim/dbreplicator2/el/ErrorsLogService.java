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

package ru.taximaxim.dbreplicator2.el;

import java.sql.SQLException;

/**
 * @author mardanov_rm
 *
 */
public interface ErrorsLogService {
    
    /**
     * Функция добавления записи в лог ошибок
     * 
     * ...
     * 
     * @throws SQLException
     */
    void add(Integer runnerId, String tableId, Long foreignId, String error);
    
    /**
     * Функция добавления записи в лог ошибок
     * 
     * ...
     * 
     * @throws SQLException
     */
    void add(Integer runnerId, String tableId, Long foreignId,  String error, SQLException e);
    
    /**
     * Функция добавления записи в лог ошибок
     * 
     * ...
     * 
     * @throws SQLException
     */
    void add(Integer runnerId, String tableId, Long foreignId, String error, Exception e);
    /**
     * Функция установки статуса сообщений об ошибке
     * 
     * ...
     * 
     * @throws SQLException
     */
    void setStatus(Integer runnerId, String tableId, Long foreignId, int status);
}
