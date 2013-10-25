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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import ru.taximaxim.dbreplicator2.model.RunnerModel;

public class ThreadPool {

    public static final Logger LOG = Logger.getLogger(ThreadPool.class);

    public ExecutorService executor = null;

    public ThreadPool(int count) {
        restart(count);
    }

    /**
     * Ожидание остановки выполнения всех потоков и завершение работы.
     */
    public void shutdown() {

        if (executor != null) {
            executor.shutdown();

            while (!executor.isTerminated()) {
                // wait wile all tasks have completed following shut down
            }

            LOG.info("ThreadPool.shutdown()");
        }
    }

    /**
     * Ожидание завершения всех потоков и инициализация нового пула с новым
     * значением.
     */
    public void restart(int count) {
        shutdown();
        executor = Executors.newFixedThreadPool(count);

        LOG.info(String.format("ThreadPool.restart(%s)", count));
    }

    /**
     * Запуск потока RunnerModel. Поток будет поставлен в очередь на выполнение.
     */
    public void start(RunnerModel runner) {
        Runnable worker = new WorkerThread(runner);
        executor.execute(worker);
    }
}
