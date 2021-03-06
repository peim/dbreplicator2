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

package ru.taximaxim.dbreplicator2;


import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.taximaxim.dbreplicator2.cf.ConnectionFactory;
import ru.taximaxim.dbreplicator2.model.RunnerService;
import ru.taximaxim.dbreplicator2.tp.WorkerThread;
import ru.taximaxim.dbreplicator2.utils.Core;

/**
 * Тест репликации данных между базами H2-H2. 
 * 
 * Данный тест использует асинхронный менеджер записей супер лог таблицы, 
 * поэтому после каждого цикла репликации вызывается инструкция 
 * Thread.sleep(REPLICATION_DELAY); Тест может некорректно работать на медленных 
 * машинах, при необходимости подгонять величину задержки вручную!
 * 
 * @author volodin_aa
 *
 */
public class IntegrityReplicatedDataTest {
    protected static final Logger LOG = Logger.getLogger(IntegrityReplicatedDataTest.class);
    
    // Задержка между циклами репликации
    private static final int REPLICATION_DELAY = 1500;
    
    protected static SessionFactory sessionFactory;
    protected static Session session;
    protected static ConnectionFactory connectionFactory;
    protected static Connection conn = null;
    protected static Connection connDest = null;
    protected static Runnable worker = null;
    protected static Runnable errorsCountWatchdogWorker = null;
    protected static Runnable errorsIntegrityReplicatedData = null;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Core.configurationClose();
        Core.getConfiguration("src/test/resources/hibernateIntegrityReplicatedData.cfg.xml");
        sessionFactory = Core.getSessionFactory();
        session = sessionFactory.openSession();
        connectionFactory = Core.getConnectionFactory();
        initialization();
    }

    @AfterClass
    public static void setUpAfterClass() throws Exception {
        if(conn!=null)
            conn.close();
        if(connDest!=null)
            connDest.close();
        if(session!=null)
            session.close();
        Core.configurationClose();
        Core.connectionFactoryClose();
        Core.sessionFactoryClose();
        Core.statsServiceClose();
    }
    
    /**
     * Проверка вставки 
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws InterruptedException 
     */
    @Test
    public void testInsert() throws SQLException, ClassNotFoundException, IOException, InterruptedException {
      //Проверка вставки
        Helper.executeSqlFromFile(conn, "sql_insert.sql");
        worker.run();
        Helper.executeSqlFromFile(conn, "sql_update.sql"); 
        worker.run();
        Thread.sleep(REPLICATION_DELAY);
        List<MyTablesType> listSource = Helper.InfoTest(conn, "t_table");
        List<MyTablesType> listDest   = Helper.InfoTest(connDest, "t_table");
        Helper.AssertEquals(listSource, listDest);

        listSource = Helper.InfoTest(conn, "t_table1");
        listDest   = Helper.InfoTest(connDest, "t_table1");
        Helper.AssertEquals(listSource, listDest);
        Thread.sleep(REPLICATION_DELAY);
        //Helper.executeSqlFromFile(connDest, "sql_insert.sql"); 
        Helper.executeSqlFromFile(connDest, "sql_update.sql"); 
        Helper.executeSqlFromFile(connDest, "sql_delete.sql"); 
        //Helper.InfoSelect(conn, "rep2_workpool_data");
        int count_rep2_errors_log = Helper.InfoCount(conn, "rep2_errors_log");
        assertTrue(String.format("rep2_errors_log чистый [%s==0]", count_rep2_errors_log), count_rep2_errors_log== 0);
        
        errorsIntegrityReplicatedData.run();
        Thread.sleep(REPLICATION_DELAY);
        int count_rep2_workpool_data = Helper.InfoCount(conn, "rep2_workpool_data");
        assertTrue(String.format("Должны быть ошибки [%s!=0]", count_rep2_workpool_data), count_rep2_workpool_data!= 0);
        
        count_rep2_errors_log = Helper.InfoCount(conn, "rep2_errors_log where c_status = 0");
        assertTrue(String.format("Должны быть ошибки rep2_errors_log [%s!=0]", count_rep2_errors_log), count_rep2_errors_log!= 0);

        Helper.executeSqlFromFile(conn, "sql_delete.sql");     
        worker.run();
        Helper.executeSqlFromFile(conn, "sql_update.sql"); 
        worker.run();
        Thread.sleep(REPLICATION_DELAY);
        
        PreparedStatement statement = conn.prepareStatement("INSERT INTO T_TABLE1 (id, _int, _boolean, _long, _decimal, _double, _float, _string, _byte, _date, _time, _timestamp) select  id_foreign, 2, true, 5968326496, 99.65, 5.62, 79.6, 'rasfar', 0, now(), now(), now() from rep2_workpool_data where id_table='T_TABLE1' and c_operation = 'D'");
        statement.execute();
        statement.close();
        worker.run();
        Thread.sleep(REPLICATION_DELAY);
        
        errorsIntegrityReplicatedData.run();
        Thread.sleep(REPLICATION_DELAY);
        
        count_rep2_errors_log = Helper.InfoCount(conn, "rep2_errors_log where c_status = 0"); 
        assertTrue(String.format("Должны быть ошибки исправлены rep2_errors_log [%s==0]", count_rep2_errors_log), count_rep2_errors_log == 0);
        
        Helper.InfoSelect(conn, "rep2_errors_log");
    }
    
    /**
     * Инициализация
     */
    public static void initialization() throws ClassNotFoundException, SQLException, IOException{
        LOG.info("initialization");
        String source = "source";
        conn = connectionFactory.getConnection(source);
        
        Helper.executeSqlFromFile(conn, "importRep2.sql");
        Helper.executeSqlFromFile(conn, "importSource.sql");
        
        String dest = "dest";
        connDest = connectionFactory.getConnection(dest);
        Helper.executeSqlFromFile(connDest, "importRep2.sql");
        Helper.executeSqlFromFile(connDest, "importDest.sql");
        
        RunnerService runnerService = new RunnerService(sessionFactory);

        worker = new WorkerThread(runnerService.getRunner(1));
        errorsCountWatchdogWorker = new WorkerThread(runnerService.getRunner(7));
        errorsIntegrityReplicatedData = new WorkerThread(runnerService.getRunner(16));
    }
}
