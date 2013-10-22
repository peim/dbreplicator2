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

import org.apache.log4j.Logger;

import ru.taximaxim.dbreplicator2.model.RunnerModel;

public class WorkerThread implements Runnable {

	public static final Logger LOG = Logger.getLogger(WorkerThread.class);
	
	private RunnerModel runner;

	public WorkerThread(RunnerModel runner) {
		this.runner = runner;
	}

	public void run() {
		LOG.info(String.format("Запуск потока: %s [%s] [%s]", 
				runner.getDescription(), runner.getId(), 
				Thread.currentThread().getName()));

		processCommand(runner);

		LOG.info(String.format("Завершение потока: %s [%s] [%s]", 
				runner.getDescription(), runner.getId(), 
				Thread.currentThread().getName()));
	}

	/**
	 * Запуск рабочего потока. Вся работа по выполнению репликации должна 
	 * выполняться здесь.
	 * 
	 * @param runner Настроенный runner.
	 */
	public void processCommand(RunnerModel runner) {
		// ...
		// Запуск
		// код
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}