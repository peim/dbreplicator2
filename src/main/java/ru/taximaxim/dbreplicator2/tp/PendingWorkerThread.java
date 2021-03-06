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
package ru.taximaxim.dbreplicator2.tp;

import java.util.Set;
import ru.taximaxim.dbreplicator2.model.Runner;
import ru.taximaxim.dbreplicator2.model.RunnerModel;

/**
 * Ожидающий рабочий поток. При запуске он удаляет раннера из набора
 * ожидающих обработки раннеров.
 * Реализует паттерн Proxy для интерфейса Runnable.
 * 
 * @author volodin_aa
 *
 */
public class PendingWorkerThread implements Runnable {

    private Set<RunnerModel> pendingRunners;
    private Runner runner;
    private WorkerThread workerThread;

    /**
     * Конструктор по списку ожидающих раннеров и раннеру
     * 
     * @param pendingRunners - список ожидающих обработки раннеров
     * @param runner - текущий раннер
     */
    public PendingWorkerThread(Runner runner, Set<RunnerModel> pendingRunners) {
        this.pendingRunners = pendingRunners;
        this.runner = runner;
    }

    @Override
    public void run() {
        // Пробуем захватить раннера
        synchronized (runner) {
            // Когда раннер захвачен и начинается обработка данных, то
            // убераем его из списка ожидающих начала обработки
            synchronized(pendingRunners) {
                pendingRunners.remove(runner);
            }
            getWorkerThread().run();
        }
    }

    /**
     * Метод с отложенной инициализацией потока обработчика раннера
     * 
     * @return the workerThread - поток обработчика раннера
     */
    protected synchronized WorkerThread getWorkerThread() {
        if (workerThread==null) {
            workerThread = new WorkerThread(runner);
        }
        return workerThread;
    }

}
